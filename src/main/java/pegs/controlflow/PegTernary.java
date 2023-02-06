package pegs.controlflow;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegTernary extends Peg {
    public PegTernary() {
        super("@?", 3);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String predicate = PegManager.getInstance().transpile(params[0]);
        String consequent = PegManager.getInstance().transpile(params[1]);
        String alternate = PegManager.getInstance().transpile(params[2]);

        return String.format("(%s ? %s : %s)", predicate, consequent, alternate);
    }
}
