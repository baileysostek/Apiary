package simulation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import core.Apiary;
import graphics.*;
import input.Keyboard;
import compiler.GLSLCompiler;
import org.lwjgl.glfw.GLFW;
import simulation.world.World;
import simulation.world.DefaultWorld2D;
import util.MathUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Simulation {
    // This is a core part of a simulation. It represents how we initialize agents.
    private final LinkedList<AgentInitializationShader> agent_initialization_shaders = new LinkedList<>();

    // These variables are used to control the speed at which the simulation runs.
    private float simulation_time = 0;
    private float simulation_updates_per_second = -1f;
    private float simulation_target_time = ( 1.0f / simulation_updates_per_second);

    //TODO: Probably refactor this because it is not needed
    private World simulation_world; // The simulation world is used as the template for the worldstate during simulation.
    // We have 2D and 3D worlds ready for simulations.

    private LinkedList<ComputeShader> steps = new LinkedList<ComputeShader>();

    private int world_width;
    private int world_height;

    // Variables used in simulation
    private int frame = 0;

    private HashSet<SSBO> agent_ssbos = new HashSet<>();

    protected Simulation(JsonObject object){
        // First thing we do is set the active simulation to this one
        SimulationManager.getInstance().setActiveSimulation(this);

        // Get reference to our world template
        JsonObject simulation_world_template = object.getAsJsonObject("world");
        world_width  = Apiary.getWindowWidth();
        world_height = Apiary.getWindowHeight();

        if (simulation_world_template.has("width")) {
            world_width = simulation_world_template.get("width").getAsInt();
        }

        if (simulation_world_template.has("height")) {
            world_height = simulation_world_template.get("height").getAsInt();
        }

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
            agent_ssbos.add(agent_ssbo);

            // Buffer for holding strings while we are generating the agent.
            String attribute_initializer_setup_data = "";
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
                    // TODO: the transpialtion step on the line below can produce multiple lines, rather than a string substitution inject the strings onto the end of the default_value array.
                    // This will have the effect of just adding on the setter in the correct place and let any additional logic happen as well.
                    String[] default_value = GLSLCompiler.getInstance().transpile(attribute_data.get("default_value")).split("\n");
                    for(int i = 0; i < default_value.length - 1; i++){
                        attribute_initializer_setup_data += default_value[i];
                    }
                    String attribute_initializer_glsl = String.format("[\"%s\", \"%s\", \"%s\", \"%s\", \"@agent_write\"]", agent_name, "fragment_index", attribute_name, default_value[default_value.length - 1]);
                    attributes_initialization_glsl.push(attribute_initializer_glsl);
                    attributes_copy_glsl += String.format("%s_read.agent[fragment_index].%s = %s_write.agent[fragment_index].%s;\n", agent_name, attribute_name, agent_name, attribute_name);
                }
            }

            // If no instances are specified, we just allocate one agent per pixel.
            int agent_instances = world_width * world_height;
            int agent_width = world_width;
            int agent_height = world_height;
            if(agent.has("instances")){
                agent_instances = agent.get("instances").getAsInt();
                int[] factors = MathUtil.factor(agent_instances);
                agent_width  = factors[(factors.length / 2) - 1];
                agent_height = factors[factors.length / 2];
            }
            agent_ssbo.allocate(agent_instances);
            agent_ssbo.flush();

            // Add agent to sim.
            SimulationManager.getInstance().addAgent(agent_name, agent_ssbo);

            // Generate the initialization data for this agent
            AgentInitializationShader agent_initializer = new AgentInitializationShader(agent_name, agent_width, agent_height, agent_attributes);
            this.agent_initialization_shaders.addLast(agent_initializer);

            // Now that the agent is added, we can get the SSBO accessor code
            agent_ssbo_glsl += agent_ssbo.generateGLSL();

            initializer_glsl += attribute_initializer_setup_data;
            // Now we need to compute the initialization data used for this agent.
            // This code will be inserted into an initialization shader.
            for(String attribute_initializer_glsl : attributes_initialization_glsl){
                // Populate the write buffer
                initializer_glsl += String.format("%s\n", GLSLCompiler.getInstance().transpile(attribute_initializer_glsl));
            }
            initializer_glsl += attributes_copy_glsl;
        }

        // Determine what world template we are using
        this.simulation_world = SimulationManager.getInstance().getWorldTemplate(simulation_world_template);

        // Now we are going to determine the pipeline steps used
        JsonArray simulation_steps = object.getAsJsonArray("steps");
        for(int i = 0; i < simulation_steps.size(); i++){
            JsonObject step = simulation_steps.get(i).getAsJsonObject();
            int iteration_width  = world_width;
            int iteration_height = world_height;
            int iteration_count= 0;
            if (step.has("for_each")) {
                // Try to get this value as a number
                try{
                    int possible_iteration_count = step.get("for_each").getAsInt();
                    iteration_count = possible_iteration_count;
                }catch(NumberFormatException e){
                    String possible_agent_reference = step.get("for_each").getAsString();
                    // Check if the passed string is an agent
                    if(SimulationManager.getInstance().hasAgent(possible_agent_reference)){
                        // get the agent count
                        iteration_count = SimulationManager.getInstance().getAgent(possible_agent_reference).getCapacity();

                        // Now we need to figure out the grid size to represent this, we will pick them middlemost factor
                        int[] factors = MathUtil.factor(iteration_count);

                        iteration_width  = factors[(factors.length / 2) - 1];
                        iteration_height = factors[factors.length / 2];
                    }
                }
            }
            if (step.has("logic")) {
                JsonArray pegs_input = step.getAsJsonArray("logic");
                steps.addLast(new ComputeShader(pegs_input, iteration_width, iteration_height));
            }
        }

        // Now we will initialize the world state
        for(AgentInitializationShader initializer : agent_initialization_shaders){
            initializer.initialize();
        }

        Keyboard.getInstance().addPressCallback(GLFW.GLFW_KEY_UP, ()->{
            simulation_updates_per_second++;
            simulation_target_time = ( 1.0f / simulation_updates_per_second);
            simulation_time = 0;
        });
        Keyboard.getInstance().addPressCallback(GLFW.GLFW_KEY_DOWN, ()->{
            simulation_updates_per_second--;
            simulation_target_time = ( 1.0f / simulation_updates_per_second);
            simulation_time = 0;
        });
    }

    public int getWorldWidth() {
        return world_width;
    }

    public int getWorldHeight() {
        return world_height;
    }

    public void update(double delta){
        simulation_world.update(delta);
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
        ShaderManager.getInstance().bind(simulation_world.getProgram(frame));
        simulation_world.render();
    }

    public void cleanup(){
        // Destroy our initialization shaders
        for(AgentInitializationShader initialization_shader : agent_initialization_shaders){
            initialization_shader.destroy();
        }
        agent_initialization_shaders.clear();

        // Destroy our Steps
        for(ComputeShader step : steps){
            step.destroy();
        }
        steps.clear();

        // We also need to free our SSBOs
        for(SSBO agent_ssbo : agent_ssbos){
            agent_ssbo.cleanup();
        }
        agent_ssbos.clear();

        // Destroy our world
        this.simulation_world.destroy();

        System.out.println("Shaders:" + ShaderManager.getInstance());
    }

    public HashMap<String, Integer> getAgentCounts(){
        HashMap<String, Integer> agent_instances = new HashMap<>();
        for(SSBO ssbo : agent_ssbos){
            agent_instances.put(ssbo.getName(), ssbo.getCapacity());
        }
        return agent_instances;
    }
}
