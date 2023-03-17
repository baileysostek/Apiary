package nodegraph.nodes.controlflow;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.Node;
import nodegraph.nodes.OutflowNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

public class ConditionalNode extends Node {

    InflowPin predicate;

    OutflowPin consequent;
    OutflowPin alternate;

    public ConditionalNode() {
        super.setTitle("if");

        super.forceRenderInflow();
        super.disableOutflowControls();

        // We handle the connection traversal.
        super.setNodeProcessesOwnFlow(true);

        predicate = super.addInputPin("condition", GLDataType.BOOL);

        consequent = super.addOutflowPin("then");
        alternate  = super.addOutflowPin("else");
    }

    @Override
    public void serialize(JsonArray evaluation_stack) {
        JsonArray conditional_logic = new JsonArray();

        // Add conditional
        if(!predicate.isConnected()){
            return; // No logic
        }
        JsonElement predicate_logic = predicate.getValue();

        JsonElement consequent_logic = JsonNull.INSTANCE;
        if(consequent.isConnected()){
            consequent_logic = new JsonArray();
            consequent.getConnection().getParent().generateIntermediate((JsonArray) consequent_logic);
        }
        JsonElement alternate_logic = JsonNull.INSTANCE;
        if(alternate.isConnected()){
            alternate_logic = new JsonArray();
            alternate.getConnection().getParent().generateIntermediate((JsonArray) alternate_logic);
        }

        JsonElement function_directive = new JsonPrimitive(FunctionDirective.CONDITIONAL.getNodeID());

        conditional_logic.add(predicate_logic);
        conditional_logic.add(consequent_logic);
        conditional_logic.add(alternate_logic);
        conditional_logic.add(function_directive);

        evaluation_stack.add(conditional_logic);
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return null;
    }
}
