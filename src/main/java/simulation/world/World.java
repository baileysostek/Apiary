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

    private final int vertex_id;
    private final int geometry_id;
    private final int fragment_id;

    private final int program_id;

    public World(String name, JsonElement arguments, int primitive_type) {
        super(name);

        // Hold onto our input arguments so child classes can reference them.
        this.arguments = arguments.getAsJsonObject();

        // Set the primitive type
        this.primitive_type = primitive_type;

        // Generate shaders to be linked together.
        this.vertex_id = generateVertex();
        this.geometry_id = generateGeometryShader();
        this.fragment_id = generateFragmentShader();

        // Link our shaders together into a program.\
        if(geometry_id >= 0) {
            this.program_id = ShaderManager.getInstance().linkShader(vertex_id, geometry_id, fragment_id);
        }else{
            this.program_id = ShaderManager.getInstance().linkShader(vertex_id, fragment_id);
        }
    }

    protected abstract int generateVertex();
    protected abstract int generateGeometryShader();
    protected abstract int generateFragmentShader();

    public abstract void render();

    @Override
    public void onAddAttribute(String attribute_name, GLDataType attribute_type) {
        this.world_uniforms.put(attribute_name, ShaderManager.getInstance().createUniform(attribute_name, attribute_type));
    }

    public int getProgram() {
        return program_id;
    }

    public int getPrimitiveType() {
        return primitive_type;
    }
}
