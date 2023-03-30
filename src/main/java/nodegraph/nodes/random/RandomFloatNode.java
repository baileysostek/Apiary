package nodegraph.nodes.random;

import com.google.gson.*;
import compiler.FunctionDirective;
import editor.Editor;
import graphics.GLDataType;
import nodegraph.Node;
import nodegraph.nodes.agent.AgentNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

import java.util.Collection;

public class RandomFloatNode extends Node {

    private final OutflowPin out;
    private final float cpu_random;

    //TODO this is limiting we need to let the return tybe be derrived.
    public RandomFloatNode(JsonObject initialization_data) {
        super(initialization_data);

        cpu_random = (float) Math.random();

        // TODO outs?
        this.out = addOutputPin("random_float", GLDataType.FLOAT);

        super.setTitle("Random Float");

        super.disableFlowControls();
        super.setWidth(128);

        // add back in any values which were modified to be non-default
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin pin) {
        JsonArray out = new JsonArray();
        out.add(new JsonPrimitive(this.cpu_random + ""));
        out.add(FunctionDirective.RANDOM_FLOAT.getNodeID());
        return out;
    }
}
