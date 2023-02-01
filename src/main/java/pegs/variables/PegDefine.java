package pegs.variables;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegDefine extends Peg {
    public PegDefine() {
        super("@define", 3);
    }

    @Override
    protected void action(Stack<JsonElement> stack, JsonElement[] params) {
        //TODO: Implement
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String variable_type = PegManager.getInstance().transpile(params[0]);
        String variable_name = PegManager.getInstance().transpile(params[1]);
        String variable_value = PegManager.getInstance().transpile(params[2]);

        return String.format("%s %s = %s ;\n", variable_type, variable_name, variable_value);
    }
}
