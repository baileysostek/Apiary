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

    // Store if we have an override or not
    private boolean overrides_vertex_shader;
    private boolean overrides_geometry_shader;
    private boolean overrides_fragment_shader;

    public World(String name, JsonElement arguments, int primitive_type) {
        super(name);

        // Hold onto our input arguments so child classes can reference them.
        this.arguments = arguments.getAsJsonObject();

        // Set the primitive type
        this.primitive_type = primitive_type;

        // Generate shaders to be linked together.
        int vertex_id_primary = generateVertex(true);
        int vertex_id_secondary = generateVertex(false);
        this.overrides_vertex_shader = vertex_id_primary != ShaderManager.getInstance().getDefaultVertexShader();

        int geometry_id_primary = generateGeometryShader(true);
        int geometry_id_secondary = generateGeometryShader(false);
        this.overrides_geometry_shader = geometry_id_primary != ShaderManager.getInstance().getDefaultGeometryShader();

        int fragment_id_primary = generateFragmentShader(true);
        int fragment_id_secondary = generateFragmentShader(false);
        this.overrides_fragment_shader = fragment_id_primary != ShaderManager.getInstance().getDefaultFragmentShader();

        // Link our shaders together into a program.\
        if(geometry_id_primary >= 0) {
            this.program_id_primary = ShaderManager.getInstance().linkShader(vertex_id_primary, geometry_id_primary, fragment_id_primary);
            this.program_id_secondary = ShaderManager.getInstance().linkShader(vertex_id_secondary, geometry_id_secondary, fragment_id_secondary);
        }else{
            this.program_id_primary = ShaderManager.getInstance().linkShader(vertex_id_primary, fragment_id_primary);
            this.program_id_secondary = ShaderManager.getInstance().linkShader(vertex_id_secondary, fragment_id_secondary);
        }

        // Now we will delete all of our shaders.
        ShaderManager.getInstance().deleteShader(vertex_id_primary);
        ShaderManager.getInstance().deleteShader(vertex_id_secondary);
        ShaderManager.getInstance().deleteShader(geometry_id_primary);
        ShaderManager.getInstance().deleteShader(geometry_id_secondary);
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

    protected boolean hasCustomVertexShader() {
        return this.overrides_vertex_shader;
    }

    protected boolean hasCustomGeometryShader() {
        return this.overrides_geometry_shader;
    }

    protected boolean hasCustomFragmentShader() {
        return this.overrides_fragment_shader;
    }
}
