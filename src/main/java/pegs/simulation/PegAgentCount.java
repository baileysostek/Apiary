package pegs.simulation;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import pegs.Peg;
import pegs.PegManager;
import simulation.Simulation;
import simulation.SimulationManager;

import java.util.LinkedList;
import java.util.Stack;

public class PegAgentCount extends Peg {
    public PegAgentCount() {
        super("@agent_count", 1);
    }

    @Override
    protected void action(Stack<JsonElement> stack, JsonElement[] params) {
        //TODO
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Here we get the type of agent we are looking for.
        String agent_type = PegManager.getInstance().transpile(params[0]);

        // Since this is the all command, we are going to compute the number of agents of this type which have been allocated for the simulation.
        if(SimulationManager.getInstance().hasActiveSimulation()) {
            Simulation simulation = SimulationManager.getInstance().getActiveSimulation();
            if(simulation.hasAgent(agent_type)) {
                int agent_count = simulation.getAllocatedCapacity(agent_type);
                // Push the agent count onto the stack
                stack.push(new JsonPrimitive(agent_count));
                // Return the agent count.
                return String.format("%s", agent_count);
            }
        }

        // No agents found.
        return "0";
    }
}
