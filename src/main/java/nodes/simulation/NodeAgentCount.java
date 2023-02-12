package nodes.simulation;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import nodes.NodeManager;
import simulation.SimulationManager;

import java.util.Stack;

public class NodeAgentCount extends NodeAgent {
    public NodeAgentCount() {
        super("@agent_count", 1);
    }

    @Override
    public String agentToGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Here we get the type of agent we are looking for.
        String agent_type = NodeManager.getInstance().transpile(params[0]);

        // Since this is the all command, we are going to compute the number of agents of this type which have been allocated for the simulation.
        if(SimulationManager.getInstance().hasActiveSimulation()) {
            if(SimulationManager.getInstance().hasAgent(agent_type)) {
                int agent_count = SimulationManager.getInstance().getAgent(agent_type).getCapacity();
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