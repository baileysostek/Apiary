package pegs.data;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegVec3 extends Peg {
    public PegVec3() {
        super("@vec3", 3);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = PegManager.getInstance().transpile(params[0]);
        String par_1 = PegManager.getInstance().transpile(params[1]);
        String par_2 = PegManager.getInstance().transpile(params[2]);

        return String.format("vec3(%s , %s , %s)", par_0, par_1, par_2);
    }
}
