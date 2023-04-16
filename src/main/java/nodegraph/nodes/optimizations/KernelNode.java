package nodegraph.nodes.agent;

import com.google.gson.*;
import compiler.FunctionDirective;
import compiler.GLSLCompiler;
import editor.Editor;
import graphics.GLDataType;
import graphics.ShaderManager;
import imgui.ImGui;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.flag.ImGuiComboFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import nodegraph.Node;
import nodegraph.NodeColors;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;
import org.joml.Vector2i;

import java.util.LinkedList;

public class KernelNode extends Node {

    private AgentNode agent = null;
    private final InflowPin instance; // This represents the point to start the kernel at

    private final ImBoolean ignore_self = new ImBoolean(false);

    private final ImInt kernel_width = new ImInt(3);
    private final ImInt kernel_height = new ImInt(3);
    private final Vector2i[][] convolution = new Vector2i[kernel_width.get()][kernel_height.get()];

    private final OutflowPin then;
    private final OutflowPin count;

    private int current_iteration_x = 0;
    private int current_iteration_y = 0;
    private int count_value = 0;

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
        super.setWidth(256);

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
        if(initialization_data.has("ignore_self")){
            ignore_self.set(initialization_data.get("ignore_self").getAsBoolean());
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
    public JsonObject serializeNode() {
        JsonObject out = new JsonObject();
        out.addProperty("kernel_width", kernel_width.get());
        out.addProperty("kernel_height", kernel_height.get());
        out.addProperty("ignore_self", ignore_self.get());
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

        // Render the controls to adjust the kernel size.
        ImGui.setNextItemWidth(this.width / 2.0f);
        ImGui.inputInt("##"+super.getID()+"_kernel_width", kernel_width);
        ImGui.sameLine();
        ImGui.setNextItemWidth(this.width / 2.0f);
        ImGui.inputInt("##"+super.getID()+"_kernel_height", kernel_height);
        ImGui.setNextItemWidth(this.width / 2.0f);
        ImGui.checkbox("Ignore Self", ignore_self);
        ImGui.sameLine();
        ImGui.setNextItemWidth(this.width / 2.0f);
        if(ImGui.button("Edit Kernel")){
            // TODO popup
        }

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
    public void transpile(JsonArray evaluation_stack) {
        // Reset state
        this.current_iteration_x = 0;
        this.current_iteration_y = 0;
        this.count_value = 0;

        // First thing we need to do is determine our start index. This is passed.
        if(instance.isConnected() && (this.agent != null) && super.getOutflow().isConnected()){

            // At this point we have an agent

            for(int j = 0; j < kernel_height.get(); j++) {
                for (int i = 0; i < kernel_width.get(); i++) {
                    // Set which iteration we are on
                    this.current_iteration_x = i;
                    this.current_iteration_y = j;

                    // Try to resolve the outflow data of this pin.
                    // Check if ignore self
                    if(ignore_self.get()){
                        // TODO fix
                        if(current_iteration_x == 1 && current_iteration_y == 1){
                            continue;
                        }
                    }
                    JsonArray kernel_iteration = new JsonArray();
                    this.getOutflow().getConnection().getParent().generateIntermediate(kernel_iteration);
                    evaluation_stack.add(kernel_iteration);

                    // Increment count if this agent could be resolved.
                    count_value++;
                }
            }

        }

        // Afterward add the serialization of our then chain.
        if (this.then.isConnected()) {
            this.then.getConnection().getParent().generateIntermediate(evaluation_stack);
        }
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        if (!(agent == null) && instance.isConnected()) {
            if(agent.hasPinWithName(outflow.getAttributeName())) {

                JsonArray agent_read_reference = new JsonArray();

                // From our kernel we need to lookup the offsets
                JsonArray agent_read_index = new JsonArray();
                // X offset
                agent_read_index.add(instance.getValue());
                agent_read_index.add(current_iteration_x - (kernel_width.get() / 2));
                agent_read_index.add(FunctionDirective.ADD.getNodeID());
                agent_read_index.add(String.format("u_%s_width", this.agent.getTitle()));
                agent_read_index.add(FunctionDirective.GET.getNodeID());
                agent_read_index.add(FunctionDirective.MOD.getNodeID());

                // Y offset
                agent_read_index.add(instance.getValue());
                agent_read_index.add(String.format("u_%s_width", this.agent.getTitle()));
                agent_read_index.add(FunctionDirective.GET.getNodeID());
                agent_read_index.add(FunctionDirective.DIV.getNodeID());
                agent_read_index.add(FunctionDirective.FLOOR.getNodeID());
                agent_read_index.add(current_iteration_y - (kernel_height.get() / 2));
                agent_read_index.add(FunctionDirective.ADD.getNodeID());
                agent_read_index.add(String.format("u_%s_height", this.agent.getTitle()));
                agent_read_index.add(FunctionDirective.GET.getNodeID());
                agent_read_index.add(FunctionDirective.MOD.getNodeID());
                agent_read_index.add(String.format("u_%s_width", this.agent.getTitle()));
                agent_read_index.add(FunctionDirective.GET.getNodeID());
                agent_read_index.add(FunctionDirective.MUL.getNodeID());

                // Convert to screen coordinates
                agent_read_index.add(FunctionDirective.ADD.getNodeID());
                agent_read_index.add("int");
                agent_read_index.add(FunctionDirective.CAST.getNodeID());

                // Agent Read
                agent_read_reference.add(this.agent.getTitle()); // First we say which agent we are reference
                agent_read_reference.add(agent_read_index);
                agent_read_reference.add(outflow.getAttributeName()); // Which property we are getting.
                agent_read_reference.add(FunctionDirective.AGENT_READ.getNodeID()); // Get the reference to the AgentRead function directive.

                return agent_read_reference;
            }
        }

        if(outflow.equals(count)){
            return new JsonPrimitive(count_value);
        }

        return JsonNull.INSTANCE;
    }
}
