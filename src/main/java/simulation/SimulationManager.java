package simulation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graphics.*;
import graphics.immediate.LineRenderer;
import graphics.texture.TextureManager;
import input.Keyboard;
import nodegraph.Node;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL43;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import simulation.world.World;
import simulation.world.DefaultWorld2D;
import util.JsonUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

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

    // World Templates
    private HashSet<Class<? extends World>> templates = new HashSet<>();

    private SimulationManager() {
        // Look for all world template classes
        String SIMULATION_WORLD_PACKAGE = "simulation.world"; // All nodes live in this class.
        Reflections reflections = new Reflections((new ConfigurationBuilder()).setScanners(new Scanners[]{Scanners.SubTypes, Scanners.TypesAnnotated}).setUrls(ClasspathHelper.forPackage(SIMULATION_WORLD_PACKAGE, new ClassLoader[0])).filterInputsBy((new FilterBuilder()).includePackage(SIMULATION_WORLD_PACKAGE)));
        Set classes = reflections.getSubTypesOf(World.class);
        Class[] world_child_classes = (Class[])classes.toArray(new Class[classes.size()]);
        for(int i = 0; i < world_child_classes.length; i++){
            Class world_child_class = world_child_classes[i];
            // Prevent instantiation of new Abstract class instance. Always need to instantiate the child-class not parent abstract class.
            if(!Modifier.isAbstract(world_child_class.getModifiers())){
                templates.add(world_child_class); // Populate our templates based on reflection.
            }
        }
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
    }

    public void render(){
        if(hasActiveSimulation()) {
            fbo.bindFrameBuffer();

            GL43.glEnable(GL43.GL_DEPTH_TEST);
            GL43.glClear(GL43.GL_DEPTH_BUFFER_BIT | GL43.GL_COLOR_BUFFER_BIT);

            ShaderManager.getInstance().bindUniforms();
            simulation.render();

            // All immediate Mode Graphics stuff
            LineRenderer.getInstance().render();

            fbo.unbindFrameBuffer();
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

    public Simulation getActiveSimulation() {
        return this.simulation;
    }

    public Uniform getFrameDelta(){
        return u_time_delta;
    }

    public Uniform getTimeSeconds(){
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

    public World getWorldTemplate(JsonObject world_initialization_data){
        // Default properties
        String simulation_name = "Unnamed Simulation";
        if(world_initialization_data.has("simulation_name")){
            simulation_name = world_initialization_data.get("simulation_name").getAsString();
        }

        String template = DefaultWorld2D.class.getName();
        if(world_initialization_data.has("template")){
            template = world_initialization_data.get("template").getAsString();
        }

        try {
            // Convert name to a class
            Class<? extends World> node_class = (Class<? extends World>) Class.forName(template);

            // If we have a template for this class instantiate an instance of it.
            if(templates.contains(node_class)){
                // Instantiate a new instance of this class.
                for(Constructor constructor : node_class.getConstructors()){
                    if(constructor.getParameterCount() == 1){
                        try {
                            World world = (World) constructor.newInstance(world_initialization_data);
                            return world;
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // We didn't recognise the world type
        System.err.println(String.format("Error: Simulation:\"%s\" uses a world template that we did not recognise. Unrecognised template:\"%s\"", simulation_name, template));
        // Default
        return new DefaultWorld2D(world_initialization_data);
    }

    public int getSimulationTexture(){
        return (fbo != null) ? fbo.getTextureID() : 0;
    }

    public Collection<Class<? extends World>> getWorldTemplates(){
        return this.templates;
    }
}
