package nodegraph.nodes.logic;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;

public class AndNode extends TemplateNode {

    public AndNode(JsonObject initialization_data) {
        super("And", FunctionDirective.AND, GLDataType.BOOL, initialization_data);

        InflowPin condition_one = addInputPin("A", GLDataType.BOOL);
        InflowPin condition_two = addInputPin("B", GLDataType.BOOL);

    }
}
