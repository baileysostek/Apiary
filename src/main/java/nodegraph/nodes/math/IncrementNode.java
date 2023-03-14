package nodegraph.nodes.math;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import compiler.FunctionDirective;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import nodegraph.Node;
import nodegraph.pin.OutflowPin;

public class IncrementNode extends Node {

    private ImString variable_name = new ImString("variable");

    public IncrementNode() {

        super.setTitle("++");

        this.forceRenderInflow();
        this.forceRenderOutflow();

        this.setWidth(128);
    }

    @Override
    public void render() {
        ImGui.setNextItemWidth(super.width);
        if(ImGui.inputText("##"+super.getID(), variable_name, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll)){

        }
    }

    @Override
    public void serialize(JsonArray evaluation_stack) {
        JsonArray define_variable = new JsonArray();

        define_variable.add(variable_name.get());       // Type
        define_variable.add(1);  // Name
        define_variable.add(FunctionDirective.INCREMENT.getNodeID());

        evaluation_stack.add(define_variable);
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return null;
    }

}
