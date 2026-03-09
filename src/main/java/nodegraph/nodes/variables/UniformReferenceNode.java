package nodegraph.nodes.variables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import editor.Editor;
import editor.UniformManager;
import graphics.GLDataType;
import graphics.ShaderManager;
import graphics.Uniform;
import imgui.ImGui;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.flag.ImGuiComboFlags;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.pin.OutflowPin;

public class UniformReferenceNode extends Node {

    Uniform uniform_reference;

    OutflowPin output;

    public UniformReferenceNode(JsonObject initialization_data) {
        super(initialization_data);
        output = super.addOutputPin("output", GLDataType.INT);

        this.setWidth(128);

        this.disableFlowControls();
    }

    @Override
    public void onLoad(JsonObject initialization_data) {
        if(initialization_data.has("reference_uniform_name")){
            String uniform_name = initialization_data.get("reference_uniform_name").getAsString();
            this.setVariable(uniform_name);
        }
    }

    @Override
    public JsonObject serializeNode() {
        JsonObject node_specific_save_data = new JsonObject();
        if(uniform_reference != null){
            node_specific_save_data.addProperty("reference_uniform_name", uniform_reference.getName());
        }
        return node_specific_save_data;
    }

    @Override
    public void render() {
        // Render the drop down of all variables
        ImGui.pushItemWidth(this.width);
        String uniform_name = (uniform_reference != null) ? uniform_reference.getName() : "Select a Uniform";
        if (ImGui.beginCombo("##"+super.getID()+"_type", uniform_name, ImGuiComboFlags.None)){
            for (String registered_uniform_name : ShaderManager.getInstance().getRegisteredUniforms()) {

                boolean is_selected = uniform_name.equals(registered_uniform_name);
                if (ImGui.selectable(registered_uniform_name, is_selected)){
                    this.setVariable(registered_uniform_name);
                }
                if (is_selected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
        ImGui.popItemWidth();

        if(this.uniform_reference != null){
            super.renderOutputAttribute("output");
        }
    }

    private void setVariable(String registered_uniform_name){
        this.uniform_reference =  ShaderManager.getInstance().getUniform(registered_uniform_name);
        if(this.uniform_reference != null){
            output.setType(uniform_reference.getType());
        }else{
            output.setType(GLDataType.INT);
        }
        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.getTypeColor(output.getDataType()));
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        if (uniform_reference != null) {
            JsonArray reference = new JsonArray();
            reference.add(this.uniform_reference.getName());
            reference.add(FunctionDirective.GET.getNodeID());
            return reference;
        }
        return JsonNull.INSTANCE;
    }
}
