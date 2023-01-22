package pegs;

import com.google.gson.JsonElement;

import java.util.LinkedList;
import java.util.Stack;

public class PegConditional extends Peg {
    protected PegConditional() {
        super("@conditional", 3);
    }

    @Override
    protected void action(Stack<JsonElement> stack, JsonElement[] params) {
        JsonElement conditional = PegManager.getInstance().parse(params[0]);
        // Try to get our conditional as a boolean
        if(conditional.isJsonPrimitive()){
            try{
                boolean conditional_value = conditional.getAsBoolean();
                if(conditional_value){
                    // Eval true state
                    stack.push(PegManager.getInstance().parse(params[1]));
                    return;
                }
            }catch(Exception e){
                //TODO error throw here
                e.printStackTrace();
            }
        }

        stack.push(PegManager.getInstance().parse(params[2]));
    }

    @Override
    protected LinkedList<String> toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        LinkedList<String> conditional = PegManager.getInstance().transpile(params[0]);
        LinkedList<String> if_statement = PegManager.getInstance().transpile(params[1]);
        LinkedList<String> else_statement = PegManager.getInstance().transpile(params[2]);

        LinkedList<String> out = new LinkedList<>();
        out.addLast(String.format("if (%s) {", conditional));
        out.addLast(String.format("%s", if_statement));
        out.addLast("} else {");
        out.addLast(String.format("%s", else_statement));
        out.addLast("}");
        return out;
    }
}
