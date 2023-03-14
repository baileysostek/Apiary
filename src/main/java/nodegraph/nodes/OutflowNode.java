package nodegraph.nodes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import graphics.GLDataType;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.pin.OutflowPin;

public class OutflowNode extends Node {

    private static String INSTANCE = "instance";

    OutflowPin instance_id;

    public OutflowNode(String title) {

        this.setTitle(title);

        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.RED);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.RED);

        instance_id = this.addOutputPin(INSTANCE, GLDataType.INT);
    }

    @Override
    public void serialize(JsonArray evaluation_stack) {

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
