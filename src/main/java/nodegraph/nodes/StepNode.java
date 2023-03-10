package nodegraph.nodes;

import graphics.GLDataType;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import nodegraph.Node;
import nodegraph.NodeColors;

public class StepNode extends Node {

    public StepNode() {

        this.setTitle("Step");

        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.AGENT_NODE_TITLE);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.AGENT_NODE_TITLE_HIGHLIGHT);

        this.addOutputAttribute("instance", GLDataType.INT);
    }

}
