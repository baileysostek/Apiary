package pegs.math;

import com.google.gson.JsonElement;
import pegs.Node;
import pegs.NodeManager;

import java.util.Stack;

public class NodeTan extends Node {
    public NodeTan() {
        super("@tan", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = NodeManager.getInstance().transpile(params[0]);

        return String.format("tan(%s)", par_0);
    }
}
