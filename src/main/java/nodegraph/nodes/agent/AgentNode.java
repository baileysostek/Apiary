package nodegraph.nodes.agent;

import com.google.gson.*;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.flag.ImGuiComboFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

import java.util.Arrays;
import java.util.LinkedList;

public class AgentNode extends Node {

    private LinkedList<Attribute> attributes = new LinkedList<>();
    private final LinkedList<Attribute> to_remove = new LinkedList<>();
    private final LinkedList<Attribute> buffer = new LinkedList<>();

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
            String attribute_name = attributeNameAtIndex((int) System.currentTimeMillis());
            addInputPin(attribute_name, GLDataType.INT);
        }
        renderAttributes();
    }

    @Override
    public InflowPin addInputPin(String attribute_name, GLDataType... accepted_types) {
        Attribute new_attribute = new Attribute(attribute_name, attribute_name, (GLDataType) Arrays.stream(accepted_types).toArray()[0]);
        attributes.addLast(new_attribute);
        return super.addInputPin(attribute_name, new_attribute.type);
    }

    private String attributeNameAtIndex(int index){
        return String.format("Attribute_%s", index);
    }

    private void renderAttributes(){
        // Remove any enqueued attributes
        for(Attribute attribute : to_remove){
            removeAttribute(attribute);
        }
        to_remove.clear();

        // Avoid concurrent modification
        buffer.addAll(attributes);
        for(Attribute attribute : buffer){
            super.renderInputAttribute(attribute.attribute_name.get(), () -> {
                ImGui.pushItemWidth(128);
                String initial_name = attribute.attribute_name.get();
                if(ImGui.inputText("##"+attribute.id, attribute.attribute_name, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll)){
                    String new_name = attribute.attribute_name.get();
                    super.renameAttribute(initial_name, new_name);
                }
                ImGui.popItemWidth();
                ImGui.sameLine();
                ImGui.pushItemWidth(96);
                if (ImGui.beginCombo("##"+attribute.id+"_type", attribute.type.getGLSL(), ImGuiComboFlags.None)){
                    for (GLDataType type : GLDataType.values()) {
                        boolean is_selected = attribute.type.equals(type);
                        if (ImGui.selectable(type.getGLSL(), is_selected)){
                            attribute.setType(type);
                        }
                        if (is_selected) {
                            ImGui.setItemDefaultFocus();
                        }
                    }
                    ImGui.endCombo();
                }
                ImGui.popItemWidth();
                ImGui.sameLine();
                ImGui.pushItemWidth(16);
                if(ImGui.imageButton(0, 16, 16)){
                    to_remove.add(attribute);
                }
                if(ImGui.isItemHovered()){
                    ImGui.beginTooltip();
                    ImGui.setTooltip(String.format("Delete %s", attribute.attribute_name.get()));
                    ImGui.endTooltip();
                }
                ImGui.popItemWidth();

            });
        };
        buffer.clear();
    }

    private void removeAttribute(Attribute to_remove){
        super.removePin(to_remove.attribute_name.get());
        this.attributes.remove(to_remove);
    }

    private class Attribute{
        private String id;
        private ImString attribute_name;
        private GLDataType type;
        private float[] value;


        public Attribute(String id, String attribute_name, GLDataType type) {
            this.id = id;
            this.attribute_name = new ImString(attribute_name);
            this.type = type;
            this.value = new float[type.getSizeInFloats()];
            Arrays.fill(value, 0);
        }

        public void setType(GLDataType type){
            if(!this.type.equals(type)){
                AgentNode.this.addInputPin(attribute_name.get(), type);
                this.type = type;
            }
        }
    }

    @Override
    public void serialize(JsonArray evaluation_stack) {
        JsonObject agent_data = new JsonObject();

        JsonObject attributes_data = new JsonObject();
        // Encode all of our attributes by looping through them and checking if they are connected.
        for(Attribute attribute : attributes){
            String attribute_name = attribute.attribute_name.get();

            // Create the data that goes in each attribute.
            JsonObject attribute_data = new JsonObject();

            // Add the datatype.
            attribute_data.add("type", new JsonPrimitive(attribute.type.getGLSL()));

            // Check if there is a link between this attribute and another node.
            InflowPin inflow = (InflowPin) super.getPinFromName(attribute_name);
            if(inflow.isConnected()) {
                // Get the outflow/source pin connecting to this pin.
                OutflowPin source = inflow.getLink();
                // If there is a connection serialize that connection.
                attribute_data.add("default_value", source.getValue());
            }

            attributes_data.add(attribute_name, attribute_data);
        }
        // Attach our attributes to agent data.
        agent_data.add("attributes", attributes_data);
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return JsonNull.INSTANCE;
    }
}
