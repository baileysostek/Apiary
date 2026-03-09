package nodegraph.nodes.vector;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;

public class AbsoluteDistanceNode extends TemplateNode {

    public AbsoluteDistanceNode(JsonObject initialization_data) {
        super("Absolute Distance", FunctionDirective.ABSOLUTE_DISTANCE, GLDataType.FLOAT, initialization_data);

        InflowPin input_x = addInputPin("x_pos", GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);
        InflowPin input_y = addInputPin("y_pos", GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);
    }

    @Override
    public void render() {
        super.render();
    }
}
