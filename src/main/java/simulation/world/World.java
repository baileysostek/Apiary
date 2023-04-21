package simulation.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graphics.GLDataType;
import graphics.GLStruct;
import graphics.ShaderManager;
import graphics.Uniform;

import java.util.LinkedHashMap;

public abstract class World extends GLStruct {

    private LinkedHashMap<String, Uniform> world_uniforms = new LinkedHashMap<>();

    private final int primitive_type;

    protected final JsonObject arguments;

    // Double Buffering
    private final int program_id_primary; // Primary Buffer
    private final int program_id_secondary; // Secondary Buffer


    public World(String name, JsonElement arguments, int primitive_type) {
        super(name);

        // Hold onto our input arguments so child classes can reference them.
        this.arguments = arguments.getAsJsonObject();

        // Set the primitive type
        this.primitive_type = primitive_type;

        // Generate shaders to be linked together.
        int vertex_id = generateVertex(true);
//        this.vertex_id = generateVertex(false);
        int geometry_id = generateGeometryShader(true);
//        this.geometry_id = generateGeometryShader(false);
        int fragment_id_primary = generateFragmentShader(true);
        int fragment_id_secondary = generateFragmentShader(false);

        // Link our shaders together into a program.\
        if(geometry_id >= 0) {
            this.program_id_primary = ShaderManager.getInstance().linkShader(vertex_id, geometry_id, fragment_id_primary);
            this.program_id_secondary = ShaderManager.getInstance().linkShader(vertex_id, geometry_id, fragment_id_secondary);
        }else{
            this.program_id_primary = ShaderManager.getInstance().linkShader(vertex_id, fragment_id_primary);
            this.program_id_secondary = ShaderManager.getInstance().linkShader(vertex_id, fragment_id_secondary);
        }

        // Now we will delete all of our shaders.
        ShaderManager.getInstance().deleteShader(vertex_id);
        ShaderManager.getInstance().deleteShader(geometry_id);
        ShaderManager.getInstance().deleteShader(fragment_id_primary);
        ShaderManager.getInstance().deleteShader(fragment_id_secondary);
    }

    protected abstract int generateVertex(boolean is_read);
    protected abstract int generateGeometryShader(boolean is_read);
    protected abstract int generateFragmentShader(boolean is_read);

    public abstract void update(double delta);
    public abstract void render();

    @Override
    public void onAddAttribute(String attribute_name, GLDataType attribute_type) {
        this.world_uniforms.put(attribute_name, ShaderManager.getInstance().createUniform(attribute_name, attribute_type));
    }

    public int getProgram(int frame_number) {
        return (frame_number & 1) == 1 ? program_id_primary : program_id_secondary;
    }

    public int getPrimitiveType() {
        return primitive_type;
    }

    public void destroy() {
        ShaderManager.getInstance().deleteProgram(this.program_id_primary);
        ShaderManager.getInstance().deleteProgram(this.program_id_secondary);
    }
}
