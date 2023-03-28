package nodegraph.nodes.pipeline;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import graphics.GLDataType;
import nodegraph.Node;
import nodegraph.nodes.data.Vec3Node;
import nodegraph.pin.OutflowPin;

public class FragmentLogicNode extends Node {

    public FragmentLogicNode(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("Fragment");

        super.addInputPin("fragment_color", GLDataType.VEC4);
    }

    @Override
    public void serialize(JsonArray evaluation_stack) {

    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return JsonNull.INSTANCE;
    }
}
