package pegs.geometry;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegEmitPoint extends Peg {
    public PegEmitPoint() {
        super("@emit_point", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        String par_0 = PegManager.getInstance().transpile(params[0]);

        return String.format(
            "gl_Position = %s;\n" +
            "EmitVertex();\n" +
            "EndPrimitive();\n", par_0
        );
    }
}
