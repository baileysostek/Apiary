package nodegraph.nodes.math;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class CastToIntNode extends TemplateNode {

    public CastToIntNode(JsonObject initialization_data) {
        super("Cast to Int", FunctionDirective.CAST_TO_INT, GLDataType.INT, initialization_data);

        // Still need to add our input values.
        addInputPin("theta", GLDataType.FLOAT);
    }
}
