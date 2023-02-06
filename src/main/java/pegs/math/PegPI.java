package pegs.math;

import com.google.gson.JsonElement;
import pegs.Peg;

import java.util.Stack;

public class PegPI extends Peg {
    public PegPI() {
        super("@pi", 0);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        return String.format("3.1415926");
    }
}
