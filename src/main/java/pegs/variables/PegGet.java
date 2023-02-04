package pegs.variables;

import com.google.gson.JsonElement;
import graphics.ShaderManager;
import pegs.Peg;
import pegs.PegManager;

import java.util.Stack;

public class PegGet extends Peg {
    public PegGet() {
        super("@get", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String variable_name  = PegManager.getInstance().transpile(params[0]);

        // When we reference some variables
        if(ShaderManager.getInstance().hasUniform(variable_name)){
            this.requiresUniform(variable_name);
        }

        // Must be a locally scoped variabel we dont know about... maybe we throw an exception here in the future if we also capture variable generation.

        return String.format("%s", variable_name);
    }
}
