package nodegraph.nodes.math;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class AddNode extends TemplateNode {
    public AddNode(JsonObject initialization_data) {
        super("Add", FunctionDirective.ADD, GLDataType.INT, initialization_data);

        addInputPin("x_pos", GLDataType.INT , GLDataType.FLOAT);
        addInputPin("y_pos", GLDataType.INT , GLDataType.FLOAT);

    }
}
