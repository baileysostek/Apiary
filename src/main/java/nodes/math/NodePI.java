package nodes.math;

import com.google.gson.JsonElement;
import nodes.Node;

import java.util.Stack;

public class NodePI extends Node {
    public NodePI() {
        super("@pi", 0);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        return String.format("3.1415926");
    }
}
