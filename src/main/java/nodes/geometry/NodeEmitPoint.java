package nodes.geometry;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeEmitPoint extends Node {
    public NodeEmitPoint() {
        super("@emit_point", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = NodeManager.getInstance().transpile(params[0]);

        return String.format(
            "gl_Position = %s;\n" +
            "EmitVertex();\n" +
            "EndPrimitive();\n", par_0
        );
    }
}
