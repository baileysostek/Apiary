package pegs.controlflow;

import com.google.gson.JsonElement;
import pegs.Node;
import pegs.NodeManager;

import java.util.Stack;

public class NodeOutColor extends Node {
    public NodeOutColor() {
        super("@out_color", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String return_value = NodeManager.getInstance().transpile(params[0]);
        return String.format("out_color = %s;\n", return_value);
    }
}
