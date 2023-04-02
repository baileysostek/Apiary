package nodegraph.nodes.logic;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;

public class OrNode extends TemplateNode {

    public OrNode(JsonObject initialization_data) {
        super("Or", FunctionDirective.OR, GLDataType.BOOL, initialization_data);

        InflowPin condition_one = addInputPin("A", GLDataType.BOOL);
        InflowPin condition_two = addInputPin("B", GLDataType.BOOL);

    }
}
