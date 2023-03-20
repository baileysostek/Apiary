package nodegraph.nodes.random;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class RandomFloatNode extends TemplateNode {

    public RandomFloatNode(JsonObject initialization_data) {
        super("random_float", FunctionDirective.RANDOM_FLOAT, GLDataType.FLOAT, initialization_data);
    }

}
