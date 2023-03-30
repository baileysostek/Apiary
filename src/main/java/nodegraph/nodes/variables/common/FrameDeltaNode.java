package nodegraph.nodes.variables.common;

import com.google.gson.JsonObject;
import graphics.GLDataType;
import nodegraph.nodes.BuiltInVariableNode;

public class FrameDeltaNode extends BuiltInVariableNode {
    public FrameDeltaNode(JsonObject initialization_data) {
        super("u_time_delta", GLDataType.FLOAT, initialization_data);
    }
}
