package nodegraph.nodes.simulation;

import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class LinearizeNode extends TemplateNode {

    public LinearizeNode() {
        super("Linearize", FunctionDirective.XY_TO_SCREEN_INDEX, GLDataType.INT);

        addInputPin("x_pos", GLDataType.INT);
        addInputPin("y_pos", GLDataType.INT);

    }
}
