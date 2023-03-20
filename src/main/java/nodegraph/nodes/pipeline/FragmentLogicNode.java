package nodegraph.nodes.pipeline;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import nodegraph.Node;
import nodegraph.pin.OutflowPin;

public class FragmentLogicNode extends Node {

    public FragmentLogicNode(JsonObject initialization_data) {
        super(initialization_data);
    }

    @Override
    public void serialize(JsonArray evaluation_stack) {

    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return JsonNull.INSTANCE;
    }
}
