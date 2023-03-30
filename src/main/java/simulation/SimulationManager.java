package simulation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graphics.*;
import graphics.texture.TextureManager;
import input.Keyboard;
import org.lwjgl.glfw.GLFW;
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
    private Uniform u_time_delta = ShaderManager.getInstance().createUniform("u_time_delta", GLDataType.FLOAT);
    private Uniform u_simulation_width = ShaderManager.getInstance().createUniform("u_simulation_width", GLDataType.INT);
    private Uniform u_simulation_height = ShaderManager.getInstance().createUniform("u_simulation_height", GLDataType.INT);


    // We are going to buffer the unload request such that it does not try to unload a simulation that is being rendered.
    private boolean unload_simulation = false;
    private JsonObject simulation_to_load = null;

    // Store our output in an FBO
    private FBO fbo;

    private SimulationManager() {

    }

    public void load(String path){
        JsonObject simulation_object = JsonUtils.loadJson(path);
        load(simulation_object);
    }

    public void load(JsonObject simulation_object){
        if (!JsonUtils.validate(simulation_object, JsonUtils.getInstance().SIMULATION_SCHEMA)) {
            System.err.println(String.format("Error: tried to load %s, however that file is not a valid Simulation."));
            return; // Not the correct format. TODO: print error.
        }
        unloadSimulation();
        simulation_to_load = simulation_object;
    }

    public void unloadSimulation(){
        this.unload_simulation = true;
    }

    public void update(double delta){
        // Check for unload request
        if(unload_simulation){
            // Unload
            if(simulation != null){
                // Cleanup the old simulation.
                simulation.cleanup();
                ShaderManager.getInstance().checkAllocated();
                simulation = null;
            }
            // Reset our variables
            unload_simulation = false;

            // Load
            if(simulation_to_load != null) {
                simulation = new Simulation(simulation_to_load);
                onLoad(simulation);
            }
            simulation_to_load = null;
        }

        // Update our uniforms
        u_time_seconds.set((float) (u_time_seconds.get()[0] + delta));
        u_time_delta.set((float) delta);
        u_simulation_width.set(hasActiveSimulation() ? simulation.getWorldWidth() : 0);
        u_simulation_height.set(hasActiveSimulation() ? simulation.getWorldHeight() : 0);
        // If we have a simulation update the simulation
        if(hasActiveSimulation()){
            simulation.update(delta);
        }
    }

    private void onLoad(Simulation simulation){
        fbo = new FBO(simulation.getWorldWidth(), simulation.getWorldHeight());
        fbo.unbindFrameBuffer();

        // SCREENSHOT
        Keyboard.getInstance().addPressCallback(GLFW.GLFW_KEY_INSERT, () -> {
            if(hasActiveSimulation()){
                System.out.println("Start");
                TextureManager.getInstance().saveTextureToFile(fbo.getTextureID(), String.format("screenshot_%s.png", (System.currentTimeMillis()+"")));
            }
        });
    }

    public void render(){
        if(hasActiveSimulation()) {
            fbo.bindFrameBuffer();

            GL43.glEnable(GL43.GL_DEPTH_TEST);
            GL43.glClear(GL43.GL_DEPTH_BUFFER_BIT | GL43.GL_COLOR_BUFFER_BIT);

            ShaderManager.getInstance().bindUniforms();
            simulation.render();

            fbo.unbindFrameBuffer();
        }

        GL43.glEnable(GL43.GL_DEPTH_TEST);
        GL43.glClear(GL43.GL_DEPTH_BUFFER_BIT | GL43.GL_COLOR_BUFFER_BIT);
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

    public Simulation getActiveSimulation() {
        return this.simulation;
    }

    public Uniform getFrameDelta(){
        return u_time_seconds;
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

    public int getSimulationTexture(){
        return (fbo != null) ? fbo.getTextureID() : 0;
    }
}
