package graphics;

import com.google.gson.JsonElement;
import core.Apiary;
import org.joml.Matrix4f;
import org.lwjgl.opengl.ATIMeminfo;
import org.lwjgl.opengl.GL43;
import compiler.GLSLCompiler;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
import simulation.SimulationManager;
import util.MathUtil;
import util.StringUtils;

import java.util.*;

public class ShaderManager {

    // Singleton Instance
    private static ShaderManager singleton;

    public final byte ALIGNMENT = 4;
    private final String GLSL_VERSION;

    // Constants used
    public static final int GL_MAJOR_VERSION = 4;
    public static final int GL_MINOR_VERSION = 3;
    private static final String READ_NAME  = "_read";
    private static final String WRITE_NAME = "_write";

    // check which GPU we have
    private static GPUType gpu;

    // Allocated
    private final HashSet<Integer> reserved_shaders = new HashSet<>(); // any default shaders cannot be deleted.
    private final HashSet<Integer> allocated_shaders = new HashSet<>();
    private final HashSet<Integer> allocated_programs = new HashSet<>();

    // This is the currently bound shader program
    private int bound_shader_program_id = -1;

    // Default shader programs.
    private final int DEFAULT_VERTEX_SHADER;
    private final int DEFAULT_GEOMETRY_SHADER;
    private final int DEFAULT_FRAGMENT_SHADER;

    // Indies used for SSBO location binding
    private final int MAX_SHADER_STORAGE_BLOCK_LOCATIONS;
    private final HashSet<Integer> locations_buffer = new HashSet<>();
    private final LinkedList<Integer> available_layout_locations = new LinkedList<>();

    // GLSL libraries
    private LinkedHashSet<String> custom_directives = new LinkedHashSet<>();

    // These are some uniforms that exist globally in every simulation and project
    private HashMap<String, Uniform> uniforms = new HashMap<>();

    //TODO move to ScreenManager or WindowManager class.
    private final Uniform u_window_size;
    private final Uniform u_aspect_ratio;

    //TODO move to CameraManager classs.
    private final Uniform u_projection_matrix;
    private final float[] u_projection_matrix_data = new float[16];
    private final Uniform u_view_matrix;
    private final Matrix4f view_matrix = new Matrix4f().identity();
    private final float[] u_view_matrix_data = new float[16];

    private HashMap<String, GLDataType> type_mapping = new HashMap<>();

    private ShaderManager() {
        // Register any custom directives we want to support
        custom_directives.add("#include");

        MAX_SHADER_STORAGE_BLOCK_LOCATIONS = GL43.glGetInteger(GL43.GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS);
        for(int i = 0; i < MAX_SHADER_STORAGE_BLOCK_LOCATIONS; i++){
            available_layout_locations.add(i);
            locations_buffer.add(i);
        }


        // Define our typeMapping
        // By default we just use the name of the enum values
        for(GLDataType type : GLDataType.values()){
            associateNameWithType(type.name(), type);
        }
        // Here we can explicitly add more name associations to make the user experience easier.

        // Ask what the GLSL version is and set it
        String version_string = GL43.glGetString(GL43.GL_SHADING_LANGUAGE_VERSION);
        version_string = version_string.replace(".", "").substring(0,3);
        int version = Integer.parseInt(version_string);

        // If we are connected to a Graphics Card which can use the requested version of GLSL
        int desired_version = (GL_MAJOR_VERSION * 100) + (GL_MINOR_VERSION * 10);
        if(version >= desired_version){
            this.GLSL_VERSION = desired_version+"";
        }else{
            this.GLSL_VERSION = "";
            System.err.println("Error: The requested GL instance of version:"+GL_MAJOR_VERSION+"."+GL_MINOR_VERSION+" could not be created on this card. The connected GPU is using version:" + version_string);
            System.exit(1);
        }

        System.out.println("Shader Version:" + this.GLSL_VERSION);

        // Here we register our uniforms
        this.u_window_size = this.createUniform("u_window_size", GLDataType.VEC2);
        this.u_aspect_ratio = this.createUniform("u_aspect_ratio", GLDataType.FLOAT);

        this.u_projection_matrix = this.createUniform("u_projection_matrix", GLDataType.MAT4);
        MathUtil.createProjectionMatrix(u_projection_matrix_data, 70.0f, Apiary.getAspectRatio(), 0.1f, 1024.0f);
        this.u_projection_matrix.set(u_projection_matrix_data);

        this.u_view_matrix = this.createUniform("u_view_matrix", GLDataType.MAT4);
        this.view_matrix.translate(0, 0, -3);
        this.u_view_matrix.set(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        );

        // Here we setup our default shaders.
        String default_vertex_source =
            generateVersionString() +
            "layout (location = 0) in vec3 position;\n" +
            "uniform mat4 u_projection_matrix;\n" +
            "uniform mat4 u_view_matrix;\n" +
            "out vec3 pass_position;\n" +
            "void main(void){\n" +
            "pass_position = position;\n" +
            "gl_Position = u_projection_matrix * u_view_matrix * vec4(position, 1.0);\n" +
            "}\n";
        DEFAULT_VERTEX_SHADER = compileShader(GL43.GL_VERTEX_SHADER, default_vertex_source);
        reserved_shaders.add(DEFAULT_VERTEX_SHADER);

        String default_geometry_source =
            generateVersionString() +
            "layout (points) in;\n" +
            "layout (points, max_vertices = 1) out;\n" +
            "void main(void){\n" +
            "    gl_Position = gl_in[0].gl_Position; \n" +
            "    EmitVertex();\n" +
            "    EndPrimitive();\n" +
            "}\n";
        DEFAULT_GEOMETRY_SHADER = compileShader(GL43.GL_GEOMETRY_SHADER, default_geometry_source);
        reserved_shaders.add(DEFAULT_GEOMETRY_SHADER);

        String default_fragment_source =
            generateVersionString() +
            "in vec3 pass_position;\n" +
            "uniform vec2 u_window_size;\n" +
            "uniform vec2 u_mouse_scroll;\n" +
            "out vec4 out_color; \n" +
            "void main(void){\n" +
            "vec2 screen_pos = (pass_position.xy / vec2(u_mouse_scroll.y) + vec2(1.0)) / vec2(2.0);\n" +
            "uint x_pos = uint(floor(screen_pos.x * u_window_size.x));\n" +
            "uint y_pos = uint(floor(screen_pos.y * u_window_size.y));\n" +
            "uint fragment_index = x_pos + (y_pos * uint(u_window_size.x));\n" +
            "uint instance = uint(fragment_index);\n" +
            "out_color = vec4(screen_pos.xy, 0.0, 1.0);" +
//            "out_color = vec4(1.0);" +
            "}\n";
        DEFAULT_FRAGMENT_SHADER = compileShader(GL43.GL_FRAGMENT_SHADER, default_fragment_source);
        reserved_shaders.add(DEFAULT_FRAGMENT_SHADER);

        this.u_window_size.set(Apiary.getWindowWidth(), Apiary.getWindowHeight());
        this.u_aspect_ratio.set(Apiary.getAspectRatio());

    }

    // Singleton initializer and getter
    public static void initialize() {
        if (singleton == null) {
            singleton = new ShaderManager();
            singleton.onResize();

            // Determine which GPU we have
            long max = -1;
            GPUType system_gpu = null;
            for(GPUType known_gpu_type : GPUType.values()){
                long available_memory = known_gpu_type.getAvailableMemoryInBytes();
                if(available_memory > max){
                    max = available_memory;
                    system_gpu = known_gpu_type;
                }
            }
            gpu = system_gpu;
        }
    }

    public static ShaderManager getInstance() {
        return singleton;
    }

    public void onResize(){

    }

    public String getGLTargetVersion() {
        return this.GLSL_VERSION;
    }

    private void associateNameWithType(String name, GLDataType type){
        type_mapping.put(name.toLowerCase(), type);
    }

    public GLDataType getGLDataTypeFromString(String type_name){
        if(type_mapping.containsKey(type_name.toLowerCase())){
            return type_mapping.get(type_name.toLowerCase());
        }
        return null;
    }

    private String glslPreCompiler(String shader_source){
        // We are going to traverse the shader_source and look for custom directives
        String[] shader_source_lines = shader_source.split("\n");

        for(int line_index = 0; line_index < shader_source_lines.length; line_index++){
            String line = shader_source_lines[line_index];
            line = line.trim(); // Remove leading and trailing whitespace.
            // Custom include header
            if(line.startsWith("#include")){
                String glsl_library_file_name = line.replace("#include", "").trim();
                // Inject the contents of the included library into our shader file, replaceing the include line with the content containted within the library.
                // This is a recursive operation so libraries can include other libraries.
                shader_source_lines[line_index] =  glslPreCompiler(StringUtils.load(String.format("/shaderlib/%s%s", glsl_library_file_name, !(glsl_library_file_name.endsWith(".glsl")) ? ".glsl" : "")) + "\n");
            }
        }

        return String.join("\n", shader_source_lines);
    }

    public int compileShader(int shader_type, String shader_source){
        // We are going to run our precompiler on the incoming shader source
        String precompiled_shader_source = glslPreCompiler(shader_source);

        // Determine the type of shader that we are loading
        String shader_type_name = "unknown";
        switch (shader_type){
            case GL43.GL_VERTEX_SHADER:{
                shader_type_name = "Vertex";
                break;
            }
            case GL43.GL_FRAGMENT_SHADER:{
                shader_type_name = "Fragment";
                break;
            }
            case GL43.GL_COMPUTE_SHADER:{
                shader_type_name = "Compute";
                break;
            }
            case GL43.GL_GEOMETRY_SHADER:{
                shader_type_name = "Geometry";
                break;
            }
        }

        // Allocate memory for the new shader program
        int shader_id   = GL43.glCreateShader(shader_type);
        this.allocated_shaders.add(shader_id);
        GL43.glShaderSource(shader_id, precompiled_shader_source);
        GL43.glCompileShader(shader_id);
        checkForError(String.format("Compiling %s Shader", shader_type_name));

        //Buffer for reading compile status
        int[] compile_buffer = new int[]{ 0 };

        GL43.glGetShaderiv(shader_id, GL43.GL_COMPILE_STATUS, compile_buffer);
        if (compile_buffer[0] == GL43.GL_FALSE) { // If our shader did not compile
            GL43.glGetShaderiv(shader_id, GL43.GL_INFO_LOG_LENGTH, compile_buffer);
            //Check that log exists
            if (compile_buffer[0] > 0) {
                String errorMesssage = GL43.glGetShaderInfoLog(shader_id);
                String line_number = "unknown";
                try {
                    line_number = errorMesssage.substring(errorMesssage.indexOf(":")+1, errorMesssage.indexOf("("));
                } catch (Exception e){
                    e.printStackTrace();
                }
                System.err.println(String.format("Error compiling %s shader| %s | %s", shader_type_name, line_number, GL43.glGetShaderInfoLog(shader_id)));
                System.err.println(precompiled_shader_source);
                //Cleanup our broken shader
                this.deleteShader(shader_id);

                return -1;
            }
        }else{
            if(compile_buffer[0] == GL43.GL_TRUE){
                checkForError("Compilation Error");
                System.out.println(String.format("%s Shader compiled Successfully.", shader_type_name));
            }else{
                System.out.println("Shader compiled in an unknown state. This may cause strange behavior.");
            }
        }

        return shader_id;
    }

    public int linkShader(int ... shaders){

        //Buffer for reading compile status
        int[] compile_buffer = new int[]{ 0 };

        //Now that we have our shaders compiled, we link them to a shader program.
        int program_id = GL43.glCreateProgram();
        this.allocated_programs.add(program_id);

        System.out.println("Linking Vertex and Fragment shaders to Program...");
        int attributeIndex = 1;
        for(String attribute : new String[]{"position"}){
            GL43.glBindAttribLocation(program_id, attributeIndex, attribute);
            attributeIndex++;
        }
        //Combine vertex and fragment shaders into one program
        for(int shader_id : shaders) {
            GL43.glAttachShader(program_id, shader_id);
        }

        checkForError("Attach Shaders");
        //Link
        GL43.glLinkProgram(program_id);
        checkForError("Linking Shaders");
        //Check that the link status was successful.
        GL43.glGetProgramiv(program_id, GL43.GL_LINK_STATUS, compile_buffer);
        if (compile_buffer[0] == GL43.GL_TRUE) {
            System.out.println("Successfully linked shaders into program.");
        }else{
            String error_message = GL43.glGetProgramInfoLog(program_id);
            System.err.println(String.format("Error linking shaders into program | %s ", error_message));
            //Cleanup our broken program
            GL43.glDeleteProgram(program_id);

            return -1;
        }

        return program_id;
    }

    public boolean checkForError(String error_cause){
        int error_check = GL43.glGetError();
        boolean had_error = false;
        while (error_check != GL43.GL_NO_ERROR) {
//            System.out.println(String.format("Error[%s]: %s", error_cause, error_check));
            error_check = GL43.glGetError();
            had_error = true;
        }
        return had_error;
    }

    // Uniform stuff
    public Uniform createUniform(String name, GLDataType type){
        Uniform uniform = new Uniform(name, type);
        this.uniforms.put(name, uniform);
        return uniform;
    }

    public void update(double delta){
        // TODO refactor to onResize
        has_sim:{
            if (SimulationManager.getInstance() != null) {
                if (SimulationManager.getInstance().hasActiveSimulation()) {
                    this.u_window_size.set(SimulationManager.getInstance().getActiveSimulation().getWorldWidth(), SimulationManager.getInstance().getActiveSimulation().getWorldHeight());
                    this.u_aspect_ratio.set((float) SimulationManager.getInstance().getActiveSimulation().getWorldWidth() / (float) SimulationManager.getInstance().getActiveSimulation().getWorldHeight());

                    // Update Projection Matrix
                    MathUtil.createProjectionMatrix(u_projection_matrix_data, 70.0f, Apiary.getAspectRatio(), 0.1f, 1024.0f);
                    this.u_projection_matrix.set(u_projection_matrix_data);

                    this.view_matrix.rotate((float) (-1.0f * delta), 0, 1, 0);
                    this.view_matrix.get(u_view_matrix_data);
                    this.u_view_matrix.set(u_view_matrix_data);

                    break has_sim;
                }
            }

            this.u_window_size.set(Apiary.getWindowWidth(), Apiary.getWindowHeight());
            this.u_aspect_ratio.set(Apiary.getAspectRatio());
        }
    }

    public String getGLSLVersion(){
        return GLSL_VERSION+"";
    }

    public void bind(int program_id) {
        this.bound_shader_program_id = program_id;
        GL43.glUseProgram(this.bound_shader_program_id);
        // Bind all uniforms
        bindUniforms();
    }

    public int getBoundShader(){
        return this.bound_shader_program_id;
    }

    public void bindUniforms() {
        for(Uniform uniform : this.uniforms.values()){
            uniform.bind();
        }
    }

    public int getNextAvailableSSBOLocation() {
        locations_buffer.clear();
        available_layout_locations.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return (o1 > o2) ? 1 : -1 ;
            }
        });

        int smallest_value = available_layout_locations.pop();

        for(int i : available_layout_locations){
            locations_buffer.add(i);
        }

        return smallest_value;
    }

    public void freeSSBOLocation(int layout_location){
        if(!locations_buffer.contains(layout_location) && layout_location < MAX_SHADER_STORAGE_BLOCK_LOCATIONS){
            locations_buffer.add(layout_location);
            available_layout_locations.add(layout_location);
        }
    }

    public String getSSBOReadIdentifier() {
        return READ_NAME;
    }

    public String getSSBOWriteIdentifier() {
        return WRITE_NAME;
    }

    public String generateVersionString() {
        return String.format("#version %s core\n", getGLTargetVersion());
    }

    public boolean hasUniform(String uniform_name) {
        return this.uniforms.containsKey(uniform_name);
    }

    public Uniform getUniform(String uniform_name) {
        return this.uniforms.get(uniform_name);
    }

    public Uniform getWindowSize(){
        return this.u_window_size;
    }

    public Uniform getAspectRatio(){
        return this.u_aspect_ratio;
    }

    public int getMaxWorkgroupInvocations(){
        return GL43.glGetInteger(GL43.GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS);
    }

    public void deleteShader(int id) {
        if(reserved_shaders.contains(id)){
            // Not allowed to delete the default shaders.
            // This can happen accidentally in the cleanup section of your code if you
            // are using a default shader and pass it to delete shader.
            return;
        }
        GL43.glDeleteShader(id);
        this.allocated_shaders.remove(id);
    }

    public void deleteProgram(int id) {
        GL43.glDeleteProgram(id);
        this.allocated_programs.remove(id);
    }

    public int generateVertexShaderFromPegs(JsonElement pegs_data, boolean is_read){
        // Clear the state of the required imports in the PegManager. This way we only import what is needed.
        GLSLCompiler.getInstance().clearPersistentData();

        // Define our substitutions to make to GLSL file
        HashMap<String, Object> substitutions = new HashMap<>();
        substitutions.put("shader_version", ShaderManager.getInstance().generateVersionString());

        // First thing we need to do besides mapping the current state of shader variables, is to compute our source code.
        substitutions.put("vertex_source", GLSLCompiler.getInstance().transpile(pegs_data));
        // Note this changes the state of PegManager.

        // Get the Agents accessor functions.
        String agent_ssbos = "";
        HashMap<String, SSBO> required_agents = GLSLCompiler.getInstance().getRequiredAgents();
        for(String agent_name : required_agents.keySet()){
            agent_ssbos += required_agents.get(agent_name).generateGLSL(is_read);
        }
        substitutions.put("agents_ssbos", agent_ssbos);

        // Uniforms
        String uniforms = "";
        HashSet<String> uniform_names = GLSLCompiler.getInstance().getRequiredUniforms();
        for(String uniform_name : uniform_names){
            if(ShaderManager.getInstance().hasUniform(uniform_name)) {
                uniforms += ShaderManager.getInstance().getUniform(uniform_name).toGLSL();
            }
        }
        substitutions.put("uniforms", uniforms);

        // Includes
        String includes = "";
        HashSet<String> required_includes = GLSLCompiler.getInstance().getRequiredIncludes();
        for(String requirement_name : required_includes){
            includes += String.format("#include %s\n", requirement_name);
        }
        substitutions.put("includes", includes);

        // Includes In Main
        String include_in_main = "";
        HashSet<String> required_to_include_in_main = GLSLCompiler.getInstance().getRequiredIncludesInMain();
        for(String requirement_name : required_to_include_in_main){
            include_in_main += String.format("#include %s\n", requirement_name);
        }
        substitutions.put("include_in_main", include_in_main);

        String source =
            "{{shader_version}}" +
            "layout (location = 0) in vec3 position;\n" +
            "{{uniforms}}" +
            "out vec3 pass_position;\n" +
            "flat out int pass_instance_id;\n" +
            "{{includes}}" +
            "{{agents_ssbos}}" +
            "void main() {\n" +
            "int instance_id = gl_InstanceID;\n" +
            "{{include_in_main}}" +
            "{{vertex_source}}"+
            "pass_position = gl_Position.xyz;\n" +
            "pass_instance_id = instance_id;\n" +
            "}\n";

        return ShaderManager.getInstance().compileShader(GL43.GL_VERTEX_SHADER, StringUtils.format(source, substitutions));
    }

    public int generateFragmentShaderFromPegs(JsonElement pegs_data, boolean is_read){
        // Clear the state of the required imports in the PegManager. This way we only import what is needed.
        GLSLCompiler.getInstance().clearPersistentData();

        // Define our substitutions to make to GLSL file
        HashMap<String, Object> substitutions = new HashMap<>();
        substitutions.put("shader_version", ShaderManager.getInstance().generateVersionString());

        // First thing we need to do besides mapping the current state of shader variables, is to compute our source code.
        substitutions.put("fragment_source", GLSLCompiler.getInstance().transpile(pegs_data));
        // Note this changes the state of PegManager.

        // Get the Agents accessor functions.
        String agent_ssbos = "";
        HashMap<String, SSBO> required_agents = GLSLCompiler.getInstance().getRequiredAgents();
        for(String agent_name : required_agents.keySet()){
            agent_ssbos += required_agents.get(agent_name).generateGLSL(!is_read);
        }
        substitutions.put("agents_ssbos", agent_ssbos);

        // Uniforms
        String uniforms = "";
        HashSet<String> uniform_names = GLSLCompiler.getInstance().getRequiredUniforms();
        for(String uniform_name : uniform_names){
            if(ShaderManager.getInstance().hasUniform(uniform_name)) {
                uniforms += ShaderManager.getInstance().getUniform(uniform_name).toGLSL();
            }
        }
        substitutions.put("uniforms", uniforms);

        // Includes
        String includes = "";
        HashSet<String> required_includes = GLSLCompiler.getInstance().getRequiredIncludes();
        for(String requirement_name : required_includes){
            includes += String.format("#include %s\n", requirement_name);
        }
        substitutions.put("includes", includes);

        // Includes In Main
        String include_in_main = "";
        HashSet<String> required_to_include_in_main = GLSLCompiler.getInstance().getRequiredIncludesInMain();
        for(String requirement_name : required_to_include_in_main){
            include_in_main += String.format("#include %s\n", requirement_name);
        }
        substitutions.put("include_in_main", include_in_main);

        String source =
            "{{shader_version}}" +
            "in vec3 pass_position;\n" +
            "flat in int pass_instance_id;\n" +
            "{{uniforms}}" +
            "uniform vec2 u_window_size;\n" +
            "uniform vec2 u_mouse_scroll;\n" +
            "out vec4 out_color; \n" +
            "{{includes}}" +
            "{{agents_ssbos}}" +
            "void main() {\n" +
            "vec2 screen_pos = (pass_position.xy / vec2(u_mouse_scroll.y) + vec2(1.0)) / vec2(2.0);\n" +
            "uint x_pos = uint(floor(screen_pos.x * u_window_size.x));\n" +
            "uint y_pos = uint(floor(screen_pos.y * u_window_size.y));\n" +
            "uint fragment_index = x_pos + (y_pos * uint(u_window_size.x));\n" +
            "uint instance = uint(fragment_index);\n" +
            "{{include_in_main}}" +
            "{{fragment_source}}"+
            "}\n";

        return ShaderManager.getInstance().compileShader(GL43.GL_FRAGMENT_SHADER, StringUtils.format(source, substitutions));
    }

    public long getAvailableGPUMemoryInBytes(){
        return gpu.getAvailableMemoryInBytes();
    }

    public int getDefaultVertexShader() {
        return DEFAULT_VERTEX_SHADER;
    }

    public int getDefaultGeometryShader() {
        return DEFAULT_GEOMETRY_SHADER;
    }

    public int getDefaultFragmentShader() {
        return DEFAULT_FRAGMENT_SHADER;
    }

    public void checkAllocated() {
        System.out.println("Shaders:" + allocated_shaders);
        System.out.println("Programs:" + allocated_programs);
    }

    public Collection<String> getRegisteredUniforms() {
        return this.uniforms.keySet();
    }
}
