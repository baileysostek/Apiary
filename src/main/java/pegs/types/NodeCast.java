package pegs.types;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class NodeCast extends Peg {
    public NodeCast() {
        super("@cast", 2);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String value = PegManager.getInstance().transpile(params[0]);
        String type = PegManager.getInstance().transpile(params[1]);

        return String.format("%s(%s)", type, value);
    }
}
