package nodes;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.ImGuiTextFilter;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.flag.ImGuiComboFlags;
import imgui.flag.ImGuiInputTextFlags;
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
                ImGui.pushItemWidth(128);
                String initial_name = attribute.attribute_name.get();
                if(ImGui.inputText("##"+attribute.id, attribute.attribute_name, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll)){
                    super.renameOutput(initial_name, attribute.attribute_name.get());
                }
                ImGui.popItemWidth();
                ImGui.sameLine();
                ImGui.pushItemWidth(128);
                if (ImGui.beginCombo("##"+attribute.id+"_type", attribute.type.getGLSL(), ImGuiComboFlags.None)){
                    for (GLDataType type : GLDataType.values()) {
                        boolean is_selected = attribute.type.equals(type);
                        if (ImGui.selectable(type.getGLSL(), is_selected)){
                            attribute.type = type;
                        }
                        if (is_selected) {
                            ImGui.setItemDefaultFocus();
                        }
                    }
                    ImGui.endCombo();
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

    @Override
    public JsonElement serialize() {
        JsonObject save_data = new JsonObject();

        JsonObject agent_data = new JsonObject();

        JsonObject attributes_data = new JsonObject();
        // Encode all of our attributes
        for(Attribute attribute : attributes){
            String attribute_name = attribute.attribute_name.get();

            // Create the data that goes in each attribute.
            JsonObject attribute_data = new JsonObject();

            // Add the datatype.
            attribute_data.add("type", new JsonPrimitive(attribute.type.getGLSL()));

            // Check if there is a link between this attribute and another node.
            Node connection = this.getLinkedNode(attribute_name);
            if(connection != null) {
                // If there is a connection serialize that connection.
                attributes_data.add("default_value", connection.serialize());
            }

            attributes_data.add(attribute_name, attribute_data);
        }
        // Attach our attributes to agent data.
        agent_data.add("attributes", attributes_data);
        // Add our data to the object
        save_data.add(this.title, agent_data);

        return save_data;
    }
}
