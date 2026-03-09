package nodegraph.nodes.logic;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;

public class GreaterNode extends TemplateNode {

    public GreaterNode(JsonObject initialization_data) {
        super("Greater", FunctionDirective.GREATER, GLDataType.BOOL, initialization_data);

        InflowPin condition_one = addInputPin("A", GLDataType.INT, GLDataType.FLOAT);
        InflowPin condition_two = addInputPin("B", GLDataType.INT, GLDataType.FLOAT);
    }
}
