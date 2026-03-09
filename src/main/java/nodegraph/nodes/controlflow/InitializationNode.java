package nodegraph.nodes.controlflow;

import com.google.gson.JsonObject;
import nodegraph.nodes.OutflowNode;

public class InitializationNode extends OutflowNode {
    public InitializationNode(JsonObject initialization_data) {
        super("Initialize", initialization_data);
    }
}
