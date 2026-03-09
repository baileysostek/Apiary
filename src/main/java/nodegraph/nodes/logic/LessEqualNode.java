package nodegraph.nodes.logic;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;

public class LessEqualNode extends TemplateNode {

    public LessEqualNode(JsonObject initialization_data) {
        super("Less or Equal", FunctionDirective.LESS_OR_EQUAL, GLDataType.BOOL, initialization_data);

        InflowPin condition_one = addInputPin("A", GLDataType.INT, GLDataType.FLOAT);
        InflowPin condition_two = addInputPin("B", GLDataType.INT, GLDataType.FLOAT);
    }
}
