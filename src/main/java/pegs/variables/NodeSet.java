package pegs.variables;

import com.google.gson.JsonElement;
import pegs.Node;
import pegs.NodeManager;

import java.util.Stack;

public class NodeSet extends Node {
    public NodeSet() {
        super("@set", 2);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String variable_name  = NodeManager.getInstance().transpile(params[0]);
        String variable_value = NodeManager.getInstance().transpile(params[1]);

        return String.format("%s = %s ;\n", variable_name, variable_value);
    }
}
