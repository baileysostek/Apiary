package pegs.vector;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegMix extends Peg {
    public PegMix() {
        super("@mix", 3);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String par_0  = PegManager.getInstance().transpile(params[0]);
        String par_1  = PegManager.getInstance().transpile(params[1]);
        String par_2  = PegManager.getInstance().transpile(params[2]);

        return String.format("mix(%s, %s, %s)", par_0, par_1, par_2);
    }
}
