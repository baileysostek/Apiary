package nodegraph;

import graphics.GLDataType;
import imgui.ImColor;

import java.util.HashMap;
import java.util.Map;

public enum NodeColors {

    CODE_NODE_TITLE(ImColor.rgba(81, 148, 204, 255)),
    CODE_NODE_TITLE_HOVER(ImColor.rgba(81, 148, 204, 255)),

    AGENT_NODE_TITLE(ImColor.rgba(47, 41, 99, 255)),
    AGENT_NODE_TITLE_HIGHLIGHT(ImColor.rgba(81, 148, 204, 255)),


    ;

    // Constants defined here
    public static final int RED     = ImColor.rgba(255,  0, 0, 255);
    public static final int ORANGE  = ImColor.rgba(255,  165, 0, 255);
    public static final int YELLOW  = ImColor.rgba(255,  255, 0, 255);
    public static final int GREEN   = ImColor.rgba(0,  128, 0, 255);
    public static final int BLUE    = ImColor.rgba(0,  0, 255, 255);
    public static final int CYAN    = ImColor.rgba(0,  255, 255, 255);
    public static final int MAGENTA = ImColor.rgba(255,  0, 255, 255);
    public static final int WHITE   = ImColor.rgba(255, 255, 255, 255);
    public static final int BLACK   = ImColor.rgba(0,  0, 0, 255);
    public static final int PINK    = ImColor.rgba(255, 200, 255, 255);
    public static final int GREY    = ImColor.rgba(128, 128, 128, 255);
    public static final int BROWN   = ImColor.rgba(165, 42, 42, 255);

    // Define a Map for our primitive types
    private static final Map<GLDataType, Integer> TYPE_COLORS = new HashMap<>();
    public static final int getTypeColor(GLDataType type){
        return TYPE_COLORS.getOrDefault(type , WHITE);
    }
    static {
        for (GLDataType type : GLDataType.values()) {
            int color = WHITE;
            switch (type) {
                case INT:
                    color = RED;
                    break;
                case FLOAT:
                    color = ORANGE;
                    break;
                case VEC2:
                    color = YELLOW;
                    break;
                case VEC3:
                    color = GREEN;
                    break;
                case VEC4:
                    color = BLUE;
                    break;
                case SAMPLER_CUBE:
                    color = MAGENTA;
                    break;
                case SAMPLER_2D:
                    color = PINK;
                    break;
                case SAMPLER_3D:
                    color = WHITE;
                    break;
                case MAT2:
                    color = BLACK;
                    break;
                case MAT3:
                    color = GREY;
                    break;
                case MAT4:
                    color = CYAN;
                    break;
                case BOOL:
                    color = BROWN;
                    break;
            }
            // Append to our map
            TYPE_COLORS.put(type, color);
        }
    }

    protected int color;

    NodeColors(int color) {
        this.color = color;
    }

    public int getColor(){
        return color;
    }
}
