package nodegraph.nodes.agent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import editor.Editor;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.flag.ImGuiComboFlags;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

import java.util.LinkedList;

public class AgentWriteNode extends Node {

    private AgentNode agent = null;
    private InflowPin instance;

    public AgentWriteNode(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("Agent Write");

        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.AGENT_NODE_TITLE);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.AGENT_NODE_TITLE_HIGHLIGHT);

        // This is a node that needs to be evaluated.
        super.forceRenderInflow();
        super.forceRenderOutflow();

        //TODO maybe change instance to agent_id
        instance = super.addInputPin("instance", GLDataType.INT);
    }

    @Override
    public void onLoad(JsonObject initialization_data) {
        if(initialization_data.has("agent_reference")){
            // We are going to look for our node
            int node_id = initialization_data.get("agent_reference").getAsInt();
            Node reference = Editor.getInstance().getNodeGraph().getNodeFromReference(node_id);
            if(reference instanceof AgentNode){
                this.setAgent((AgentNode) reference);
            }
        }
    }

    @Override
    public JsonObject nodeSpecificSaveData() {
        JsonObject out = new JsonObject();
        if(this.agent != null) {
            out.addProperty("agent_reference", this.agent.getReferenceID());
        }
        return out;
    }

    @Override
    public void render() {
        // Render our index
        super.renderInputAttribute("instance");

        // Render the drop down of different
        ImGui.pushItemWidth(96);
        String agent_name = (agent != null) ? agent.getTitle() : "Select an Agent.";
        if (ImGui.beginCombo("##"+super.getID()+"_type", agent_name, ImGuiComboFlags.None)){
            for (Node node : Editor.getInstance().getNodeGraph().getNodesOfType(AgentNode.class)) {
                AgentNode agent_node = (AgentNode) node;

                boolean is_selected = agent_node.getTitle().equals(agent_name);
                if (ImGui.selectable(agent_node.getTitle(), is_selected)){
                    this.setAgent(agent_node);
                }
                if (is_selected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
        ImGui.popItemWidth();

        if (!(agent == null)) {
            ImGui.newLine(); // Render outflow attributes for this agentNode.
            for(InflowPin inflow_pin : super.getNodeInflowPins()){
                if(inflow_pin.equals(instance)){
                    continue;
                }
                super.renderInputAttribute(inflow_pin.getAttributeName());
            }
        }
    }

    public void setAgent(AgentNode agent) {
        if(this.agent == null || !this.agent.equals(agent)) {
            // We need to update out outflows
            this.agent = agent;
            setInflowPins(this.agent);
        }
    }

    private void setInflowPins (AgentNode agent) {
        super.clearInflowPins();
        for(InflowPin pin : agent.getNodeInflowPins()){
            LinkedList<GLDataType> pin_data_types = pin.getAcceptedTypes();
            if(pin_data_types.size() == 1) {
                GLDataType type = pin_data_types.getFirst();
                super.addInputPin(pin.getAttributeName(), type);
            }
        }
    }

    @Override
    public void serialize (JsonArray evaluation_stack) {
        // Each attribute we are trying to write to needs to have its own VM code.
        JsonArray agent_writes = new JsonArray();

        // We can buffer some information for later so we dont need to compute it per attribute we are trying to modify.
        String agent_name = this.agent.getTitle();
        JsonElement index = instance.getValue();

        for (InflowPin inflow : super.getNodeInflowPins()) {

            // Dont process the instance pin.
            if(inflow.equals(instance)){
                continue;
            }

            if(inflow.hasNonDefaultValue()) {
                JsonArray agent_write_instance = new JsonArray();

                agent_write_instance.add(agent_name); // First we say which agent we are reference
                agent_write_instance.add(index);
                agent_write_instance.add(inflow.getAttributeName()); // Which property we are getting.
                agent_write_instance.add(inflow.getValue());
                agent_write_instance.add(FunctionDirective.AGENT_WRITE.getNodeID()); // Get the reference to the AgentRead function directive.

                // Add this to the set of agent_writes
                agent_writes.add(agent_write_instance);
            }
        }

        // If we were able to resolve
        if(agent_writes.size() > 0){
            evaluation_stack.add(agent_writes);
        }
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return JsonNull.INSTANCE;
    }
}
