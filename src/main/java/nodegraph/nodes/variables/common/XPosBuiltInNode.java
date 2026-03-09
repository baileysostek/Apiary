package nodegraph.nodes.variables.common;

import com.google.gson.JsonObject;
import graphics.GLDataType;
import nodegraph.nodes.BuiltInVariableNode;

public class XPosBuiltInNode extends BuiltInVariableNode {
    public XPosBuiltInNode(JsonObject initialization_data) {
        super("x_pos", GLDataType.INT, initialization_data);
    }
}
