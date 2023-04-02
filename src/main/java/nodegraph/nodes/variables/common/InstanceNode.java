package nodegraph.nodes.variables.common;

import com.google.gson.JsonObject;
import graphics.GLDataType;
import nodegraph.nodes.BuiltInVariableNode;

public class InstanceNode extends BuiltInVariableNode {
    public InstanceNode(JsonObject initialization_data) {
        super("instance", GLDataType.INT, initialization_data);
    }
}
