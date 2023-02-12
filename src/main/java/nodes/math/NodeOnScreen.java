package nodes.math;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeOnScreen extends Node {
    public NodeOnScreen() {
        super("@on_screen", 2);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = NodeManager.getInstance().transpile(params[0]);
        String par_1 = NodeManager.getInstance().transpile(params[1]);
        return String.format("!(%s < -1.0 || %s > 1.0 || %s < -1.0 || %s > 1.0)", par_0, par_0, par_1, par_1);
    }
}
