package pegs.controlflow;

import com.google.gson.JsonElement;
import pegs.Peg;
import pegs.PegManager;
import util.StringUtils;

import java.util.HashMap;
import java.util.Stack;

/**
 * For each assumes that you want to run over EACH of the numbers in the supplied range.
 * This function always starts at i == 0 and continues to i == (iteration_size -1)
 * Regular for loop allows you to specify start and end points.
 */
public class PegForEach extends Peg {
    public PegForEach() {
        super("@for_each", 2);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {

        // Determine our upper bound
        int upper_bound = Integer.parseInt(PegManager.getInstance().transpile(params[0]));
        String loop_body = PegManager.getInstance().transpile(params[1]);

        HashMap<String, Object> substitutions = new HashMap<>();
        substitutions.put("lower_bound", 0);
        substitutions.put("upper_bound", upper_bound);
        substitutions.put("loop_body", loop_body);

        return StringUtils.format(
            //TODO replace "i" with a call to get the next available variable name.
            "for (int i = {{lower_bound}}; i < {{upper_bound}}; i++) {\n" +
            "{{loop_body}}\n"+
            "}\n",
            substitutions
        );
    }
}
