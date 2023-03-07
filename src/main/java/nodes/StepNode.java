package nodes;

import com.google.gson.JsonPrimitive;
import graphics.GLDataType;
import imgui.extension.imnodes.flag.ImNodesColorStyle;

public class StepNode extends Node{

    public StepNode() {

        this.setTitle("Step");

        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.AGENT_NODE_TITLE);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.AGENT_NODE_TITLE_HIGHLIGHT);

        for(GLDataType type : GLDataType.values()){
            super.addOutputAttribute(type.name(), type);
        }
    }

}
