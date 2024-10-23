package nodegraph.nodes.agent;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import editor.Editor;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.flag.ImGuiComboFlags;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.pin.OutflowPin;

public class AgentCountNode extends Node {

    private AgentNode agent = null;

    private OutflowPin count;

    public AgentCountNode(JsonObject initialization_data) {
        super(initialization_data);

        this.setTitle("Agent Count");

        this.disableFlowControls();

        this.applyStyle(ImNodesCol.TitleBar, NodeColors.RED);
        this.applyStyle(ImNodesCol.TitleBarHovered, NodeColors.RED);

        count = this.addOutputPin("count", GLDataType.INT);
    }

    @Override
    public void onLoad(JsonObject initialization_data) {
        if(initialization_data.has("agent_reference")){
            // We are going to look for our node
            int node_id = initialization_data.get("agent_reference").getAsInt();
            Node reference = Editor.getInstance().getNodeGraph().getNodeFromReference(node_id);
            if(reference instanceof AgentNode){
                this.agent = ((AgentNode) reference);
            }
        }
    }

    @Override
    public JsonObject serializeNode() {
        JsonObject out = new JsonObject();
        if(this.agent != null) {
            out.addProperty("agent_reference", this.agent.getReferenceID());
        }
        return out;
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        if(outflow.equals(count)){
            return new JsonPrimitive((agent != null) ? agent.getAgentInstances() : 0);
        }
        return JsonNull.INSTANCE;
    }

    @Override
    public void render() {
        ImGui.setNextItemWidth(this.width);
        String agent_name = (agent != null) ? agent.getTitle() : "Select an Agent.";
        if (ImGui.beginCombo("##"+super.getID()+"_type", agent_name, ImGuiComboFlags.None)){
            for (Node node : Editor.getInstance().getNodeGraph().getNodesOfType(AgentNode.class)) {
                AgentNode agent_node = (AgentNode) node;

                boolean is_selected = agent_node.getTitle().equals(agent_name);
                if (ImGui.selectable(agent_node.getTitle(), is_selected)){
                    this.agent = agent_node;
                }
                if (is_selected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
        // Some other stuff
        super.render();
    }
}
