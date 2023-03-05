package nodes;

import imgui.ImColor;

public enum NodeColors {

    CODE_NODE_TITLE(ImColor.intToColor(81, 148, 204, 255)),
    CODE_NODE_TITLE_HOVER(ImColor.intToColor(81, 148, 204, 255)),

    AGENT_NODE_TITLE(ImColor.intToColor(47, 41, 99, 255)),
    AGENT_NODE_TITLE_HIGHLIGHT(ImColor.intToColor(81, 148, 204, 255)),


    ;

    public static final int WHITE = ImColor.intToColor(255, 255, 255, 255) ;

    protected int color;

    NodeColors(int color) {
        this.color = color;
    }

    public int getColor(){
        return color;
    }
}
