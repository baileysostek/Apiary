package pegs.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import pegs.Peg;
import pegs.PegManager;

import java.util.LinkedList;
import java.util.Stack;

public class PegXor extends Peg {
    public PegXor() {
        super("@xor", 2);
    }

    @Override
    protected void action(Stack<JsonElement> stack, JsonElement[] params) {

        boolean par_0 = false;
        try{
            par_0 =  params[0].getAsBoolean();
        }catch (Exception e){
            e.printStackTrace();
        }

        boolean par_1 = false;
        try{
            par_1 =  params[1].getAsBoolean();
        }catch (Exception e){
            e.printStackTrace();
        }

        stack.push(new JsonPrimitive(par_0 ^ par_1));
    }

    @Override
    protected LinkedList<String> toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        LinkedList<String> par_0 = PegManager.getInstance().transpile(params[0]);
        LinkedList<String> par_1 = PegManager.getInstance().transpile(params[1]);

        LinkedList<String> out = new LinkedList<>();
        out.push(String.format("(%s ^^ %s)", par_0, par_1));
        return out;
    }
}
