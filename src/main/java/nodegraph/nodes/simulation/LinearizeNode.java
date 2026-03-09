package nodegraph.nodes.simulation;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class LinearizeNode extends TemplateNode {

    public LinearizeNode(JsonObject initialization_data) {
        super("Linearize", FunctionDirective.XY_TO_SCREEN_INDEX, GLDataType.INT, initialization_data);

        addInputPin("x_pos", GLDataType.INT);
        addInputPin("y_pos", GLDataType.INT);

    }
}
