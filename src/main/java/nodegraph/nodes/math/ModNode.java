package nodegraph.nodes.math;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

public class ModNode extends TemplateNode {

    GLDataType target_type = GLDataType.INT;

    public ModNode(JsonObject initialization_data) {
        super("mod", FunctionDirective.MOD, GLDataType.FLOAT, initialization_data);

        // Still need to add our input values.
        InflowPin input_x = addInputPin("A", GLDataType.INT, GLDataType.FLOAT, GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);
        InflowPin input_y = addInputPin("B", GLDataType.INT, GLDataType.FLOAT, GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);

        if(initialization_data.has("output_type")){
            target_type = GLDataType.valueOf(initialization_data.get("output_type").getAsString());
            out.setType(target_type);
        }

        input_x.onConnect((OutflowPin other) -> {
            if(!input_y.isConnected()){
                target_type = other.getDataType();
                out.setType(target_type);
            }
        });

        input_y.onConnect((OutflowPin other) -> {
            if(!input_x.isConnected()){
                target_type = other.getDataType();
                out.setType(target_type);
            }
        });
    }

    @Override
    public JsonObject serializeNode() {
        JsonObject out = new JsonObject();
        out.addProperty("output_type",target_type.name());
        return out;
    }

    @Override
    public void render() {
        super.render();
    }
}
