package pegs.simulation;

import com.google.gson.JsonElement;
import graphics.ShaderManager;
import pegs.NodeManager;
import simulation.SimulationManager;
import util.StringUtils;

import java.util.HashMap;
import java.util.Stack;

public class NodeAgentWrite extends NodeAgent {

    public NodeAgentWrite() {
        super("@agent_write", 4);
    }

    @Override
    public String agentToGLSL(Stack<JsonElement> stack, JsonElement[] params) {

        HashMap<String, Object> substitutions = new HashMap<>();
        substitutions.put("agent_type", NodeManager.getInstance().transpile(params[0]));
        substitutions.put("agent_index", NodeManager.getInstance().transpile(params[1]));
        substitutions.put("agent_property", NodeManager.getInstance().transpile(params[2]));
        substitutions.put("new_value", NodeManager.getInstance().transpile(params[3]));
        substitutions.put("write_identifier", ShaderManager.getInstance().getSSBOWriteIdentifier());

        // Since this is the all command, we are going to compute the number of agents of this type which have been allocated for the simulation.
        if(SimulationManager.getInstance().hasActiveSimulation()) {
            if(SimulationManager.getInstance().hasAgent((String) substitutions.get("agent_type"))) {
                return StringUtils.format("{{agent_type}}{{write_identifier}}.agent[{{agent_index}}].{{agent_property}} = {{new_value}};\n", substitutions);
            }else{
                //TODO throw compilation error.
                System.err.println("Error cannot parse properties of this agent");
            }
        }

        // No agents found.
        return "0";
    }
}
