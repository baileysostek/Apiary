package pegs.variables;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegSet extends Peg {
    public PegSet() {
        super("@set", 2);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String variable_name  = PegManager.getInstance().transpile(params[0]);
        String variable_value = PegManager.getInstance().transpile(params[1]);

        return String.format("%s = %s ;\n", variable_name, variable_value);
    }
}
