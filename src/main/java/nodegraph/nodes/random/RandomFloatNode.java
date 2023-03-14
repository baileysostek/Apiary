package nodegraph.nodes.random;

import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class RandomFloatNode extends TemplateNode {

    public RandomFloatNode() {
        super("random_float", FunctionDirective.RANDOM_FLOAT, GLDataType.FLOAT);
    }

}
