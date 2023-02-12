package pegs.simulation;

import com.google.gson.JsonElement;
import pegs.NodeManager;
import simulation.SimulationManager;

import java.util.Stack;

public class NodeGetAgentAtIndex extends NodeAgent {
    public NodeGetAgentAtIndex() {
        super("@get_agent_at_index", 2);
    }

    @Override
    public String agentToGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Here we get the type of agent we are looking for.
        String agent_type = NodeManager.getInstance().transpile(params[0]);
        String agent_index = NodeManager.getInstance().transpile(params[1]);

        // Since this is the all command, we are going to compute the number of agents of this type which have been allocated for the simulation.
        if(SimulationManager.getInstance().hasActiveSimulation()) {
            if(SimulationManager.getInstance().hasAgent(agent_type)) {
                int agent_count = SimulationManager.getInstance().getAgent(agent_type).getCapacity();
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
