package nodegraph.nodes.math;

import com.google.gson.JsonElement;
import nodegraph.Node;
import nodegraph.pin.OutflowPin;

public class IncrementNode extends Node {

    public IncrementNode() {
        this.forceRenderInflow();
        this.forceRenderOutflow();
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return null;
    }

}
