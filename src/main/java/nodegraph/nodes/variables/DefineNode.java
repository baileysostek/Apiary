package nodegraph.nodes.variables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import compiler.FunctionDirective;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.flag.ImGuiComboFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import nodegraph.Node;
import nodegraph.nodes.agent.AgentNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

import java.util.Collection;

public class DefineNode extends Node {

    private ImString variable_name = new ImString();
    private ImString variable_value = new ImString();

    private GLDataType type = GLDataType.VEC3;
    private float[] value;


    public DefineNode() {
        super();

        this.setTitle("Variable Definition");

        super.addOutputPin(variable_name.get(), type);

        super.forceRenderInflow();
        super.forceRenderOutflow();
    }

    @Override
    public void render() {
        super.renderOutputAttribute(variable_name.get(), () -> {
            ImGui.pushItemWidth(128);
            String initial_name = variable_name.get();
            if(ImGui.inputText("##"+super.getID(), variable_name, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll)){
                String new_name = variable_name.get();
                super.renameAttribute(initial_name, new_name);
            }
            ImGui.popItemWidth();
            ImGui.sameLine();
            ImGui.pushItemWidth(128);
            if (ImGui.beginCombo("##"+super.getID()+"_type", type.getGLSL(), ImGuiComboFlags.None)){
                for (GLDataType type : GLDataType.values()) {
                    boolean is_selected = type.equals(type);
                    if (ImGui.selectable(type.getGLSL(), is_selected)){
                        this.type = type;
                        super.addOutputPin(variable_name.get(), type);
                    }
                    if (is_selected) {
                        ImGui.setItemDefaultFocus();
                    }
                }
                ImGui.endCombo();
            }
            ImGui.popItemWidth();
            ImGui.sameLine();
            ImGui.pushItemWidth(128);
            if(ImGui.inputText("##"+super.getID()+"_value", variable_value, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll)){

            }
            ImGui.popItemWidth();
        });
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
        return new JsonPrimitive(variable_name.get());
    }
}
