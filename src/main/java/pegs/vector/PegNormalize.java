package pegs.vector;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegNormalize extends Peg {
    public PegNormalize() {
        super("@normalize", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String par_0  = PegManager.getInstance().transpile(params[0]);

        return String.format("normalize(%s)", par_0);
    }
}
