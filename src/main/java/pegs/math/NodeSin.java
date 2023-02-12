package pegs.math;

import com.google.gson.JsonElement;
import pegs.Node;
import pegs.NodeManager;

import java.util.Stack;

public class NodeSin extends Node {
    public NodeSin() {
        super("@sin", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = NodeManager.getInstance().transpile(params[0]);

        return String.format("sin(%s)", par_0);
    }
}
