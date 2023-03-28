package nodegraph.nodes.pipeline;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.Node;
import nodegraph.pin.OutflowPin;

public class FragmentLogicNode extends Node {

    public FragmentLogicNode(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("Fragment");

        super.addInputPin("fragment_color", GLDataType.VEC4);
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return outflow.getValue();
    }

    @Override
    public void serialize (JsonArray evaluation_stack) {
        evaluation_stack.add("out_color");
        super.serialize(evaluation_stack);
        evaluation_stack.add(FunctionDirective.SET.getNodeID());
    }
}
