package pegs.math;

import com.google.gson.JsonElement;
import pegs.Node;
import pegs.NodeManager;

import java.util.Stack;

public class NodeCos extends Node {
    public NodeCos() {
        super("@cos", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = NodeManager.getInstance().transpile(params[0]);

        return String.format("cos(%s)", par_0);
    }
}
