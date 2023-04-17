package nodegraph.nodes.logic;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;

public class GreaterEqualNode extends TemplateNode {

    public GreaterEqualNode(JsonObject initialization_data) {
        super("Greater or Equal", FunctionDirective.GREATER_OR_EQUAL, GLDataType.BOOL, initialization_data);

        InflowPin condition_one = addInputPin("A", GLDataType.INT, GLDataType.FLOAT);
        InflowPin condition_two = addInputPin("B", GLDataType.INT, GLDataType.FLOAT);
    }
}
