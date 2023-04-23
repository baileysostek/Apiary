package simulation.world;

import camera.ViewMode;
import com.google.gson.JsonObject;
import graphics.ShaderManager;
import graphics.VAO;
import graphics.immediate.LineRenderer;
import org.lwjgl.opengl.GL43;

public class DefaultWorld3D extends World{

    // If no Vertex Shader override has been provided render these points in 3D space but on a 2D quad.
    private final VAO vao_2D;
    float[] vertices_2D = {
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
        1.0f,  1.0f, 0.0f
    };

    // Set of 3D Vertices to use to render actual 3D data.
    private final VAO vao_3D;
    float[] vertices_3D = {0.0f, 0.0f, 0.0f};

    private int instances = 0;

    public DefaultWorld3D(JsonObject world_initialization_data) {
        super(
            world_initialization_data.has("simulation_name") ? world_initialization_data.get("simulation_name").getAsString() : "Unnamed Simulation",
            world_initialization_data.has("arguments") ? world_initialization_data.get("arguments").getAsJsonObject() : world_initialization_data,
            GL43.GL_POINTS
        );


        this.vao_2D = new VAO();
        this.vao_2D.bind();

        int vbo_vertex_id = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo_vertex_id);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, vertices_2D, GL43.GL_STATIC_DRAW);
        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 0, 0);
        GL43.glEnableVertexAttribArray(0);
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, 0);
        this.vao_2D.unbind();

        this.vao_3D = new VAO();
        this.vao_3D.bind();

        int vbo_vertex_id_3D = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo_vertex_id_3D);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, vertices_3D, GL43.GL_STATIC_DRAW);
        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 0, 0);
        GL43.glEnableVertexAttribArray(0);
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, 0);
        this.vao_3D.unbind();

        // Check if we have an instances param passed in arguments
        if (this.arguments.has("instance_count")) {
            this.instances = this.arguments.get("instance_count").getAsInt();
        }
    }

    @Override
    public int generateVertex(boolean is_odd) {
        if(!this.arguments.has("vertex_logic")){
            System.err.println("Error: no member \"vertex_logic\" exists in the arguments of this simulation's definition file.");
            /**
             * The default Vertex Shader in the 3D configuration will render a spinning cube on the screen with the simulation running in a plane inside of it.
             */
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

        ShaderManager.getInstance().setViewMode(ViewMode.VIEW_3D);

        LineRenderer.getInstance().drawLine(-1, -1, -1, 1, -1, -1);
        LineRenderer.getInstance().drawLine( 1, -1, -1, 1,  1, -1);
        LineRenderer.getInstance().drawLine( 1,  1, -1,-1,  1, -1);
        LineRenderer.getInstance().drawLine(-1,  1, -1,-1, -1, -1);

        LineRenderer.getInstance().drawLine(-1, -1,  1, 1, -1,  1);
        LineRenderer.getInstance().drawLine( 1, -1,  1, 1,  1,  1);
        LineRenderer.getInstance().drawLine( 1,  1,  1,-1,  1,  1);
        LineRenderer.getInstance().drawLine(-1,  1,  1,-1, -1,  1);

        LineRenderer.getInstance().drawLine(-1, -1,  -1,-1, -1,  1);
        LineRenderer.getInstance().drawLine( 1, -1,  -1, 1, -1,  1);
        LineRenderer.getInstance().drawLine( 1,  1,  -1, 1,  1,  1);
        LineRenderer.getInstance().drawLine(-1,  1,  -1,-1,  1,  1);

    }

    @Override
    public void render() {
        // Clear the Screen
        GL43.glClearColor(0f, 0f, 0f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);

        if(super.hasCustomVertexShader()) {
            // Real 3D
            GL43.glPointSize(3);
            this.vao_3D.bind();
            GL43.glDrawArraysInstanced(GL43.GL_POINTS, 0, vertices_3D.length / 3, this.instances);
            this.vao_3D.unbind();
        }else{
            // This stage renders an input image to the screen.
            // Instead of rendering the screen we are going to render points based on something.
            // 2D Plane in 3D
            this.vao_2D.bind();
            GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, vertices_2D.length / 3);
            this.vao_2D.unbind();
        }

        GL43.glUseProgram(0);
    }
}
