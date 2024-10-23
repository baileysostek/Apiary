package nodegraph.nodes.random;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import imgui.extension.imnodes.flag.ImNodesCol;
import nodegraph.NodeColors;
import nodegraph.nodes.TemplateNode;

public class RandomBoolNode extends TemplateNode {

    public RandomBoolNode(JsonObject initialization_data) {
        super("random_bool", FunctionDirective.RANDOM_BOOL, GLDataType.BOOL, initialization_data);
        this.applyStyle(ImNodesCol.TitleBar, NodeColors.getTypeColor(GLDataType.BOOL));
    }
}
