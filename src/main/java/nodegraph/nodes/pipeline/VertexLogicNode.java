package nodegraph.nodes.pipeline;

import com.google.gson.*;
import compiler.FunctionDirective;
import editor.Editor;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.flag.ImGuiComboFlags;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.nodes.agent.AgentNode;
import nodegraph.pin.OutflowPin;
import simulation.SimulationManager;

public class VertexLogicNode extends Node {

    private AgentNode agent = null;

    public VertexLogicNode(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("Vertex");

        super.addInputPin("position", GLDataType.VEC3);

        this.forceRenderInflow();
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


    @Override
    public void transpile(JsonArray evaluation_stack) {
        evaluation_stack.add("world_position");
        super.transpile(evaluation_stack);
        evaluation_stack.add(FunctionDirective.SET.getNodeID());
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return JsonNull.INSTANCE;
    }

    public JsonElement getInstanceCount() {
        return new JsonPrimitive((agent != null) ? agent.getAgentInstances() : 0);
    }
}
