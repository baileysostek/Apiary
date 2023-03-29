package nodegraph.nodes.variables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import editor.Editor;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.flag.ImGuiComboFlags;
import nodegraph.Node;
import nodegraph.nodes.variables.DefineNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

import java.util.Collection;

public class IncrementNode extends Node {

    private DefineNode reference;

    private InflowPin value;

    public IncrementNode(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("Increment Value");

        this.forceRenderInflow();
        this.forceRenderOutflow();

        this.setWidth(128);

        // We dont know the data type yet.
        value = super.addInputPin("value", GLDataType.INT);
    }

    @Override
    public void render() {
        ImGui.setNextItemWidth(super.width);

        String variable_name = (reference == null) ? "Select a Variable" : reference.getVariableName();

        Collection<Node> variables = Editor.getInstance().getNodeGraph().getNodesOfType(DefineNode.class);

        if (ImGui.beginCombo("##"+super.getID()+"_var", variable_name, ImGuiComboFlags.None)){
            for (Node node : variables) {
                DefineNode define_node = (DefineNode) node;
                if(define_node.getVariableName().isEmpty()){
                    continue; // If the variable does not have a name we will skip over it.
                }

                boolean is_selected = reference != null && reference.equals(define_node);
                if (ImGui.selectable(define_node.getVariableName(), is_selected)){
                    reference = define_node;
                }
                if (is_selected) {
                    ImGui.setItemDefaultFocus();
                }
            }

//            if (ImGui.selectable("Select a Variable", reference == null)){
//                attribute.setType(type);
//            }
//            if (is_selected) {
//                ImGui.setItemDefaultFocus();
//            }
            ImGui.endCombo();
        }

        if(reference != null){
            value.setType(reference.getVariableDataType());
        }

        // Render the value
        super.renderInputAttribute(value.getAttributeName());
    }

    @Override
    public void serialize(JsonArray evaluation_stack) {
        JsonArray define_variable = new JsonArray();

        define_variable.add(reference.getVariableName());// Type
        define_variable.add(value.getValue());  // Name
        define_variable.add(FunctionDirective.INCREMENT.getNodeID());

        evaluation_stack.add(define_variable);
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return null;
    }

}
