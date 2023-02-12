package pegs.data;

import com.google.gson.JsonElement;
import pegs.Node;
import pegs.NodeManager;

import java.util.Stack;

public class NodeVec3 extends Node {
    public NodeVec3() {
        super("@vec3", 3);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = NodeManager.getInstance().transpile(params[0]);
        String par_1 = NodeManager.getInstance().transpile(params[1]);
        String par_2 = NodeManager.getInstance().transpile(params[2]);

        return String.format("vec3(%s , %s , %s)", par_0, par_1, par_2);
    }
}
