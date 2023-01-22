package pegs.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import pegs.Peg;
import pegs.PegManager;

import java.util.LinkedList;
import java.util.Stack;

public class PegNot extends Peg {
    public PegNot() {
        super("@not", 1);
    }

    @Override
    protected void action(Stack<JsonElement> stack, JsonElement[] params) {

        boolean par_0 = false;
        try{
            par_0 =  params[0].getAsBoolean();
        }catch (Exception e){
            e.printStackTrace();
        }

        stack.push(new JsonPrimitive(!par_0));
    }

    @Override
    protected LinkedList<String> toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        LinkedList<String> par_0 = PegManager.getInstance().transpile(params[0]);

        LinkedList<String> out = new LinkedList<>();
        out.push(String.format("!%s", par_0));
        return out;
    }
}
