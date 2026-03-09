package nodegraph.nodes.math;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class CosNode extends TemplateNode {

    public CosNode(JsonObject initialization_data) {
        super("cos", FunctionDirective.COS, GLDataType.FLOAT, initialization_data);

        // Still need to add our input values.
        addInputPin("theta", GLDataType.FLOAT);
    }
}
