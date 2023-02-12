package nodes.math;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeRotate2D extends Node {
    public NodeRotate2D() {
        super("@rotate_2d", 2);
        this.requiresInclude("rotation_2d");
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = NodeManager.getInstance().transpile(params[0]);
        String par_1 = NodeManager.getInstance().transpile(params[1]);
        return String.format("rotate(%s,%s)", par_0, par_1);
    }
}
