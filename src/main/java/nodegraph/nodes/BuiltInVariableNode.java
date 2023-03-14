package nodegraph.nodes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import compiler.FunctionDirective;
import graphics.GLDataType;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.pin.OutflowPin;

public class BuiltInVariableNode extends Node {
    String built_in_variable_name;

    public BuiltInVariableNode(String built_in, GLDataType type) {
        super.setTitle(built_in);
        this.built_in_variable_name = built_in;

        super.addOutputPin(built_in, type);

        // No flow controls
        super.disableFlowControls();

        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.PINK);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.WHITE);
    }

    @Override
    public void serialize(JsonArray evaluation_stack) {
        // Built in are not serialized to anything on the object stack. They dont do any evaluation they are just a value.
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        JsonArray out = new JsonArray();
        out.add(built_in_variable_name);
        out.add(FunctionDirective.GET.getNodeID());
        return out;
    }
}
