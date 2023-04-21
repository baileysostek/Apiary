package simulation.world;

import com.google.gson.JsonObject;
import graphics.ShaderManager;
import graphics.VAO;
import graphics.immediate.LineRenderer;
import org.lwjgl.opengl.GL43;

public class DefaultWorld3D extends World{

    private final VAO vao;

    float vertices[] = {
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
        1.0f,  1.0f, 0.0f
    };

    public DefaultWorld3D(JsonObject world_initialization_data) {
        super(
            world_initialization_data.has("simulation_name") ? world_initialization_data.get("simulation_name").getAsString() : "Unnamed Simulation",
            world_initialization_data.has("arguments") ? world_initialization_data.get("arguments").getAsJsonObject() : world_initialization_data,
            GL43.GL_POINTS
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
        if(!this.arguments.has("vertex_logic")){
            System.err.println("Error: no member \"vertex_logic\" exists in the arguments of this simulation's definition file.");
            return ShaderManager.getInstance().getDefaultVertexShader();
        }
        return ShaderManager.getInstance().generateVertexShaderFromPegs(this.arguments.get("vertex_logic"), is_odd);
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
        LineRenderer.getInstance().drawLine(0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1);
        LineRenderer.getInstance().drawLine((float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random(), 1, 1, 1, 1, 1, 1);
    }

    @Override
    public void render() {
        // Clear the Screen
        GL43.glClearColor(1f, 1f, 1f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);

        // This stage renders an input image to the screen.
        // Instead of rendering the screen we are going to render points based on something.
        this.vao.bind();
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, vertices.length / 3);
        this.vao.unbind();
        GL43.glUseProgram(0);
    }
}
