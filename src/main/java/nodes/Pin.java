package nodes;

import com.google.gson.JsonElement;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesPinShape;

public class Pin {

    String name;
    JsonElement value;
    int shape = ImNodesPinShape.Circle;

}
