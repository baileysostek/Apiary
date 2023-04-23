package nodegraph.nodes.math;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class CastToFloatNode extends TemplateNode {

    public CastToFloatNode(JsonObject initialization_data) {
        super("Cast to Float", FunctionDirective.CAST_TO_FLOAT, GLDataType.FLOAT, initialization_data);

        // Still need to add our input values.
        addInputPin("theta", GLDataType.INT);
    }
}
