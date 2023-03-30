package nodegraph.nodes.optimizations;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import graphics.GLDataType;
import nodegraph.Node;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

public class ScreenPosToIndex extends Node {

    InflowPin screen_pos;
    OutflowPin index;

    public ScreenPosToIndex(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("Screen position to index.");

        // This node does not need to insert anything into the serialization
        super.disableFlowControls();

        screen_pos = super.addInputPin("Screen Position", GLDataType.VEC2);
        index = super.addOutputPin("index", GLDataType.INT);
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {

        if(outflow.equals(index)){
            JsonElement vec_2_reference;
            if(screen_pos.hasNonDefaultValue()){
                vec_2_reference = screen_pos.getValue();
            }else{
                vec_2_reference = new JsonPrimitive("vec2(0.0,0.0)");
            }

            JsonArray screen_pos_to_index = new JsonArray();
            // Get a reference to the X component
            screen_pos_to_index.add(vec_2_reference);
            String[] x_to_index = new String[]{
                    "x", "@ref", "1.0", "@add", "2.0", "@div", "u_window_size.x", "@get", "@mul"
            };
            for(String data : x_to_index) {
                screen_pos_to_index.add(data);
            }

            // Get a reference to the Y component
            screen_pos_to_index.add(vec_2_reference);
            String[] y_to_index = new String[]{
                    "y", "@ref", "1.0", "@add", "2.0", "@div", "u_window_size.y", "@get", "@mul"
            };
            for(String data : y_to_index) {
                screen_pos_to_index.add(data);
            }
            screen_pos_to_index.add("@xy_to_screen_index");

            return screen_pos_to_index;
        }
        return null;
    }
}
