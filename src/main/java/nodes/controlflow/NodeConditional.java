package nodes.controlflow;

import com.google.gson.JsonElement;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeConditional extends Node {
    public NodeConditional() {
        super("@conditional", 3);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> object_stack, JsonElement[] params) {
        String conditional = NodeManager.getInstance().transpile(params[0]);
        String consequent = NodeManager.getInstance().transpile(params[1]);
        String alternate = NodeManager.getInstance().transpile(params[2]);

        // If we dont have a consequent but do have an alternate...
        if(consequent.isEmpty() && !alternate.isEmpty()){
            // We are going to invert the if statement to avoid the if/else case
            String out = "";
            out += (String.format("if (!(%s)) {\n", conditional)); // Inverted
            out += (String.format("\t%s", alternate.endsWith("\n") ? alternate : alternate + "\n"));
            out += ("}\n");
            return out;
        }else{
            // Default if else case
            String out = "";
            out += (String.format("if (%s) {\n", conditional));
            out += (String.format("\t%s", consequent.endsWith("\n") ? consequent : consequent + "\n"));
            if(alternate.isEmpty()) {
                // End the conditional.
                out += ("}\n");
            } else {
                // Add the alternate
                out += ("} else {\n");
                out += (String.format("\t%s", alternate.endsWith("\n") ? alternate : alternate + "\n"));
                out += ("}\n");
            }
            return out;
        }
    }
}
