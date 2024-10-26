package nodegraph.nodes.agent;

import com.google.gson.*;
import core.Apiary;
import editor.Editor;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.flag.ImGuiComboFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImInt;
import imgui.type.ImString;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.*;

public class AgentNode extends Node {

    private static int attribute_id = 0;

    private ImString agent_name = new ImString();
    private ImInt agent_instances = new ImInt();

    private LinkedHashMap<String, Attribute> attributes = new LinkedHashMap<>();
    private final LinkedList<Attribute> to_remove   = new LinkedList<>();
    private final LinkedList<Attribute> buffer      = new LinkedList<>();

    public AgentNode(JsonObject initialization_data){
        super(initialization_data);

        this.applyStyle(ImNodesCol.TitleBar, NodeColors.AGENT_NODE_TITLE);
        this.applyStyle(ImNodesCol.TitleBarHovered, NodeColors.AGENT_NODE_TITLE_HIGHLIGHT);

        if(initialization_data.has("agent_name")){
            super.setTitle(initialization_data.get("agent_name").getAsString());
            agent_name.set(super.getTitle());
        }

        if(initialization_data.has("agent_instances")){
            agent_instances.set(initialization_data.get("agent_instances").getAsInt());
        }

        if(initialization_data.has("attributes")){
            JsonElement attributes_data = initialization_data.get("attributes");
            if(attributes_data.isJsonArray()){
                JsonArray attributes_array = attributes_data.getAsJsonArray();
                for(int i = 0; i < attributes_array.size(); i++){
                    JsonObject attribute_data = attributes_array.get(i).getAsJsonObject();
                    addInputPin(attribute_data.get("name").getAsString(), GLDataType.valueOf(attribute_data.get("type").getAsString()));
                }
            }
        }

        super.forceRenderInflow();

        super.setWidth(256);
    }

    @Override
    public JsonObject serializeNode() {
        JsonObject out = new JsonObject();

        out.addProperty("agent_name", this.getTitle());
        out.addProperty("agent_instances", this.agent_instances.get());

        JsonArray attributes = new JsonArray();
        for (Attribute attribute : getAttributes()) {
            JsonObject attribute_data = new JsonObject();
            attribute_data.addProperty("name", attribute.attribute_name.get());
            attribute_data.addProperty("type", attribute.type.name());
            attributes.add(attribute_data);
        }
        out.add("attributes", attributes);

        return out;
    }

    @Override
    public void render() {
        ImGui.setNextItemWidth(this.width);
        if(ImGui.inputText("##"+super.getID()+"_agent_name", agent_name)){
            setTitle(agent_name.get());
        }
        ImGui.setNextItemWidth(this.width);
        if(ImGui.inputInt("##"+super.getID()+"_agent_instances", agent_instances)){
            // Nothing
        }
        ImGui.newLine();
        if(ImGui.button("Add Attribute")){
            String attribute_name = attributeNameAtIndex((int) System.currentTimeMillis());
            addInputPin(attribute_name, GLDataType.INT);
        }
        renderAttributes();
    }

    @Override
    public InflowPin addInputPin(String attribute_name, GLDataType... accepted_types) {
        Attribute new_attribute = new Attribute(attribute_name, (GLDataType) Arrays.stream(accepted_types).toArray()[0]);
        attributes.put(attribute_name, new_attribute);
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
        buffer.addAll(getAttributes());
        for(Attribute attribute : buffer){
            super.renderInputAttribute(attribute.attribute_name.get(), () -> {
                ImGui.pushItemWidth(128);
                String initial_name = attribute.attribute_name.get();
                if(ImGui.inputText("##"+attribute.id, attribute.attribute_name, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll)){
                    String new_name = attribute.attribute_name.get();
                    super.renameAttribute(initial_name, new_name);
                    this.attributes.put(new_name, this.attributes.get(initial_name));
                    this.attributes.remove(initial_name);
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
                if(ImGui.imageButton(Editor.getInstance().getTrashIcon(), 16, 16)){
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
        String attribute_name = to_remove.attribute_name.get();
        super.removePin(attribute_name);
        this.attributes.remove(attribute_name);
    }

    private class Attribute{
        private int id;
        private ImString attribute_name;
        private GLDataType type;
        private float[] value;


        public Attribute(String attribute_name, GLDataType type) {
            this.id = ++attribute_id;
            this.attribute_name = new ImString(attribute_name);
            this.type = type;
            this.value = new float[type.getSizeInFloats()];
            Arrays.fill(value, 0);
        }

        public void setType(GLDataType type){
            if(!this.type.equals(type)){
                removePin(attribute_name.get());
                AgentNode.this.addInputPin(attribute_name.get(), type);
                this.type = type;
            }
        }
    }

    @Override
    public void transpile(JsonArray evaluation_stack) {
        JsonObject agent_data = new JsonObject();

        // Add the number of instances
        if(agent_instances.get() > 0){
            agent_data.add("instances", new JsonPrimitive(agent_instances.get()));
        }

        JsonObject attributes_data = new JsonObject();
        // Encode all of our attributes by looping through them and checking if they are connected.
        for(Attribute attribute : getAttributes()){
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
        evaluation_stack.add(agent_data);
    }

    public boolean overridesInstances(){
        return this.agent_instances.get() > 0;
    }

    public long getAgentInstances(){
        return this.overridesInstances() ? this.agent_instances.get() : Editor.getInstance().getAllocatedScreenSize();
    }

    public long getBufferSizeInBytes(){
        long size_in_bytes = 0;
        for(Attribute attribute : attributes.values()){
            int bytes = attribute.type.getSizeInBytes();
            size_in_bytes += bytes + MathUtil.computeAlignment(bytes, 4);
        }
        size_in_bytes *= 2L;
        return size_in_bytes;
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return JsonNull.INSTANCE;
    }

    public Collection<Attribute> getAttributes(){
        return this.attributes.values();
    }
}
