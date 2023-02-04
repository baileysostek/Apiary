package pegs.math;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegAbs extends Peg {
    public PegAbs() {
        super("@abs", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = PegManager.getInstance().transpile(params[0]);

        return String.format("abs(%s)", par_0);
    }
}
