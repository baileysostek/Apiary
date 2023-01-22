package pegs.math;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import pegs.Peg;
import pegs.PegManager;

import java.util.LinkedList;
import java.util.Stack;

public class PegGreater extends Peg {
    public PegGreater() {
        super("@greater", 2);
    }

    @Override
    protected void action(Stack<JsonElement> stack, JsonElement[] params) {

        float par_0 = 0;
        try{
            par_0 =  params[0].getAsFloat();
        }catch (Exception e){
            e.printStackTrace();
        }

        float par_1 = 0;
        try{
            par_1 =  params[1].getAsFloat();
        }catch (Exception e){
            e.printStackTrace();
        }

        stack.push(new JsonPrimitive(par_0 > par_1));
    }

    @Override
    protected LinkedList<String> toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        LinkedList<String> par_0 = PegManager.getInstance().transpile(params[0]);
        LinkedList<String> par_1 = PegManager.getInstance().transpile(params[1]);

        LinkedList<String> out = new LinkedList<>();
        out.push(String.format("%s > %s", par_0, par_1));
        return out;
    }
}
