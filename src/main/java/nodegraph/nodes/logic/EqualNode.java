package nodegraph.nodes.logic;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;

public class EqualNode extends TemplateNode {

    public EqualNode(JsonObject initialization_data) {
        super("Equal", FunctionDirective.EQUALS, GLDataType.BOOL, initialization_data);

        InflowPin condition_one = addInputPin("A", GLDataType.values());
        InflowPin condition_two = addInputPin("B", GLDataType.values());
    }
}
