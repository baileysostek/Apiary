package pegs.variables;

import com.google.gson.JsonElement;
import pegs.Node;
import pegs.NodeManager;

import java.util.Stack;

public class NodeDefine extends Node {
    public NodeDefine() {
        super("@define", 3);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String variable_type = NodeManager.getInstance().transpile(params[0]);
        String variable_name = NodeManager.getInstance().transpile(params[1]);
        String variable_value = NodeManager.getInstance().transpile(params[2]);

        return String.format("%s %s = %s ;\n", variable_type, variable_name, variable_value);
    }
}
