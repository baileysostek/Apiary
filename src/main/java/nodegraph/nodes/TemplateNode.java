package nodegraph.nodes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.Node;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

import java.util.Collection;

public class TemplateNode extends Node {

    final String name;
    final FunctionDirective template;
    final GLDataType return_type;
    final OutflowPin out;

    //TODO this is limiting we need to let the return tybe be derrived.
    public TemplateNode(String name, FunctionDirective template, GLDataType return_type) {
        this.name = name;
        this.template = template;
        this.return_type = return_type;

        // TODO outs?
        this.out = addOutputPin(name, return_type);

        super.setTitle(template.getNodeID());

        super.disableFlowControls();
        super.setWidth(128);
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
                    OutflowPin inflow_source = super.getPinSource(inflow);
                    if (inflow_source != null) {
                        if (inflow_source.isConnected()) {
                            JsonElement element = inflow_source.getValue();
                            local_evaluation_stack.add(element);
                        }
                    } else {
                        local_evaluation_stack.add(JsonNull.INSTANCE);
                    }
                }

                // Finally, we add the Directive
                local_evaluation_stack.add(function_directive);
                return local_evaluation_stack;
            }
        }

        return function_directive;
    }
}
