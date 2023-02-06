package pegs.math;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegRotate2D extends Peg {
    public PegRotate2D() {
        super("@rotate_2d", 2);
        this.requiresInclude("rotation_2d");
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = PegManager.getInstance().transpile(params[0]);
        String par_1 = PegManager.getInstance().transpile(params[1]);
        return String.format("rotate(%s,%s)", par_0, par_1);
    }
}
