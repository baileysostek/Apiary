package simulation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import graphics.GLDataType;
import graphics.SSBO;
import graphics.ShaderManager;
import graphics.VAO;
import input.Mouse;
import org.lwjgl.opengl.GL43;
import pegs.PegManager;
import simulation.world.World;
import simulation.world.World2D;
import util.JsonUtils;

import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class Simulation {

    private final int program_id;
    private final VAO vao;

    float vertices[] = {
        -1.0f, -1.0f, 0.0f,
         1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
         1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
         1.0f,  1.0f, 0.0f
    };

//    float vertices[] = {
//        -0.5f, -0.5f, 0.0f,
//        0.5f, -0.5f, 0.0f,
//        0.0f,  0.5f, 0.0f
//    };




    private String name;
    private World simulation_world; // The simulation world is used as the tempalte for the worldstate during simulation.
    // We have 2D and 3D worlds ready for simulations.
    private LinkedHashMap<String, SSBO> agents = new LinkedHashMap<>();

    private LinkedHashSet<Step> pipeline = new LinkedHashSet<Step>();

    private float delta_over_time = 0;

    /**
     * We use constructor overloading
     * @param path
     */
    protected Simulation(String path) {
        this(JsonUtils.loadJson(path));
    }

    protected Simulation(JsonObject object){
        // First thing we do is set the active simulation to this one
        SimulationManager.getInstance().setActiveSimulation(this);

        // Determine what world template we are using
        JsonObject simulation_world_template = object.getAsJsonObject("world");
        resolve_world_template:{
            // Check for a name
            String simulation_world_name = "Default Simulation Name";
            if(simulation_world_template.has("name")){
                simulation_world_name = simulation_world_template.get("name").getAsString();
            }
            // Resolve the type
            if (simulation_world_template.has("type")) {
                String template_identifier = simulation_world_template.get("type").getAsString();
                //TODO: Better resolution of what template the user wants that just an IF chain here
                if(template_identifier.toLowerCase().equals("2d")){
                    this.simulation_world = new World2D(simulation_world_name);
                    break resolve_world_template;
                }

                // We didnt recognise the world type
                System.err.println(String.format("Error: Simulation:\"%s\" uses a world template that we did not recognise. Unrecognised template:\"%s\"", simulation_world_name, template_identifier));
            } else {
                System.err.println(String.format("Error: Simulation:\"%s\" does not specify a world template. Using default of World2D."));
            }

            // At this point we have not resolved the world template so use the default world template of World2D
            this.simulation_world = new World2D(simulation_world_name);
        }
        // Load the agents
        JsonObject simulation_agents = object.getAsJsonObject("agents");

        for(String agent_name : simulation_agents.keySet()){
            JsonObject agent = simulation_agents.get(agent_name).getAsJsonObject();

            // Here we define what attributes are present on this agent.
            LinkedHashMap<String, GLDataType> agent_types = new LinkedHashMap<>();

            // This is where we will hold attributes for this agent.
            SSBO agent_ssbo = new SSBO(agent_name, agent_types);

            JsonObject agent_attributes = agent.get("attributes").getAsJsonObject();
            for (String attribute_name : agent_attributes.keySet()) {
                JsonObject attribute_data = agent_attributes.get(attribute_name).getAsJsonObject();
                String attribute_type_name = attribute_data.get("type").getAsString();
                GLDataType attribute_type = ShaderManager.getInstance().getGLDataTypeFromString(attribute_type_name);
                if (attribute_type != null) {
                    agent_ssbo.addAttribute(attribute_name, attribute_type);
                } else {
                    System.err.println(String.format("Unrecognised GL uniform type[%s] for agent[%s] at attribute[%s].", attribute_type_name, agent_name, attribute_name));
                }
            }

            agent_ssbo.allocate(Integer.MAX_VALUE);
            agent_ssbo.flush();

            // Add agent to sim.
            agents.put(agent_name, agent_ssbo);
        }
        // Now we are going to determine the pipeline steps used
        JsonArray simulation_steps = object.getAsJsonArray("steps");
        for(int i = 0; i < simulation_steps.size(); i++){
            JsonObject step = simulation_steps.get(i).getAsJsonObject();
            // Process data about each step.
//            if(JsonUtils.validate(step, JsonUtils.getInstance().STEP_SCHEMA)){
                JsonArray pegs_input = step.getAsJsonArray("logic");
                String glsl = PegManager.getInstance().transpile(pegs_input);
                System.out.println(glsl);
//            }
            // Schema validate the step
        }

        // Shader

        String vertex_source =
                String.format("#version %s\n", ShaderManager.getInstance().getGLTargetVersion()) +
                "layout (location = 0) in vec3 position;\n" +
                "out vec3 pass_position;\n" +
                "void main(void){\n" +
                "pass_position = position;\n" +
                "gl_Position = vec4(position, 1.0);\n" +
                "}\n";

        String fragment_source =
                String.format("#version %s\n", ShaderManager.getInstance().getGLTargetVersion()) +
                agents.get("cell").generateGLSL() +
                "in vec3 pass_position;\n" +
                "uniform vec2 u_window_size;\n" +
                "uniform vec2 u_mouse_pos_pixels;\n" +
                "uniform float u_time_seconds;\n" +
                "out vec4 out_color; \n" +
                "void main(void){\n" +
                "vec2 screen_pos = (pass_position.xy + vec2(1)) / vec2(2);\n" +
                "int fragment_index = int(floor((screen_pos.x * u_window_size.x) + (u_window_size.x * (screen_pos.y * u_window_size.y))));\n" +
                "if (all_cell.agent[fragment_index].alive > 0.5) {\n" +
                "    out_color = vec4(all_cell.agent[fragment_index].color , 1.0);\n" +
                "} else {\n" +
                "    out_color = vec4(0.0 , 0.0 , 0.0 , 1.0);\n" +
                "}\n" +
                "}\n";

//        String fragment_source = String.format("#version %s\n", ShaderManager.getInstance().getGLTargetVersion()) +
//            "in vec3 pass_position;\n" +
//            "uniform vec2 u_window_size;\n" +
//            "uniform vec2 u_mouse_pos_pixels;\n" +
//            "uniform float u_time_seconds;\n" +
//            "out vec4 out_color; \n" +
//            "void main(void){\n" +
//            "    out_color = vec4(0.0 , 0.0 , 0.0 , 1.0);\n" +
//            "}\n";

        int vertex   = ShaderManager.getInstance().compileShader(GL43.GL_VERTEX_SHADER, vertex_source);
        int fragment = ShaderManager.getInstance().compileShader(GL43.GL_FRAGMENT_SHADER, fragment_source);

        this.program_id = ShaderManager.getInstance().linkShader(vertex, fragment);

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

    public void update(double delta){
        //TODO move to shader manager.
        // If we have a program loaded
        if(program_id > -1){
            ShaderManager.getInstance().bind(program_id);
        }
        // Bind all of our uniform variable
        Mouse.getInstance().bindUniforms();
    }

    public void render(){
        // Now we try to step the simulation
//        for(Step step : pipeline){
//            for(String agent_name : step.getAgents()){
//
//            }
//        }

        GL43.glClearColor(1f, 1f, 1f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);

//        this.agents.get("cell").flush();

        // This stage renders an input image to the screen.
        this.vao.bind();
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, vertices.length / 3);
        this.vao.unbind();
        GL43.glUseProgram(0);

    }

    public void cleanup(){
        // Free all allocated buffers and stuff
    }

    public boolean hasAgent(String agent_type) {
        return this.agents.containsKey(agent_type);
    }

    //TODO: FIX
    public int getAllocatedCapacity(String agent_type){
//        if(hasAgent(agent_type)){
//            return this.agents.get(agent_type).getCapacity();
//        }
        // We dont have an agent of this type. So our allocated capacity is 0
        return Integer.MAX_VALUE;
    }
}
