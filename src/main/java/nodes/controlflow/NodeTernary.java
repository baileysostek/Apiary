package nodes.controlflow;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeTernary extends Node {
    public NodeTernary() {
        super("@?", 3);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String predicate = NodeManager.getInstance().transpile(params[0]);
        String consequent = NodeManager.getInstance().transpile(params[1]);
        String alternate = NodeManager.getInstance().transpile(params[2]);

        return String.format("(%s ? %s : %s)", predicate, consequent, alternate);
    }
}
