package nodegraph.nodes;

import com.google.gson.*;
import graphics.GLDataType;
import imgui.extension.imnodes.flag.ImNodesCol;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.pin.OutflowPin;

public class OutflowNode extends Node {

    private static String INSTANCE = "instance";

    OutflowPin instance_id;

    public OutflowNode(String title, JsonObject initialization_data) {
        super(initialization_data);

        this.setTitle(title);

        this.applyStyle(ImNodesCol.TitleBar, NodeColors.RED);
        this.applyStyle(ImNodesCol.TitleBarHovered, NodeColors.RED);

        instance_id = this.addOutputPin(INSTANCE, GLDataType.INT);
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {

        // If we are asking for the Inflow
        if(outflow.equals(instance_id)){
            return new JsonPrimitive(INSTANCE);
        }

        return JsonNull.INSTANCE;
    }
}
