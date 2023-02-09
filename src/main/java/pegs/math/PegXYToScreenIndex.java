package pegs.math;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegXYToScreenIndex extends Peg {
    public PegXYToScreenIndex() {
        super("@xy_to_screen_index", 2);

        this.requiresUniform("u_window_size");
        this.requiresIncludeInMain("fragment_index");
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String x = PegManager.getInstance().transpile(params[0]);
        String y = PegManager.getInstance().transpile(params[1]);

        return String.format("int(mod(%s, window_width_pixels)) + (int(mod(%s, window_height_pixels)) * window_width_pixels)", x, y);
//        return String.format("int(clamp(%s, 0, window_width_pixels)) + (int(clamp(%s, 0, window_height_pixels)) * window_width_pixels)", x, y);
    }
}
