package nodes.vector;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeMix extends Node {

    public NodeMix() {
        super("@mix", 3);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String par_0  = NodeManager.getInstance().transpile(params[0]);
        String par_1  = NodeManager.getInstance().transpile(params[1]);
        String par_2  = NodeManager.getInstance().transpile(params[2]);

        return String.format("mix(%s, %s, %s)", par_0, par_1, par_2);
    }
}
