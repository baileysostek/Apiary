package nodes;

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
            super.input_values.put(param_name, null);
        }
        for(String output_name : node_type.getOutputNames()){
            this.output_values.put(output_name, null);
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



}
