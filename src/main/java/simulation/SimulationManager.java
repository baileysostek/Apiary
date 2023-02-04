package simulation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graphics.GLDataType;
import graphics.SSBO;
import graphics.ShaderManager;
import graphics.Uniform;
import org.lwjgl.opengl.GL43;
import simulation.world.AgentGrid2D;
import simulation.world.World;
import simulation.world.DefaultWorld2D;
import util.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Locale;

public class SimulationManager {

    // Singleton Instance
    private static SimulationManager singleton;

    // If we have a simulation loaded
    private Simulation simulation = null;

    // All agents currently defined
    private LinkedHashMap<String, SSBO> agents = new LinkedHashMap<>();

    // Here are all uniforms that every simulation has access too. Simulation Specific uniforms can be registered as well.
    private Uniform u_time_seconds = ShaderManager.getInstance().createUniform("u_time_seconds", GLDataType.FLOAT);

    private SimulationManager() {}

    public void load(String path){
        cleanup();

        // Ensure that the loaded simulation file abides by our simulation schema.
        JsonObject object = JsonUtils.loadJson(path);
        if(!JsonUtils.validate(object, JsonUtils.getInstance().SIMULATION_SCHEMA)){
            return; // Not the correct format. TODO: print error.
        }

        // The object is valid
        simulation = new Simulation(object);

        // Here we will parse the agent definitions and generate our SSBOs


        System.out.println(simulation);

    }

    public void update(double delta){
        // Update our uniforms
        u_time_seconds.set((float) (u_time_seconds.get()[0] + delta));
        // If we have a simulation update the simulation
        if(hasActiveSimulation()){
            simulation.update(delta);
        }
    }

    public void render(){

        GL43.glEnable(GL43.GL_DEPTH_TEST);
        GL43.glClear(GL43.GL_DEPTH_BUFFER_BIT | GL43.GL_COLOR_BUFFER_BIT);

        if(hasActiveSimulation()){
            // Right before we render we are going to bind all of our uniforms
            ShaderManager.getInstance().bindUniforms();
            simulation.render();
        }
    }


    // Singleton initializer and getter
    public static void initialize() {
        if (singleton == null) {
            singleton = new SimulationManager();
        }
    }

    public static SimulationManager getInstance() {
        return singleton;
    }

    // Getters and setters
    protected void setActiveSimulation(Simulation simulation){
        this.simulation = simulation;
    }

    public boolean hasActiveSimulation(){
        return simulation != null;
    }

    public void cleanup(){
        if(simulation != null){
            simulation.cleanup();
        }
    }

    public Simulation getActiveSimulation() {
        return this.simulation;
    }

    public String getTimeUniformName(){
        return u_time_seconds.getName();
    }

    public void addAgent(String agent_name, SSBO agent){
        this.agents.put(agent_name, agent);
    }

    public boolean hasAgent(String agent_name){
        return this.agents.containsKey(agent_name);
    }

    public SSBO getAgent(String agent_name){
        return this.agents.get(agent_name);
    }

    public World getWorldTemplate(String template_name, JsonElement arguments){
        if(template_name.toLowerCase(Locale.ROOT).equals("agentgrid2d")){
            return new AgentGrid2D(template_name, arguments);
        }
        // We didnt recognise the world type
        System.err.println(String.format("Error: Simulation:\"%s\" uses a world template that we did not recognise. Unrecognised template:\"%s\"", template_name, template_name));
        // Default
        return new DefaultWorld2D(template_name);
    }
}
