package nodes.math;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeXYToScreenIndex extends Node {
    public NodeXYToScreenIndex() {
        super("@xy_to_screen_index", 2);
        this.requiresUniform("u_window_size");
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String x = NodeManager.getInstance().transpile(params[0]);
        String y = NodeManager.getInstance().transpile(params[1]);

        return String.format("int(mod(%s, window_width_pixels)) + (int(mod(%s, window_height_pixels)) * window_width_pixels)", x, y);
//        return String.format("int(clamp(%s, 0, window_width_pixels)) + (int(clamp(%s, 0, window_height_pixels)) * window_width_pixels)", x, y);
    }
}
