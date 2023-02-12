package nodes.math;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeMin extends Node {
    public NodeMin() {
        super("@min", 2);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = NodeManager.getInstance().transpile(params[0]);
        String par_1 = NodeManager.getInstance().transpile(params[1]);

        return String.format("min(%s,%s)", par_0, par_1);
    }
}
