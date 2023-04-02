package nodegraph.nodes.vector;

import com.google.gson.JsonObject;
import graphics.GLDataType;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import nodegraph.NodeColors;

public class DecomposeVec2 extends DecomposeStructureNode{
    public DecomposeVec2(JsonObject initialization_data) {
        super(GLDataType.VEC2, new String[]{"x", "y"}, new GLDataType[]{GLDataType.FLOAT, GLDataType.FLOAT}, initialization_data);
        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.getTypeColor(GLDataType.FLOAT));
    }
}
