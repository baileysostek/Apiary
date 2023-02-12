package nodes.code;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeCode extends Node {
    public NodeCode() {
        super("@code", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String code = NodeManager.getInstance().transpile(params[0]);
        return code;
    }
}
