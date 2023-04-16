package nodegraph.nodes.controlflow;

import com.google.gson.JsonObject;
import editor.Editor;
import imgui.ImGui;
import imgui.flag.ImGuiComboFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import nodegraph.Node;
import nodegraph.nodes.OutflowNode;
import nodegraph.nodes.agent.AgentNode;

public class StepNode extends OutflowNode {

    ImInt step_index = new ImInt(0);

    ImBoolean use_agent_reference = new ImBoolean(true);
    private AgentNode agent = null;
    ImInt for_each_count = new ImInt(0);

    public StepNode(JsonObject initialization_data) {
        super("Step", initialization_data);

        if(initialization_data.has("step_index")){
            step_index.set(initialization_data.get("step_index").getAsInt());
        }

        if(initialization_data.has("use_agent")){
            use_agent_reference.set(initialization_data.get("use_agent").getAsBoolean());
        }

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

        out.addProperty("step_index", step_index.get());
        out.addProperty("use_agent", use_agent_reference.get());
        if(use_agent_reference.get()){
            if(this.agent != null) {
                out.addProperty("agent_reference", this.agent.getReferenceID());
            }
        }

        return out;
    }

    @Override
    public void render() {
        ImGui.setNextItemWidth(this.width);
        if(ImGui.inputInt("##"+super.getID()+"_step_index", step_index)){

        }
        ImGui.setNextItemWidth(16);
        if(ImGui.checkbox("##"+super.getID()+"_use_agent", use_agent_reference)){

        }
        ImGui.sameLine();
        ImGui.setNextItemWidth(this.width - 16 - 8);
        if(use_agent_reference.get()){
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
        }else{
            if(ImGui.inputInt("##"+super.getID()+"_for_each_count", for_each_count)){

            }
        }
        // Some other stuff
        super.render();
    }

    public boolean hasIterationCountSet() {
        return this.agent != null || for_each_count.get() > 0;
    }

    public boolean useAgent(){
        return this.use_agent_reference.get();
    }

    public String getSelectedAgent(){
        return this.agent.getTitle();
    }

    public int getIterationCount(){
        return this.for_each_count.get();
    }

    public int getStepIndex(){
        return this.step_index.get();
    }
}
