package nodegraph.nodes.data;

import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class Vec3Node extends TemplateNode {

    public Vec3Node() {
        super("vec3", FunctionDirective.VEC3, GLDataType.VEC3);

        // Still need to add our input values.
        addInputPin("x", GLDataType.FLOAT);
        addInputPin("y", GLDataType.FLOAT);
        addInputPin("z", GLDataType.FLOAT);
    }
}
