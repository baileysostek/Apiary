package pegs.math;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegTan extends Peg {
    public PegTan() {
        super("@tan", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = PegManager.getInstance().transpile(params[0]);

        return String.format("tan(%s)", par_0);
    }
}
