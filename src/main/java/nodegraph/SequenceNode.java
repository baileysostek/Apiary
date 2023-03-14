package nodegraph;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import nodegraph.pin.OutflowPin;

public class SequenceNode extends Node{
    public SequenceNode() {
        this.setTitle("Sequence");

        // Force the inflow
        this.forceRenderInflow();

        for(int i = 0; i < 8; i++){
            super.addOutflowPin(String.format("step_%s", i+""));
        }
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return JsonNull.INSTANCE;
    }
}
