package nodes;

import graphics.GLDataType;
import imgui.ImGui;
import imgui.ImGuiTextFilter;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class AgentNode extends Node{

    private LinkedList<Attribute> attributes = new LinkedList<>();

    public AgentNode(String agent_name){
        this();
        super.setTitle(agent_name);
    }

    public AgentNode() {
        super();
        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.AGENT_NODE_TITLE);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.AGENT_NODE_TITLE_HIGHLIGHT);
    }

    @Override
    public void render() {
        if(ImGui.button("Add Attribute")){
            String attribute_name = attributeNameAtIndex(attributes.size() + 1);
            attributes.addLast(new Attribute(attribute_name, attribute_name, GLDataType.FLOAT));
            super.output_values.put(attribute_name, null);
        }
        renderAttributes();
    }

    private String attributeNameAtIndex(int index){
        return String.format("Attribute_%s", index);
    }

    private void renderAttributes(){
        for(Attribute attribute : attributes){
            super.renderOutputAttribute(attribute.id, () -> {
                ImGui.pushItemWidth(256);
                String initial_name = attribute.attribute_name.get();
                if(ImGui.inputText("##"+attribute.id, attribute.attribute_name)){
                    super.renameOutput(initial_name, attribute.attribute_name.get());
                }
                ImGui.popItemWidth();
            });
        };
    }

    private class Attribute{
        String id;
        ImString attribute_name;
        GLDataType type;
        float[] value;

        public Attribute(String id, String attribute_name, GLDataType type) {
            this.id = id;
            this.attribute_name = new ImString(attribute_name);
            this.type = type;
            this.value = new float[type.getSizeInFloats()];
            Arrays.fill(value, 0);
        }
    }

}
