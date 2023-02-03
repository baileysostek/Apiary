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
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String conditional = PegManager.getInstance().transpile(params[0]);
        String if_statement = PegManager.getInstance().transpile(params[1]);
        String else_statement = PegManager.getInstance().transpile(params[2]);

        String out = "";
        out += (String.format("if (%s) {\n", conditional));
        out += (String.format("%s", if_statement));
        out += ("} else {\n");
        out += (String.format("%s", else_statement));
        out += ("}\n");
        return out;
    }
}
