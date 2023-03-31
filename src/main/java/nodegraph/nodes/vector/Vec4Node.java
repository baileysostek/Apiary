package nodegraph.nodes.vector;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class Vec4Node extends TemplateNode {

    public Vec4Node(JsonObject initialization_data) {
        super("vec4", FunctionDirective.VEC4, GLDataType.VEC4, initialization_data);

        // Still need to add our input values.
        addInputPin("x", GLDataType.FLOAT);
        addInputPin("y", GLDataType.FLOAT);
        addInputPin("z", GLDataType.FLOAT);
        addInputPin("w", GLDataType.FLOAT);
    }
}
