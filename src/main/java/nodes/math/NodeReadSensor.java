package nodes.math;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeReadSensor extends Node {
    public NodeReadSensor() {
        super("@read_sensor", 4);
        this.requiresInclude("rotation_2d");
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = NodeManager.getInstance().transpile(params[0]);
        String par_1 = NodeManager.getInstance().transpile(params[1]);
        String par_2 = NodeManager.getInstance().transpile(params[2]);
        String par_3 = NodeManager.getInstance().transpile(params[3]);
        return String.format("readSensor(%s,%s,%s,%s)", par_0, par_1, par_2, par_3);
    }
}
