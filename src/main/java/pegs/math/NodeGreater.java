package pegs.math;

import com.google.gson.JsonElement;
import pegs.Node;
import pegs.NodeManager;

import java.util.Stack;

public class NodeGreater extends Node {
    public NodeGreater() {
        super("@greater", 2);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = NodeManager.getInstance().transpile(params[0]);
        String par_1 = NodeManager.getInstance().transpile(params[1]);

        return String.format("%s > %s", par_0, par_1);
    }
}
