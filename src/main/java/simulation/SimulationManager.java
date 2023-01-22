package simulation;

import com.google.gson.JsonObject;
import graphics.Shader;
import org.lwjgl.opengl.GL46;
import util.JsonUtils;

public class SimulationManager {

    // Singleton Instance
    private static SimulationManager singleton;
    // Singleton variables
    private Simulation simulation = null;

    private SimulationManager() {

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
        if(hasActiveSimulation()){
            simulation.update(delta);
        }
    }

    public void render(){

        GL46.glEnable(GL46.GL_DEPTH_TEST);
        GL46.glClear(GL46.GL_DEPTH_BUFFER_BIT | GL46.GL_COLOR_BUFFER_BIT);

        if(hasActiveSimulation()){
            simulation.render();
        }
    }

    public boolean hasActiveSimulation(){
        return simulation != null;
    }

    public void cleanup(){
        if(simulation != null){
            simulation.cleanup();
        }
    }

}
