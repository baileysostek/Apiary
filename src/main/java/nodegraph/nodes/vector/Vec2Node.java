package nodegraph.nodes.vector;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class Vec2Node extends TemplateNode {

    public Vec2Node(JsonObject initialization_data) {
        super("vec2", FunctionDirective.VEC2, GLDataType.VEC2, initialization_data);

        // Still need to add our input values.
        addInputPin("x", GLDataType.FLOAT);
        addInputPin("y", GLDataType.FLOAT);
    }
}
