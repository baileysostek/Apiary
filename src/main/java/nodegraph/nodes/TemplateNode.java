package nodegraph.nodes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import compiler.FunctionDirective;
import editor.Editor;
import graphics.GLDataType;
import nodegraph.Node;
import nodegraph.nodes.agent.AgentNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

import java.util.Collection;

public abstract class TemplateNode extends Node {

    final String name;
    final FunctionDirective template;
    final GLDataType return_type;
    protected final OutflowPin out;

    //TODO this is limiting we need to let the return tybe be derrived.
    public TemplateNode(String name, FunctionDirective template, GLDataType return_type, JsonObject initialization_data) {
        super(initialization_data);

        this.name = name;
        this.template = template;
        this.return_type = return_type;

        // TODO outs?
        this.out = addOutputPin(name, return_type);

        super.setTitle(template.getNodeID());

        super.disableFlowControls();
        super.setWidth(128);

        // add back in any values which were modified to be non-default
    }

    @Override
    public void onLoad(JsonObject initialization_data) {
        if(initialization_data.has("non_default_values")){
            JsonObject non_default_values = initialization_data.get("non_default_values").getAsJsonObject();
            for(String inflow_name : non_default_values.keySet()){
                InflowPin inflow = (InflowPin) super.getPinFromName(inflow_name);
                inflow.setValue(non_default_values.get(inflow_name).getAsString());
            }
        }
    }

    @Override
    public JsonObject nodeSpecificSaveData() {
        JsonObject out = new JsonObject();
        JsonObject non_default_values = new JsonObject();
        for (InflowPin inflow : super.getNodeInflowPins()) {
            if(!inflow.isConnected() && inflow.hasNonDefaultValue()){
                non_default_values.add(inflow.getAttributeName(), inflow.getValue());
            }
        }
        if(non_default_values.size() > 0) {
            out.add("non_default_values", non_default_values);
        }
        return out;
    }

    /**
     * Here we are generating the VM code to be interpreted by our VM.
     * @return
     */
    @Override
    public JsonElement getValueOfPin(OutflowPin pin) {

        JsonElement function_directive = new JsonPrimitive(template.getNodeID());

        if (pin.equals(out)) {
            Collection<InflowPin> inflow_pins = super.getNodeInflowPins();
            if(inflow_pins.size() > 0) {
                JsonArray local_evaluation_stack = new JsonArray();

                for (InflowPin inflow : inflow_pins) {
                    local_evaluation_stack.add(inflow.getValue());
                }

                // Finally, we add the Directive
                local_evaluation_stack.add(function_directive);
                return local_evaluation_stack;
            }
        }

        return function_directive;
    }
}
