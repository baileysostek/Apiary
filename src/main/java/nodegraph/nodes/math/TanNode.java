package nodegraph.nodes.math;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class TanNode extends TemplateNode {

    public TanNode(JsonObject initialization_data) {
        super("tan", FunctionDirective.TAN, GLDataType.FLOAT, initialization_data);

        // Still need to add our input values.
        addInputPin("theta", GLDataType.FLOAT);
    }
}
