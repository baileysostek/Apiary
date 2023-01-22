package pegs;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Stack;

public class PegRandomFloat extends Peg{

    public PegRandomFloat(){
        super("@random_float", 0);
    }

    @Override
    protected void action(Stack<JsonElement> stack, JsonElement[] params) {
        stack.push(new JsonPrimitive(Math.random()));
    }
}
