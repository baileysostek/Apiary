package pegs.simulation;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import pegs.Peg;
import pegs.PegManager;
import simulation.Simulation;
import simulation.SimulationManager;

import java.util.LinkedList;
import java.util.Stack;

public class PegGetAgentAtIndex extends Peg {
    public PegGetAgentAtIndex() {
        super("@get_agent_at_index", 2);
    }

    @Override
    protected void action(Stack<JsonElement> stack, JsonElement[] params) {
        //TODO
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Here we get the type of agent we are looking for.
        String agent_type = PegManager.getInstance().transpile(params[0]);
        String agent_index = PegManager.getInstance().transpile(params[1]);

        // Since this is the all command, we are going to compute the number of agents of this type which have been allocated for the simulation.
        if(SimulationManager.getInstance().hasActiveSimulation()) {
            Simulation simulation = SimulationManager.getInstance().getActiveSimulation();
            if(simulation.hasAgent(agent_type)) {
                int agent_count = simulation.getAllocatedCapacity(agent_type);
                try {
                    int agent_index_integer = Integer.parseInt(agent_index);
                    if (agent_index_integer >= 0 && agent_index_integer < agent_count) {
                        return String.format("all_%s.agent[%s]", agent_type, agent_index + "");
                    } else {
                        //TODO throw error
                        System.err.println("Error tried to index into " + agent_type + "[" + agent_index_integer + "] however only " + agent_count + " instances of that agent exist.");
                    }
                }catch(NumberFormatException e){
                    e.printStackTrace();
                    // Warning this could be a variable reference, we need to know what variables have been defined in this shader.
                    return String.format("all_%s.agent[%s]", agent_type, agent_index + "");
                }
            }
        }else{
            //TODO throw error
            System.err.println("Error: agent" + agent_type + " does not exist in this simulation.");
        }

        // Null pointer
        return "//Error with GetAgentAtIndexPeg\n";
    }
}
