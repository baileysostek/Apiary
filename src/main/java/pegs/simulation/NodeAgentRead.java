package pegs.simulation;

import com.google.gson.JsonElement;
import graphics.ShaderManager;
import pegs.NodeManager;
import simulation.Simulation;
import simulation.SimulationManager;
import util.StringUtils;

import java.util.HashMap;
import java.util.Stack;

public class NodeAgentRead extends NodeAgent {

    public NodeAgentRead() {
        super("@agent_read", 3);
    }

    @Override
    public String agentToGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        HashMap<String, Object> substitutions = new HashMap<>();
        substitutions.put("agent_type", NodeManager.getInstance().transpile(params[0]));
        substitutions.put("agent_index", NodeManager.getInstance().transpile(params[1]));
        substitutions.put("agent_property", NodeManager.getInstance().transpile(params[2]));
        substitutions.put("read_identifier", ShaderManager.getInstance().getSSBOReadIdentifier());

        // Since this is the all command, we are going to compute the number of agents of this type which have been allocated for the simulation.
        if(SimulationManager.getInstance().hasActiveSimulation()) {
            Simulation simulation = SimulationManager.getInstance().getActiveSimulation();
            if(SimulationManager.getInstance().hasAgent((String) substitutions.get("agent_type"))) {
                return StringUtils.format("{{agent_type}}{{read_identifier}}.agent[{{agent_index}}].{{agent_property}}", substitutions);
            }else{
                //TODO throw compilation error.
                System.err.println("Error cannot parse properties of this agent");
            }
        }

        // No agents found.
        return "0";
    }
}
