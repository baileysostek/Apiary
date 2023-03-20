package nodegraph.nodes.variables.common;

import com.google.gson.JsonObject;
import graphics.GLDataType;
import nodegraph.nodes.BuiltInVariableNode;

public class YPosBuiltInNode extends BuiltInVariableNode {
    public YPosBuiltInNode(JsonObject initialization_data) {
        super("y_pos", GLDataType.INT, initialization_data);
    }
}
