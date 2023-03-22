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
import nodegraph.nodes.TemplateNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

import java.util.LinkedList;

public class AgentReadNode extends Node {

    private AgentNode agent = null;
    private InflowPin instance;

    public AgentReadNode(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("Agent Read");

        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.AGENT_NODE_TITLE);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.AGENT_NODE_TITLE_HIGHLIGHT);

        super.disableFlowControls();

        //TODO maybe change instance to agent_id
        instance = super.addInputPin("instance", GLDataType.INT);
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
                    if(agent == null || !agent.equals(agent_node)) {
                        // We need to update out outflows
                        agent = agent_node;
                        setOutflowPins(agent);
                    }
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
            for(OutflowPin outflow : super.getNodeOutflowPins()){
                super.renderOutputAttribute(outflow.getAttributeName());
            }
        }
    }

    private void setOutflowPins(AgentNode agent){
        super.clearOutflowPins();
        for(InflowPin pin : agent.getNodeInflowPins()){
            LinkedList<GLDataType> pin_data_types = pin.getAcceptedTypes();
            if(pin_data_types.size() == 1) {
                GLDataType type = pin_data_types.getFirst();
                super.addOutputPin(pin.getAttributeName(), type);
            }
        }
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        JsonArray agent_read_reference = new JsonArray();

        agent_read_reference.add(this.agent.getTitle()); // First we say which agent we are reference

        agent_read_reference.add(instance.getValue());

        agent_read_reference.add(outflow.getAttributeName()); // Which property we are getting.

        agent_read_reference.add(FunctionDirective.AGENT_READ.getNodeID()); // Get the reference to the AgentRead function directive.

        return agent_read_reference;
    }
}