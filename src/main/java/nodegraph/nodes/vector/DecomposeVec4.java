package nodegraph.nodes.vector;

import com.google.gson.JsonObject;
import graphics.GLDataType;
import imgui.extension.imnodes.flag.ImNodesCol;
import nodegraph.NodeColors;

public class DecomposeVec4 extends DecomposeStructureNode{
    public DecomposeVec4(JsonObject initialization_data) {
        super(GLDataType.VEC4, new String[]{"x", "y", "z", "w"}, new GLDataType[]{GLDataType.FLOAT, GLDataType.FLOAT, GLDataType.FLOAT, GLDataType.FLOAT}, initialization_data);
        this.applyStyle(ImNodesCol.TitleBar, NodeColors.getTypeColor(GLDataType.FLOAT));
    }
}
