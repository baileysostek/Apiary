package pegs.code;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegCode extends Peg {
    public PegCode() {
        super("@code", 1);
    }

    @Override
    protected void action(Stack<JsonElement> stack, JsonElement[] params) {
       //TODO
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String code = PegManager.getInstance().transpile(params[0]);
        return code;
    }
}
