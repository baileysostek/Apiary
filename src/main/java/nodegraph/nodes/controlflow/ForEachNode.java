package nodegraph.nodes.controlflow;

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
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

import java.util.LinkedList;

public class ForEachNode extends Node {

    private static final String ITERATOR_VARIABLE_NAME = "agent_index_";

    private AgentNode agent = null;

    private final OutflowPin agent_instance;
    private final OutflowPin then;

    private int current_iteration_x = 0;
    private int current_iteration_y = 0;
    private int count_value = 0;

    public ForEachNode(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("For");

        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.AGENT_NODE_TITLE);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.AGENT_NODE_TITLE_HIGHLIGHT);

        super.setNodeProcessesOwnFlow(true);

        // This is a node that needs to be evaluated.
        super.forceRenderInflow();
        super.forceRenderOutflow();

        //
        super.setWidth(256);

        agent_instance = super.addOutputPin("instance", GLDataType.INT);
        then = super.addOutflowPin("then");
    }

    @Override
    public void onLoad(JsonObject initialization_data) {

//        // Initialize our Kernel data
//        if(initialization_data.has("size")){
//            size.set(initialization_data.get("kernel_width").getAsInt());
//        }
//        if(initialization_data.has("kernel_height")){
//            kernel_height.set(initialization_data.get("kernel_height").getAsInt());
//        }
//        if(initialization_data.has("ignore_self")){
//            ignore_self.set(initialization_data.get("ignore_self").getAsBoolean());
//        }

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
    public JsonObject serializeNode() {
        JsonObject out = new JsonObject();
//        out.addProperty("kernel_width", kernel_width.get());
//        out.addProperty("kernel_height", kernel_height.get());
//        out.addProperty("ignore_self", ignore_self.get());
        if(this.agent != null) {
            out.addProperty("agent_reference", this.agent.getReferenceID());
        }
        return out;
    }

    @Override
    public void render() {
        // Render our index
        ImGui.pushItemWidth(this.width / 2.0f);
        super.renderInputAttribute("instance");
        ImGui.popItemWidth();
        ImGui.sameLine();
        // Render the drop-down of different agents to select.
        ImGui.setNextItemWidth(this.width / 2.0f);
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

        ImGui.image(0, this.width, 2);
        // Render the agent attributes
        if (!(agent == null)) {
            // Index
            super.renderOutputAttribute(agent_instance.getAttributeName());

            // Direct properties of the agent
            for(OutflowPin outflow : super.getNodeOutflowPins()){
                if(agent.hasPinWithName(outflow.getAttributeName())) {
                    super.renderOutputAttribute(outflow.getAttributeName());
                }
            }
        }

        // Now we need to render a divider
        ImGui.image(0, this.width, 2);
        super.renderOutputAttribute(then.getAttributeName());
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
    public void transpile(JsonArray evaluation_stack) {

        // Compute the logic for the
        JsonArray for_loop_data = new JsonArray();

        for_loop_data.add(this.getIterationVariableName());
        for_loop_data.add("0");
        for_loop_data.add(this.agent.getAgentInstances()+"");
        JsonArray iteration_logic = new JsonArray();
        if(this.getOutflow().isConnected()) {
            super.getOutflow().getConnection().getParent().transpile(iteration_logic);
        }
        for_loop_data.add(iteration_logic);
        for_loop_data.add(FunctionDirective.FOR_EACH.getNodeID());

        // Push the data used to generate our for_loop to the evaluation_stack
        evaluation_stack.add(for_loop_data);

        // Push the continued evaluation onto the stack.
        if(then.isConnected()){
            JsonArray then_data = new JsonArray();
            then.getConnection().getParent().transpile(then_data);
            evaluation_stack.add(then_data);
        }


        // Reset state
//        this.current_iteration_x = 0;
//        this.current_iteration_y = 0;
//        this.count_value = 0;
//
//        // First thing we need to do is determine our start index. This is passed.
//        if(size.isConnected() && (this.agent != null) && super.getOutflow().isConnected()){
//
//            // At this point we have an agent
//
//            for(int j = 0; j < kernel_height.get(); j++) {
//                for (int i = 0; i < kernel_width.get(); i++) {
//                    // Set which iteration we are on
//                    this.current_iteration_x = i;
//                    this.current_iteration_y = j;
//
//                    // Try to resolve the outflow data of this pin.
//                    // Check if ignore self
//                    if(ignore_self.get()){
//                        // TODO fix
//                        if(current_iteration_x == 1 && current_iteration_y == 1){
//                            continue;
//                        }
//                    }
//                    JsonArray kernel_iteration = new JsonArray();
//                    this.getOutflow().getConnection().getParent().generateIntermediate(kernel_iteration);
//                    evaluation_stack.add(kernel_iteration);
//
//                    // Increment count if this agent could be resolved.
//                    count_value++;
//                }
//            }
//
//        }
//
//        // Afterward add the serialization of our then chain.
//        if (this.then.isConnected()) {
//            this.then.getConnection().getParent().generateIntermediate(evaluation_stack);
//        }
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        if (!(agent == null) && outflow.isConnected()) {

            if(agent.hasPinWithName(outflow.getAttributeName())){
                // Agent Read
                JsonArray agent_read_reference = new JsonArray();
                agent_read_reference.add(this.agent.getTitle()); // First we say which agent we are reference
                agent_read_reference.add(getIterationVariableName());
                agent_read_reference.add(outflow.getAttributeName()); // Which property we are getting.
                agent_read_reference.add(FunctionDirective.AGENT_READ.getNodeID()); // Get the reference to the AgentRead function directive.

                return agent_read_reference;
            }

            // If we are accessing the agent_instance
            if(outflow.equals(agent_instance)){
                return new JsonPrimitive(getIterationVariableName());
            }

        }

        return JsonNull.INSTANCE;
    }

    private String getIterationVariableName(){
        return String.format("%s%s", ITERATOR_VARIABLE_NAME, this.getReferenceID());
    }
}
