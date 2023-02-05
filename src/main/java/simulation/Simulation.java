package simulation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import core.Apiary;
import graphics.*;
import input.Mouse;
import org.lwjgl.opengl.GL43;
import pegs.PegManager;
import simulation.world.World;
import simulation.world.DefaultWorld2D;
import util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Simulation {
    // This is a core part of a simulation. It represents how we initialize agents.
    private final int initialize_id;

    // These variables are used to control the speed at which the simulation runs.
    private float simulation_time = 0;
    private float simulation_updates_per_second = -1f;
    private float simulation_target_time = ( 1.0f / simulation_updates_per_second);


    private World simulation_world; // The simulation world is used as the template for the worldstate during simulation.
    // We have 2D and 3D worlds ready for simulations.

    private LinkedList<ComputeShader> steps = new LinkedList<ComputeShader>();

    private boolean initialized = false;

    // Variables used in simulation
    private int frame = 0;

    protected Simulation(JsonObject object){
        // First thing we do is set the active simulation to this one
        SimulationManager.getInstance().setActiveSimulation(this);

        // Load the agents
        JsonObject simulation_agents = object.getAsJsonObject("agents");

        String agent_ssbo_glsl = "";
        String initializer_glsl = "";
        for(String agent_name : simulation_agents.keySet()){
            JsonObject agent = simulation_agents.get(agent_name).getAsJsonObject();

            // Here we define what attributes are present on this agent.
            LinkedHashMap<String, GLDataType> agent_types = new LinkedHashMap<>();

            // This is where we will hold attributes for this agent.
            SSBO agent_ssbo = new SSBO(agent_name, agent_types);

            // Buffer for holding strings while we are generating the agent.
            LinkedList<String> attributes_initialization_glsl = new LinkedList<>();
            String attributes_copy_glsl = "";

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
                // In an attribute definition we also define the way that the data is initialized. We need to generate GLSL from these instructions and put that glsl in a compute shader.
                if(attribute_data.has("default_value")) {
                    String attribute_initializer_glsl = String.format("[\"%s\", \"%s\", \"%s\", \"%s\", \"@agent_write\"]", agent_name, "fragment_index", attribute_name, PegManager.getInstance().transpile(attribute_data.get("default_value").getAsJsonArray()));
                    attributes_initialization_glsl.push(attribute_initializer_glsl);
                    attributes_copy_glsl += String.format("%s_read.agent[fragment_index].%s = %s_write.agent[fragment_index].%s;\n", agent_name, attribute_name, agent_name, attribute_name);
                }
            }

            // If no instances are specified, we just allocate one agent per pixel.
            int agent_instances = Apiary.getWindowWidth() * Apiary.getWindowHeight();
            if(agent.has("instances")){
                agent_instances = agent.get("instances").getAsInt();
            }
            agent_ssbo.allocate(agent_instances);
            agent_ssbo.flush();

            // Add agent to sim.
            SimulationManager.getInstance().addAgent(agent_name, agent_ssbo);

            // Now that the agent is added, we can get the SSBO accessor code
            agent_ssbo_glsl += agent_ssbo.generateGLSL();

            // Now we need to compute the initialization data used for this agent.
            // This code will be inserted into an initialization shader.
            for(String attribute_initializer_glsl : attributes_initialization_glsl){
                // Populate the write buffer
                initializer_glsl += String.format("%s\n", PegManager.getInstance().transpile(attribute_initializer_glsl));
            }
            initializer_glsl += attributes_copy_glsl;
        }

        // We are going to need to construct a compute shader to initialize this agent's data.
        HashMap<String, Object> initialization_substitutions = new HashMap<>();
        initialization_substitutions.put("shader_version", ShaderManager.getInstance().generateVersionString());
        initialization_substitutions.put("agent_ssbo_glsl", agent_ssbo_glsl);
        initialization_substitutions.put("initializer_glsl", initializer_glsl);

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
                this.simulation_world = SimulationManager.getInstance().getWorldTemplate(template_identifier, simulation_world_template.get("arguments"));
                break resolve_world_template;
            } else {
                System.err.println(String.format("Error: Simulation:\"%s\" does not specify a world template. Using default of World2D."));
            }

            // At this point we have not resolved the world template so use the default world template of World2D
            this.simulation_world = new DefaultWorld2D(simulation_world_name);
        }

        // TODO send to compute shader.
        // Now we are able to determine the initialization GLSL
        String initialize_source = StringUtils.format(
    "{{shader_version}}" +
            //TODO make implicit for shader type.
            String.format("layout (local_size_x = 1, local_size_y = 1) in;\n") +
            //TODO make uniform generation implicit
            "uniform float u_time_seconds;\n" +
            "uniform vec2 u_window_size;\n" +
            "{{agent_ssbo_glsl}}" +
            // TODO make imports implicit
            "#include noise\n" +
            "void main() {\n" +
            "int x_pos = int(gl_GlobalInvocationID.x);\n" +
            "int y_pos = int(gl_GlobalInvocationID.y);\n" +
            "vec3  inputs = vec3( x_pos, y_pos, u_time_seconds ); // Spatial and temporal inputs\n" +
            "float rand   = random( inputs );              // Random per-pixel value\n" +
            "int fragment_index = x_pos + (y_pos * int(u_window_size.x));\n" +
            "{{initializer_glsl}}" +
            "}", initialization_substitutions
        );

        int initialize = ShaderManager.getInstance().compileShader(GL43.GL_COMPUTE_SHADER, initialize_source);
        this.initialize_id = ShaderManager.getInstance().linkShader(initialize);

        // Now we are going to determine the pipeline steps used
        JsonArray simulation_steps = object.getAsJsonArray("steps");
        for(int i = 0; i < simulation_steps.size(); i++){
            JsonObject step = simulation_steps.get(i).getAsJsonObject();
            if (step.has("logic")) {
                JsonArray pegs_input = step.getAsJsonArray("logic");
                steps.push(new ComputeShader(pegs_input));
            }
        }
    }

    public void update(double delta){

        // Initialize Data
        if(!initialized) {
            ShaderManager.getInstance().bind(initialize_id);
            Mouse.getInstance().bindUniforms();
            ShaderManager.getInstance().bindUniforms();
            GL43.glDispatchCompute(Apiary.getWindowWidth(), Apiary.getWindowHeight(), 1);
            GL43.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);
            initialized = true;
        }

        // Execute compute
        if(simulation_time >= simulation_target_time) {
            simulation_time -= simulation_target_time;

            for(ComputeShader compute_shader : steps){
                // Logic needs to be done with a set of two buffers and frame counting.
                // Instead of buffer copying we are just going to generate two separate shaders where the buffers locations are swapped.
                ShaderManager.getInstance().bind(compute_shader.getShaderID(frame));
                compute_shader.computeAndWait();
            }

            frame++;
            frame%=2;
        }
        simulation_time += delta;
    }

    public void render(){
        ShaderManager.getInstance().bind(simulation_world.getProgram());
        simulation_world.render();
    }

    public void cleanup(){
        // Free all allocated buffers and stuff
    }
}
