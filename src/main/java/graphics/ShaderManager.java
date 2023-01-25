package graphics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lwjgl.opengl.GL46;
import simulation.world.World;
import util.StringUtils;

import java.util.HashMap;

public class ShaderManager {

    // Singleton Instance
    private static ShaderManager singleton;
    // Singleton variables
    private HashMap<String, Shader> shaders = new HashMap<>();

    public final byte ALIGNMENT = 4;
    public final String GLSL_VERSION;

    private int active_shader = -1;

    private HashMap<String, GLDataType> type_mapping = new HashMap<>();

    private ShaderManager() {
        // Define our typeMapping
        // By default we just use the name of the enum values
        for(GLDataType type : GLDataType.values()){
            associateNameWithType(type.name(), type);
        }
        // Here we can explicitly add more name associations to make the user experience easier.

        // Ask what the GLSL version is and set it
        String version = GL46.glGetString(GL46.GL_SHADING_LANGUAGE_VERSION);
        this.GLSL_VERSION = version.replace(".", "").substring(0,3);
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

    public int compileShader(int shader_type, String shader_source){
        String shader_type_name = "unknown";
        switch (shader_type){
            case GL46.GL_VERTEX_SHADER:{
                shader_type_name = "Vertex";
                break;
            }
            case GL46.GL_FRAGMENT_SHADER:{
                shader_type_name = "Fragment";
                break;
            }
            case GL46.GL_COMPUTE_SHADER:{
                shader_type_name = "Compute";
                break;
            }
        }
        // Allocate memory for the new shader program
        int shader_id   = GL46.glCreateShader(shader_type);
        GL46.glShaderSource(shader_id, shader_source);
        GL46.glCompileShader(shader_id);
        checkForError(String.format("Compiling %s Shader", shader_type_name));

        //Buffer for reading compile status
        int[] compile_buffer = new int[]{ 0 };

        GL46.glGetShaderiv(shader_id, GL46.GL_COMPILE_STATUS, compile_buffer);
        if (compile_buffer[0] == GL46.GL_FALSE) { // If our shader did not compile
            GL46.glGetShaderiv(shader_id, GL46.GL_INFO_LOG_LENGTH, compile_buffer);
            //Check that log exists
            if (compile_buffer[0] > 0) {
                String errorMesssage = GL46.glGetShaderInfoLog(shader_id);
                String line_number = "unknown";
                try {
                    line_number = errorMesssage.substring(errorMesssage.indexOf("(") + 1, errorMesssage.indexOf(")"));
                } catch (Exception e){
                    e.printStackTrace();
                }
                System.err.println(String.format("Error compiling %s shader| %s | %s", shader_type_name, line_number, GL46.glGetShaderInfoLog(shader_id)));
                //Cleanup our broken shader
                GL46.glDeleteShader(shader_id);

                System.exit(0);

                return -1;
            }
        }else{
            if(compile_buffer[0] == GL46.GL_TRUE){
                checkForError("Compilation Error");
                System.out.println("Shader compiled Successfully.");
            }else{
                System.out.println("Shader compiled in an unknown state. This may cause strange behavior.");
            }
        }

        return shader_id;
    }

    public int linkShader(int vertex, int fragment){

        //Buffer for reading compile status
        int[] compile_buffer = new int[]{ 0 };

        //Now that we have our shaders compiled, we link them to a shader program.
        int program_id = GL46.glCreateProgram();

        System.out.println("Linking Vertex and Fragment shaders to Program...");

        int attributeIndex = 1;
        for(String attribute : new String[]{"position"}){
            GL46.glBindAttribLocation(program_id, attributeIndex, attribute);
            attributeIndex++;
        }

        //Combine vertex and fragment shaders into one program
        GL46.glAttachShader(program_id, vertex);
        GL46.glAttachShader(program_id, fragment);

        checkForError("Attach Shaders");

        //Link
        GL46.glLinkProgram(program_id);
        checkForError("Linking Shaders");

        //Check that the link status was successful.
        GL46.glGetProgramiv(program_id, GL46.GL_LINK_STATUS, compile_buffer);
        if (compile_buffer[0] == GL46.GL_TRUE) {
            System.out.println("Successfully linked shaders into program.");
        }else{
            String error_message = GL46.glGetProgramInfoLog(program_id);
            System.err.println(String.format("Error linking shaders into program | %s ", error_message));
            //Cleanup our broken program
            GL46.glDeleteProgram(program_id);
            System.exit(1);

            return -1;
        }

        return program_id;
    }

    public void checkForError(String error_cause){
        int error_check = GL46.glGetError();
        while (error_check != GL46.GL_NO_ERROR) {
            System.out.println(String.format("Error[%s]: %s", error_cause, error_check));
            error_check = GL46.glGetError();
        }
    }
}
