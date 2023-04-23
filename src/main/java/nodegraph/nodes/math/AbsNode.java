package nodegraph.nodes.math;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

public class AbsNode extends TemplateNode {

    GLDataType target_type = GLDataType.INT;

    public AbsNode(JsonObject initialization_data) {
        super("Absolute Value", FunctionDirective.ABS, GLDataType.INT, initialization_data);

        if(initialization_data.has("output_type")){
            target_type = GLDataType.valueOf(initialization_data.get("output_type").getAsString());
            out.setType(target_type);
        }

        InflowPin input = addInputPin("input", GLDataType.INT , GLDataType.FLOAT, GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);
        input.onConnect((OutflowPin other) -> {
            target_type = other.getDataType();
            out.setType(target_type);
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
