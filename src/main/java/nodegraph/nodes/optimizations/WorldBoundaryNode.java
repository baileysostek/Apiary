package nodegraph.nodes.optimizations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graphics.GLDataType;
import nodegraph.Node;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

public class WorldBoundaryNode extends Node {

    GLDataType target_type_pos = GLDataType.VEC2;
    OutflowPin out_pos;

    GLDataType target_type_vel = GLDataType.VEC2;
    OutflowPin out_vel;

    // The mode we are operating in.
    private WorldMode mode = WorldMode.WRAP;

    public WorldBoundaryNode(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("World Boundary");

        // Position
        out_pos = super.addOutputPin("out pos", target_type_pos);
        if(initialization_data.has("pos_type")){
            setPosType(GLDataType.valueOf(initialization_data.get("pos_type").getAsString()));
        }
        // Smart outflow with variable datatype
        InflowPin input_pos = addInputPin("input pos", GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);
        input_pos.onConnect((OutflowPin other) -> {
            setPosType(other.getDataType());
        });

        // Velocity
        out_vel = super.addOutputPin("out vel", target_type_vel);
        if(initialization_data.has("vel_type")){
            setVelType(GLDataType.valueOf(initialization_data.get("vel_type").getAsString()));
        }
        // Smart outflow with variable datatype
        InflowPin input_vel = addInputPin("input vel", GLDataType.VEC2, GLDataType.VEC3, GLDataType.VEC4);
        input_vel.onConnect((OutflowPin other) -> {
            setVelType(other.getDataType());
        });

        // Delta time

        // Mode

    }

    private void setPosType(GLDataType type){
        target_type_pos = type;
        out_pos.setType(target_type_pos);
    }

    private void setVelType(GLDataType type){
        target_type_pos = type;
        out_vel.setType(target_type_pos);
    }

    @Override
    public JsonObject serializeNode() {
        JsonObject out = new JsonObject();
        out.addProperty("pos_type", target_type_pos.name());
        out.addProperty("vel_type", target_type_vel.name());
        return out;
    }

    @Override
    public void render() {
        // Render all of our inputs and outputs
        super.render();

        // Render the dropdown of increment behaviors

    }
    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        // BOUNCE Behavior
        return null;
    }

    private enum WorldMode{
        WRAP("Wrap"),
        BOUNCE("Bounce"),
        RANDOM_VEL("Randomize Velocity");

        String mode_name;
        WorldMode(String mode_name){
            this.mode_name = mode_name;
        }
    }
}
