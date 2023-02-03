package pegs.code;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegCode extends Peg {
    public PegCode() {
        super("@code", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String code = PegManager.getInstance().transpile(params[0]);
        return code;
    }
}
