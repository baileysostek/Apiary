package pegs.math;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class NodeIsOnScreen extends Peg {
    public NodeIsOnScreen() {
        super("@on_screen", 2);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = PegManager.getInstance().transpile(params[0]);
        String par_1 = PegManager.getInstance().transpile(params[1]);
        return String.format("!(%s < -1.0 || %s > 1.0 || %s < -1.0 || %s > 1.0)", par_0, par_0, par_1, par_1);
    }
}
