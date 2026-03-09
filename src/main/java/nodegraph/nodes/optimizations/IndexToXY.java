package nodegraph.nodes.optimizations;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.Node;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

public class IndexToXY extends Node {

    InflowPin index;

    OutflowPin x;
    OutflowPin y;

    public IndexToXY(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("Index to XY");

        // This node does not need to insert anything into the serialization
        super.disableFlowControls();

        index = super.addInputPin("index", GLDataType.INT);

        x = super.addOutputPin("x", GLDataType.INT);
        y = super.addOutputPin("y", GLDataType.INT);
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {

        if(outflow.equals(x)){
            JsonArray out = new JsonArray();

            out.add(index.getValue());
            out.add("window_width_pixels");
            out.add(FunctionDirective.GET.getNodeID());
            out.add(FunctionDirective.MOD_I.getNodeID());

            return out;
        }

        if(outflow.equals(y)){
            JsonArray out = new JsonArray();

            out.add(index.getValue());
            out.add("window_width_pixels");
            out.add(FunctionDirective.GET.getNodeID());
            out.add(FunctionDirective.DIV.getNodeID());
            out.add(FunctionDirective.FLOOR.getNodeID());
            out.add("int");
            out.add(FunctionDirective.CAST.getNodeID());

            return out;
        }

        return JsonNull.INSTANCE;
    }
}
