package nodegraph.nodes.logic;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;

public class CrossProductNode extends TemplateNode {

    public CrossProductNode(JsonObject initialization_data) {
        super("Cross Product", FunctionDirective.CROSS_PRODUCT, GLDataType.FLOAT, initialization_data);

        InflowPin input_x = addInputPin("A", GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);
        InflowPin input_y = addInputPin("B", GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);
    }
}
