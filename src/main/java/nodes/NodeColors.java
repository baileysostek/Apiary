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
    public static final int PINK = ImColor.rgba(255, 200, 255, 255);

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
                    color = 0xFF000000;
                    break;
                case FLOAT:
                    color = 0xFFFF0000;
                    break;
                case VEC2:
                    color = 0xFF00FF00;
                    break;
                case VEC3:
                    color = 0xFFFFFF00;
                    break;
                case VEC4:
                    color = 0xFF0000FF;
                    break;
                case SAMPLER_CUBE:
                    color = 0xFFFF00FF;
                    break;
                case SAMPLER_2D:
                    color = 0xFF00FFFF;
                    break;
                case SAMPLER_3D:
                    color = 0xFFFFFFFF;
                    break;
                case MAT2:
                    color = 0xFF0F0F0F;
                    break;
                case MAT3:
                    color = 0xFFFF0F0F;
                    break;
                case MAT4:
                    color = 0xFF0FFF0F;
                    break;
                case BOOL:
                    color = 0xFFFFFF0F;
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
