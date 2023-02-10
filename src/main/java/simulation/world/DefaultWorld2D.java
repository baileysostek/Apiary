package simulation.world;

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

    public DefaultWorld2D(String name) {
        super(name, new JsonObject(), GL43.GL_TRIANGLES);

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
        return ShaderManager.getInstance().getDefaultGeometryShader();
    }

    @Override
    public int generateFragmentShader(boolean is_odd) {
        return ShaderManager.getInstance().getDefaultFragmentShader();
    }

    @Override
    public void render() {
        // Clear the Screen
        GL43.glClearColor(1f, 1f, 1f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);

        // This stage renders an input image to the screen.
        this.vao.bind();
        GL43.glDrawArrays(this.getPrimitiveType(), 0, vertices.length / 3);
        this.vao.unbind();
        GL43.glUseProgram(0);
    }
}
