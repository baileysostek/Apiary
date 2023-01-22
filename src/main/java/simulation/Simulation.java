package simulation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graphics.GLDataType;
import graphics.SSBO;
import graphics.ShaderManager;
import org.lwjgl.opengl.GL46;
import simulation.world.World;
import simulation.world.World2D;
import util.JsonUtils;
import util.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class Simulation {

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

            agent_ssbo.allocate(100);
            agent_ssbo.flush();
            agent_ssbo.computeSafeSizeInFloats();

            // Add agent to sim.
            agents.put(agent_name, agent_ssbo);
        }

        // Now we are going to determine the pipeline steps used
    }

    public void update(double delta){

    }

    public void render(){
        // Now we try to step the simulation
        for(Step step : pipeline){
            for(String agent_name : step.getAgents()){

            }
        }


    }

    public void cleanup(){
        // Free all allocated buffers and stuff
    }
}
