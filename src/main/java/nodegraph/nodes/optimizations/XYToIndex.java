package nodegraph.nodes.optimizations;

import com.google.gson.*;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.Node;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

public class XYToIndex extends Node {

    InflowPin x;
    InflowPin y;

    OutflowPin index;

    public XYToIndex(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("XY  to index.");

        // This node does not need to insert anything into the serialization
        super.disableFlowControls();

        x = super.addInputPin("x", GLDataType.INT);
        y = super.addInputPin("y", GLDataType.INT);

        index = super.addOutputPin("index", GLDataType.INT);
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {

        if(outflow.equals(index)){
            JsonArray out = new JsonArray();

            out.add(x.getValue());
            out.add(y.getValue());
            out.add(FunctionDirective.XY_TO_SCREEN_INDEX.getNodeID());

            return out;
        }

        return JsonNull.INSTANCE;
    }
}
