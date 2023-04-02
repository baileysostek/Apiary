package nodegraph.nodes.variables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import editor.Editor;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.flag.ImGuiComboFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import nodegraph.Node;
import nodegraph.nodes.agent.AgentNode;
import nodegraph.pin.OutflowPin;

public class DefineNode extends Node {

    private ImString variable_name = new ImString();
    private ImString variable_value = new ImString();

    private OutflowPin output;

    private GLDataType type = GLDataType.VEC3;

    public DefineNode(JsonObject initialization_data) {
        super(initialization_data);

        this.setTitle("Variable Definition");

        output = super.addOutputPin("output", type);

        // Small node
        this.setWidth(128);

        super.forceRenderInflow();
        super.forceRenderOutflow();
    }

    @Override
    public void onLoad(JsonObject initialization_data) {
        if(initialization_data.has("variable_name")){
            this.variable_name.set(initialization_data.get("variable_name").getAsString());
        }
        if(initialization_data.has("variable_type")){
            this.setType(GLDataType.valueOf(initialization_data.get("variable_type").getAsString()));
        }
        if(initialization_data.has("variable_value")){
            this.variable_value.set(initialization_data.get("variable_value").getAsString());
        }
    }

    @Override
    public JsonObject nodeSpecificSaveData() {
        JsonObject out = new JsonObject();
        out.addProperty("variable_name", this.variable_name.get());
        out.addProperty("variable_type", this.type.name());
        out.addProperty("variable_value", this.variable_value.get());
        return out;
    }

    @Override
    public void render() {
        ImGui.pushItemWidth(this.width);
        String initial_name = variable_name.get();
        if(ImGui.inputText("##"+super.getID(), variable_name, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll)){
            String new_name = variable_name.get();
            super.renameAttribute(initial_name, new_name);
        }
        ImGui.popItemWidth();
        ImGui.pushItemWidth(this.width);
        if (ImGui.beginCombo("##"+super.getID()+"_type", type.getGLSL(), ImGuiComboFlags.None)){
            for (GLDataType type : GLDataType.values()) {
                boolean is_selected = type.equals(type);
                if (ImGui.selectable(type.getGLSL(), is_selected)){
                    this.setType(type);
                }
                if (is_selected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
        ImGui.popItemWidth();
        ImGui.pushItemWidth(this.width);
        if(ImGui.inputText("##"+super.getID()+"_value", variable_value, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll)){

        }
        ImGui.popItemWidth();

        super.renderOutputAttribute(output.getAttributeName());
    }

    private void setType(GLDataType type){
        this.type = type;
        output.setType(type);
    }

    @Override
    public void serialize(JsonArray evaluation_stack) {
        JsonArray define_variable = new JsonArray();

        define_variable.add(type.getGLSL());       // Type
        define_variable.add(variable_name.get());  // Name
        define_variable.add(variable_value.get()); // Value
        define_variable.add(FunctionDirective.DEFINE.getNodeID());

        evaluation_stack.add(define_variable);
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        JsonArray out_elements = new JsonArray();
        out_elements.add(variable_name.get());
        out_elements.add(FunctionDirective.GET.getNodeID());
        return out_elements;
    }

    public String getVariableName(){
        return variable_name.get();
    }

    public GLDataType getVariableDataType(){
        return this.type;
    }
}
