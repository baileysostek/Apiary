package nodes.simulation;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public abstract class NodeAgent extends Node {
    protected NodeAgent(String key, int num_params) {
        super(key, num_params);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Here we get the type of agent we are looking for.
        String agent_type = NodeManager.getInstance().transpile(params[0]);

        // Tell the PegManager that we are referencing an agent, therefore we need to include the agents SSBO in whatever shader this Peg was used in.
        NodeManager.getInstance().requireAgent(agent_type);

        return agentToGLSL(stack, params);
    }

    public abstract String agentToGLSL(Stack<JsonElement> stack, JsonElement[] params);
}
