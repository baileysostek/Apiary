package nodes;

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
    public static final int WHITE = ImColor.rgba(255, 255, 255, 255) ;
    public static final int BLUE = ImColor.rgba(0,  0, 255, 255) ;

    // Define a Map for our primative types
    private static final Map<GLDataType, Integer> TYPE_COLORS = new HashMap<>();
    public static final int getTypeColor(GLDataType type){
        return TYPE_COLORS.getOrDefault(type , WHITE);
    }
    static {
        for (GLDataType type : GLDataType.values()) {
            int color = WHITE;
            switch (type) {
                case INT:
                    color = BLUE;
                    break;
                case FLOAT:
                    color = 0xFF0000FF;
                    break;
                case VEC2:
                    break;
                case VEC3:
                    break;
                case VEC4:
                    break;
                case SAMPLER_CUBE:
                    break;
                case SAMPLER_2D:
                    break;
                case SAMPLER_3D:
                    break;
                case MAT2:
                    break;
                case MAT3:
                    break;
                case MAT4:
                    break;
                case BOOL:
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
