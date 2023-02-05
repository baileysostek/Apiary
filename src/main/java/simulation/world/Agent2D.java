package simulation.world;

import com.google.gson.JsonElement;
import graphics.ShaderManager;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL46;

import java.util.Arrays;
import java.util.HashMap;

public class Agent2D extends World{

    private HashMap<String, Integer> agent_instances = new HashMap<>();

    // VAO
    private int vao_id;

    public Agent2D(String name, JsonElement arguments) {
        super(name, arguments, GL43.GL_POINTS);

        agent_instances.put("boids", 2560 * 1440);

        vao_id = GL46.glGenVertexArrays();
        GL46.glBindVertexArray(vao_id);
        int vbo_vertex = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo_vertex);
        float[] vertices = new float[]{0.0f, 0.0f, 0.0f};
        Arrays.fill(vertices, 0.0f);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, vertices, GL46.GL_DYNAMIC_DRAW);
        GL46.glVertexAttribPointer(0, 3, GL46.GL_FLOAT, false, 0, 0);
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);
    }

    @Override
    public int generateVertex() {
        if(!this.arguments.has("vertex_logic")){
            System.err.println("Error: no member \"vertex_logic\" exists in the arguments of this simulation's definition file.");
            return ShaderManager.getInstance().getDefaultVertexShader();
        }
        return ShaderManager.getInstance().generateVertexShader(this.arguments.get("vertex_logic"));
    }

    @Override
    public int generateGeometryShader() {
        return -1;
    }

    @Override
    public int generateFragmentShader() {
        if(!this.arguments.has("fragment_logic")){
            System.err.println("Error: no member \"fragment_logic\" exists in the arguments of this simulation's definition file.");
            return ShaderManager.getInstance().getDefaultFragmentShader();
        }
        return ShaderManager.getInstance().generateFragmentShaderFromPegs(this.arguments.get("fragment_logic"));
    }

    @Override
    public void render() {
        GL43.glClearColor(0f, 0.2f, 0.4f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT | GL43.GL_DEPTH_BUFFER_BIT);

        // Set the Blend Mode
        GL46.glEnable(GL46.GL_BLEND);
        GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE_MINUS_SRC_ALPHA);

        // First we need to bind a VAO, this VAO contains almost nothing.
        GL46.glBindVertexArray(vao_id);
        // We need to enable an attribute array
        GL46.glEnableVertexAttribArray(0);
//        GL46.glVertexAttribDivisor(0, 0);

        // Custom Vertex Shader
        for(String agent_name : agent_instances.keySet()){
            int agent_instance = agent_instances.get(agent_name);
            GL43.glDrawArraysInstanced(GL43.GL_POINTS, 0, 1, agent_instance);
        }

        GL43.glUseProgram(0);
    }
}
