package graphics;

import core.Apiary;
import org.lwjgl.opengl.GL43;
import simulation.world.World;
import util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class ShaderManager {

    // Singleton Instance
    private static ShaderManager singleton;

    public final byte ALIGNMENT = 4;
    private final String GLSL_VERSION;

    public static final int GL_MAJOR_VERSION = 4;
    public static final int GL_MINOR_VERSION = 3;

    private int bound_shader = -1;

    // Indies used for SSBO location binding
    private static int next_available_location = 0;

    // GLSL libraries
    private LinkedHashSet<String> custom_directives = new LinkedHashSet<>();
    private LinkedHashMap<String, String[]> glsl_library_files = new LinkedHashMap<>();

    // These are some uniforms that exist globally in every simulation and project
    private HashMap<String, Uniform> uniforms = new HashMap<>();

    //TODO move to ScreenManager or WindowManager class.
    private final Uniform u_window_size;
    private final Uniform u_aspect_ratio;

    private HashMap<String, GLDataType> type_mapping = new HashMap<>();

    // Quad the size of the screen.
    float vertices[] = {
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
        1.0f,  1.0f, 0.0f
    };

    private ShaderManager() {
        // Register any custom directives we want to support
        custom_directives.add("#include");

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
    }

    // Singleton initializer and getter
    public static void initialize() {
        if (singleton == null) {
            singleton = new ShaderManager();
        }
    }

    public static ShaderManager getInstance() {
        return singleton;
    }

    public int loadShader(String name){
        return -1;
    }

    public String getGLTargetVersion() {
        return this.GLSL_VERSION;
    }

    public void loadUniformsFromWorld(World world){
        HashMap<String, GLDataType> attributes = world.getAttributes();
        for(String attribute_name : attributes.keySet()){

        }
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
        }

        // Allocate memory for the new shader program
        int shader_id   = GL43.glCreateShader(shader_type);
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
                    line_number = errorMesssage.substring(errorMesssage.indexOf("(") + 1, errorMesssage.indexOf(")"));
                } catch (Exception e){
                    e.printStackTrace();
                }
                System.err.println(String.format("Error compiling %s shader| %s | %s", shader_type_name, line_number, GL43.glGetShaderInfoLog(shader_id)));
                System.err.println(precompiled_shader_source.split("\n")[Integer.parseInt(line_number) - 1]);
                //Cleanup our broken shader
                GL43.glDeleteShader(shader_id);

                System.exit(1);

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

    public void checkForError(String error_cause){
//        System.out.println("check - 1");
//        int error_check = GL43.glGetError();
//        System.out.println("check - 2");
//        while (error_check != GL43.GL_NO_ERROR) {
//            System.out.println("check - 3");
//            System.out.println(String.format("Error[%s]: %s", error_cause, error_check));
//            error_check = GL43.glGetError();
//        }
    }

    // Uniform stuff
    public Uniform createUniform(String name, GLDataType type){
        Uniform uniform = new Uniform(name, type);
        this.uniforms.put(name, uniform);
        return uniform;
    }

    public void update(double delta){
        this.u_window_size.set(Apiary.getWindowWidth(), Apiary.getWindowHeight());
        this.u_aspect_ratio.set(Apiary.getAspectRatio());
    }

    public String getGLSLVersion(){
        return GLSL_VERSION+"";
    }

    public void bind(int program_id) {
        this.bound_shader = program_id;
        GL43.glUseProgram(this.bound_shader);
    }

    public int getBoundShader(){
        return this.bound_shader;
    }

    public void bindUniforms() {
        for(Uniform uniform : this.uniforms.values()){
            uniform.bind();
        }
    }

    public int getNextAvailableSSBOLocation() {
        return next_available_location++;
    }
}
