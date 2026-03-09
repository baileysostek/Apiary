package nodegraph.nodes.math;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

public class MixNode extends TemplateNode {

    GLDataType target_type = GLDataType.INT;

    public MixNode(JsonObject initialization_data) {
        super("Mix", FunctionDirective.MIX, GLDataType.INT, initialization_data);

        if(initialization_data.has("output_type")){
            target_type = GLDataType.valueOf(initialization_data.get("output_type").getAsString());
            out.setType(target_type);
        }

        InflowPin input_a = addInputPin("A", GLDataType.INT , GLDataType.FLOAT, GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);
        InflowPin input_b = addInputPin("B", GLDataType.INT , GLDataType.FLOAT, GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);
        InflowPin input_theta = addInputPin("Theta", GLDataType.FLOAT);


        input_a.onConnect((OutflowPin other) -> {
            if(!input_b.isConnected()){
                target_type = other.getDataType();
                input_b.setType(target_type);
                out.setType(target_type);
            }
        });
        input_b.onConnect((OutflowPin other) -> {
            if(!input_a.isConnected()){
                target_type = other.getDataType();
                input_a.setType(target_type);
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
