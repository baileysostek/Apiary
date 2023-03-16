package nodegraph.nodes.math;

import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class AddNode extends TemplateNode {
    public AddNode() {
        super("Add", FunctionDirective.ADD, GLDataType.INT);

        addInputPin("x_pos", GLDataType.INT , GLDataType.FLOAT);
        addInputPin("y_pos", GLDataType.INT , GLDataType.FLOAT);

    }
}
