package pegs.controlflow;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegConditional extends Peg {
    public PegConditional() {
        super("@conditional", 3);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> object_stack, JsonElement[] params) {
        String conditional = PegManager.getInstance().transpile(params[0]);
        String consequent = PegManager.getInstance().transpile(params[1]);
        String alternate = PegManager.getInstance().transpile(params[2]);

        String out = "";
        out += (String.format("if (%s) {\n", conditional));
        out += (String.format("\t%s", consequent));
        out += ("} else {\n");
        out += (String.format("\t%s", alternate));
        out += ("}\n");
        return out;
    }
}
