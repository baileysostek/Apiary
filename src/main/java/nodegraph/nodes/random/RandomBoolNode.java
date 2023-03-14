package nodegraph.nodes.random;

import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class RandomBoolNode extends TemplateNode {

    public RandomBoolNode() {
        super("random_bool", FunctionDirective.RANDOM_BOOL, GLDataType.BOOL);
    }
}
