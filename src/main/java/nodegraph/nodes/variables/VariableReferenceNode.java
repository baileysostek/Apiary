package nodegraph.nodes.variables;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import editor.Editor;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.flag.ImGuiComboFlags;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.nodes.agent.AgentNode;
import nodegraph.pin.OutflowPin;

public class VariableReferenceNode extends Node {

    DefineNode variable_reference;
    OutflowPin output;

    public VariableReferenceNode(JsonObject initialization_data) {
        super(initialization_data);
        output = super.addOutputPin("output", GLDataType.INT);

        this.setWidth(128);

        this.disableFlowControls();
    }

    @Override
    public void onLoad(JsonObject initialization_data) {
        if(initialization_data.has("variable_reference")){
            Node node = Editor.getInstance().getNodeGraph().getNodeFromReference(initialization_data.get("variable_reference").getAsInt());
            if (node instanceof DefineNode) {
                this.setVariable((DefineNode) node);
            }
        }
    }

    @Override
    public JsonObject serializeNode() {
        JsonObject node_specific_save_data = new JsonObject();
        if(variable_reference != null){
            node_specific_save_data.addProperty("variable_reference", variable_reference.getReferenceID());
        }
        return node_specific_save_data;
    }

    @Override
    public void render() {
        // Render the drop down of all variables
        ImGui.pushItemWidth(this.width);
        String variable_name = (variable_reference != null) ? variable_reference.getVariableName() : "Select a Variable";
        if (ImGui.beginCombo("##"+super.getID()+"_type", variable_name, ImGuiComboFlags.None)){
            for (Node node : Editor.getInstance().getNodeGraph().getNodesOfType(DefineNode.class)) {
                DefineNode variable_node = (DefineNode) node;

                boolean is_selected = variable_node.getVariableName().equals(variable_name);
                if (ImGui.selectable(variable_node.getVariableName(), is_selected)){
                    this.setVariable(variable_node);
                }
                if (is_selected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
        ImGui.popItemWidth();

        if(this.variable_reference != null){
            super.renderOutputAttribute("output");
        }
    }

    private void setVariable(DefineNode variable_reference){
        this.variable_reference = variable_reference;
        if(this.variable_reference != null){
            output.setType(variable_reference.getVariableDataType());
        }else{
            output.setType(GLDataType.INT);
        }
        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.getTypeColor(output.getDataType()));
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        if (variable_reference != null) {
            return variable_reference.getOutputValue();
        }
        return JsonNull.INSTANCE;
    }
}
