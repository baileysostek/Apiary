package nodes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

public class TemplateNode extends Node{

    private final NodeTemplates node_type;

    public TemplateNode(NodeTemplates node_type) {
        super();

        this.node_type = node_type;

        this.title = node_type.getNodeID();

        // Populate our parameter map based off our our template.
        for(String param_name : node_type.getInputNames()){
            super.addInputAttribute(param_name, GLDataType.FLOAT);
        }
        for(String output_name : node_type.getOutputNames()){
            super.addOutputAttribute(output_name, GLDataType.VEC2);
        }

    }

    @Override
    public void render(){
        // Render the input nodes.
        int index = 0;
        Object[] out_names = this.node_type.getOutputNames().toArray();
        for(String input_name : this.node_type.getInputNames()){
            super.renderInputAttribute(input_name);
            if(index < this.node_type.getOutputNames().size()){
                String out_name = (String) out_names[index];
                ImGui.sameLine();
                super.renderOutputAttribute(out_name);
            }
            index++;
        }
        // Render the output nodes.
        int output_index = 0;
        for(String output_name : this.node_type.getOutputNames()){
            if(output_index >= index) {
                super.renderOutputAttribute(output_name);
            }
            output_index++;
        }
    }

    // To IR
    @Override
    public JsonElement serialize() {
        JsonElement[] inputs = new JsonElement[this.getInputNames().size()];
        int index = 0;
        for(String param_name : this.getInputNames()){
            inputs[index] = super.getInputValue(param_name);
            index++;
        }
        JsonElement[] outputs = new JsonElement[this.getOutputNames().size()];
        index = 0;
        for(String output_name : getOutputNames()){
            outputs[index] = super.getOutputValue(output_name);
            index++;
        }
        // Create our out array
        int capacity = inputs.length + outputs.length + 1; // The +1 is for the name of this node it ALWAYS comes last
        JsonArray output = new JsonArray(capacity);
        for(int i = 0; i < capacity; i++){
            if(i < inputs.length) {
                output.add(inputs[i]);
            }else{
                if(i - outputs.length < outputs.length){
                    output.add(inputs[i]);
                }
            }
        }
        // This appends the call directive to the json array after the inputs and outputs.
        // Example for ADD
        // in: A, B Out: NONE  Directive: @add
        // [A, B, "@add"]
        output.add(this.node_type.getNodeID());

        return output;
    }
}
