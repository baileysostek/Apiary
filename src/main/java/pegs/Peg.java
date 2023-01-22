package pegs;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import java.util.LinkedList;
import java.util.Stack;

public abstract class Peg {
    private final String key;
    private final int num_params;

    protected Peg(String key, int num_params) {
        // Ensure that the key starts with an @ symbol and is lower case
        this.key = (key.startsWith("@") ? key : "@" + key).toLowerCase();

        // This is the number of parameters that this peg expects to be on the stack to consume.
        this.num_params = num_params;
    }

    protected final void performAction(Stack<JsonElement> stack){
        JsonElement[] params = new JsonElement[num_params];
        for(int i = 0; i < num_params; i++){
            params[num_params - i - 1] = stack.pop();
        }
        action(stack, params);
    }

    protected abstract void action(Stack<JsonElement> stack, JsonElement[] params);

    protected final LinkedList<String> transpile(Stack<JsonElement> stack){
        JsonElement[] params = new JsonElement[num_params];
        for(int i = 0; i < num_params; i++){
            params[num_params - i - 1] = stack.pop();
        }
        LinkedList<String> out = toGLSL(stack, params);
        String concat = "";
        for(String line : out){
            concat += line + " ";
        }
        stack.push(new JsonPrimitive(concat));
        return out;
    }
    protected LinkedList<String> toGLSL(Stack<JsonElement> stack, JsonElement[] params){
        LinkedList<String> default_glsl = new LinkedList<>();
        default_glsl.push(this.key);
        return default_glsl;
    };

    public String getKey() {
        return this.key;
    }
}
