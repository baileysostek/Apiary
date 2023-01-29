package pegs.data;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegVec4 extends Peg {
    public PegVec4() {
        super("@vec4", 4);
    }

    @Override
    protected void action(Stack<JsonElement> stack, JsonElement[] params) {
        // TODO
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = PegManager.getInstance().transpile(params[0]);
        String par_1 = PegManager.getInstance().transpile(params[1]);
        String par_2 = PegManager.getInstance().transpile(params[2]);
        String par_3 = PegManager.getInstance().transpile(params[3]);

        return String.format("vec4(%s , %s , %s , %s)", par_0, par_1, par_2, par_3);
    }
}
