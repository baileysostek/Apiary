package nodes.vector;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeNormalize extends Node {
    public NodeNormalize() {
        super("@normalize", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String par_0  = NodeManager.getInstance().transpile(params[0]);

        return String.format("normalize(%s)", par_0);
    }
}
