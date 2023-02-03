package pegs.controlflow;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegOutColor extends Peg {
    public PegOutColor() {
        super("@out_color", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String return_value = PegManager.getInstance().transpile(params[0]);
        return String.format("out_color = %s;\n", return_value);
    }
}
