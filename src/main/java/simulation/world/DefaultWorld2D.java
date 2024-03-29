package simulation.world;

import camera.ViewMode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graphics.ShaderManager;
import graphics.VAO;
import org.lwjgl.opengl.GL43;

public class DefaultWorld2D extends World{

    private final VAO vao;

    float vertices[] = {
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
        1.0f,  1.0f, 0.0f
    };

    public DefaultWorld2D(JsonObject world_initialization_data) {
        super(
                world_initialization_data.has("simulation_name") ? world_initialization_data.get("simulation_name").getAsString() : "Unnamed Simulation",
                world_initialization_data.has("arguments") ? world_initialization_data.get("arguments").getAsJsonObject() : world_initialization_data,
                GL43.GL_TRIANGLES
        );

        this.vao = new VAO();
        this.vao.bind();

        int vbo_vertex_id = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo_vertex_id);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, vertices, GL43.GL_STATIC_DRAW);
        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 0, 0);
        GL43.glEnableVertexAttribArray(0);
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, 0);
        this.vao.unbind();
    }

    @Override
    public int generateVertex(boolean is_odd) {
        return ShaderManager.getInstance().getDefaultVertexShader();
    }

    @Override
    public int generateGeometryShader(boolean is_odd) {
        return -1;
    }

    @Override
    public int generateFragmentShader(boolean is_odd) {
        if(!this.arguments.has("fragment_logic")){
            System.err.println("Error: no member \"fragment_logic\" exists in the arguments of this simulation's definition file.");
            return ShaderManager.getInstance().getDefaultFragmentShader();
        }
        return ShaderManager.getInstance().generateFragmentShaderFromPegs(this.arguments.get("fragment_logic"), is_odd);
    }

    @Override
    public void update(double delta) {
        ShaderManager.getInstance().setViewMode(ViewMode.VIEW_2D);
    }

    @Override
    public void render() {
        // Clear the Screen
        GL43.glClearColor(1f, 1f, 1f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);

        // This stage renders an input image to the screen.
        this.vao.bind();
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, vertices.length / 3);
        this.vao.unbind();
        GL43.glUseProgram(0);
    }
}
