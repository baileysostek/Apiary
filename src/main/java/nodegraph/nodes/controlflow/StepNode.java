package nodegraph.nodes.controlflow;

import com.google.gson.JsonObject;
import nodegraph.nodes.OutflowNode;

public class StepNode extends OutflowNode {
    public StepNode(JsonObject initialization_data) {
        super("Step", initialization_data);
    }
}
