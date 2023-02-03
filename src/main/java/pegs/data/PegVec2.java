package pegs.data;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegVec2 extends Peg {
    public PegVec2() {
        super("@vec2", 2);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = PegManager.getInstance().transpile(params[0]);
        String par_1 = PegManager.getInstance().transpile(params[1]);

        return String.format("vec2(%s , %s)", par_0, par_1);
    }
}
