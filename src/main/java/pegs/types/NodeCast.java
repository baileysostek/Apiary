package pegs.types;

import com.google.gson.JsonElement;
import pegs.Node;
import pegs.NodeManager;

import java.util.Stack;

public class NodeCast extends Node {
    public NodeCast() {
        super("@cast", 2);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String value = NodeManager.getInstance().transpile(params[0]);
        String type = NodeManager.getInstance().transpile(params[1]);

        return String.format("%s(%s)", type, value);
    }
}
