package nodegraph.nodes.vector;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

public class NormalizeNode extends TemplateNode {

    GLDataType target_type = GLDataType.VEC2;

    public NormalizeNode(JsonObject initialization_data) {
        super("Normalize", FunctionDirective.NORMALIZE, GLDataType.VEC2, initialization_data);

        if(initialization_data.has("output_type")){
            target_type = GLDataType.valueOf(initialization_data.get("output_type").getAsString());
            out.setType(target_type);
            System.out.println(target_type);
        }

        InflowPin vector = addInputPin("vector", GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);

        vector.onConnect((OutflowPin other) -> {
            target_type = other.getDataType();
            out.setType(target_type);
        });
    }

    @Override
    public JsonObject nodeSpecificSaveData() {
        JsonObject out = new JsonObject();
        out.addProperty("output_type",target_type.name());
        return out;
    }

    @Override
    public void render() {
        super.render();
    }
}
