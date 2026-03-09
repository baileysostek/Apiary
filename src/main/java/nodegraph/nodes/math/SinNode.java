package nodegraph.nodes.math;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class SinNode extends TemplateNode {

    public SinNode(JsonObject initialization_data) {
        super("sin", FunctionDirective.SIN, GLDataType.FLOAT, initialization_data);

        // Still need to add our input values.
        addInputPin("theta", GLDataType.FLOAT);
    }
}
