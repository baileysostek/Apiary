package pegs.math;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegSub extends Peg {
    public PegSub() {
        super("@sub", 2);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = PegManager.getInstance().transpile(params[0]);
        String par_1 = PegManager.getInstance().transpile(params[1]);

        return String.format("(%s - %s)", par_0, par_1);
    }
}
