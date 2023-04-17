package nodegraph.nodes.vector;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;

public class RotateVec2Node extends TemplateNode {
    public RotateVec2Node(JsonObject initialization_data) {
        super("Rotate 2D", FunctionDirective.ROTATE_2D, GLDataType.VEC2, initialization_data);

        InflowPin vector = addInputPin("vector", GLDataType.VEC2);
        InflowPin theta = addInputPin("theta", GLDataType.INT , GLDataType.FLOAT);
    }

    @Override
    public void render() {
        super.render();
    }
}
