package nodegraph.nodes.agent;

import com.google.gson.*;
import compiler.FunctionDirective;
import editor.Editor;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.flag.ImGuiComboFlags;
import imgui.type.ImInt;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

import java.util.LinkedList;

public class KernelNode extends Node {

    private AgentNode agent = null;
    private InflowPin instance; // This represents the point to start the kernel at

    private ImInt kernel_width = new ImInt(3);
    private ImInt kernel_height = new ImInt(3);

    private OutflowPin then;
    private OutflowPin count;

    public KernelNode(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("Convolution Kernel");

        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.AGENT_NODE_TITLE);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.AGENT_NODE_TITLE_HIGHLIGHT);

        super.setNodeProcessesOwnFlow(true);

        // This is a node that needs to be evaluated.
        super.forceRenderInflow();
        super.forceRenderOutflow();

        //

        //TODO maybe change instance to agent_id
        instance = super.addInputPin("instance", GLDataType.INT);
        then = super.addOutflowPin("then");
        count = super.addOutputPin("count", GLDataType.INT);
    }

    @Override
    public void onLoad(JsonObject initialization_data) {

        // Initialize our Kernel data
        if(initialization_data.has("kernel_width")){
            kernel_width.set(initialization_data.get("kernel_width").getAsInt());
        }
        if(initialization_data.has("kernel_height")){
            kernel_height.set(initialization_data.get("kernel_height").getAsInt());
        }

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
        out.addProperty("kernel_width", kernel_width.get());
        out.addProperty("kernel_height", kernel_height.get());
        if(this.agent != null) {
            out.addProperty("agent_reference", this.agent.getReferenceID());
        }
        return out;
    }

    @Override
    public void render() {
        // Render our index
        super.renderInputAttribute("instance");
        ImGui.sameLine();
        // Render the drop down of different
        ImGui.pushItemWidth(this.width / 2.0f);
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

        // Render the controls to adjust the kernel size.
        ImGui.setNextItemWidth(this.width / 2.0f);
        ImGui.inputInt("##"+super.getID()+"_kernel_width", kernel_width);
        ImGui.sameLine();
        ImGui.setNextItemWidth(this.width / 2.0f);
        ImGui.inputInt("##"+super.getID()+"_kernel_height", kernel_height);

        ImGui.image(0, this.width, 2);
        // Render the agent attributes
        if (!(agent == null)) {
            for(OutflowPin outflow : super.getNodeOutflowPins()){
                if(agent.hasPinWithName(outflow.getAttributeName())) {
                    super.renderOutputAttribute(outflow.getAttributeName());
                }
            }
        }

        // Now we need to render a divider
        ImGui.image(0, this.width, 2);
        super.renderOutputAttribute(then.getAttributeName());
        super.renderOutputAttribute(count.getAttributeName());
    }

    public void setAgent(AgentNode agent) {
        if(this.agent == null || !this.agent.equals(agent)) {
            // We need to update out outflows
            this.agent = agent;
            setOutflowPins(this.agent);
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
    public void serialize (JsonArray evaluation_stack) {
        JsonArray kernel_evaluation = new JsonArray();
        evaluation_stack.add(kernel_evaluation);
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
