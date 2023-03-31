package nodegraph.nodes.vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.Node;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

public abstract class DecomposeStructureNode extends Node {

    InflowPin input_vector;

    OutflowPin[] outputs;


    public DecomposeStructureNode(GLDataType input, String[] components, GLDataType[] types, JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle(String.format("Decompose %s", input.name()));
        super.disableFlowControls();

        input_vector = addInputPin("input", input);
        outputs = new OutflowPin[components.length];

        // Still need to add our input values.
        for(int i = 0; i < components.length; i++){
            outputs[i] = addOutputPin(components[i], types[i]);
        }
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        JsonArray out = new JsonArray();
        for(OutflowPin outflow_pin : this.outputs){
            if(outflow_pin == outflow){
                out.add(input_vector.getValue());
                out.add(outflow_pin.getAttributeName());
                out.add(FunctionDirective.REF.getNodeID());
            }
        }
        return out;
    }
}
