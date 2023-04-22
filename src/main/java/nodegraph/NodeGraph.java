package nodegraph;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import editor.Editor;
import imgui.ImGui;
import imgui.flag.ImGuiComboFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import nodegraph.nodes.agent.AgentNode;
import nodegraph.nodes.controlflow.InitializationNode;
import nodegraph.nodes.controlflow.StepNode;
import nodegraph.nodes.pipeline.FragmentLogicNode;
import nodegraph.nodes.pipeline.VertexLogicNode;
import nodegraph.nodes.variables.DefineNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;
import nodegraph.pin.Pin;
import simulation.SimulationManager;
import simulation.world.DefaultWorld2D;
import simulation.world.DefaultWorld3D;
import simulation.world.World;
import util.JsonUtils;
import util.StringUtils;

import java.util.*;

public class NodeGraph {

    // Hold references to our nodes.
    private LinkedHashMap<Integer, Integer> node_id_to_reference = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Node> nodes = new LinkedHashMap<>();
    private LinkedHashMap<Class<Node>, LinkedList<Node>> typed_nodes = new LinkedHashMap<>();

    // Simulation Parameters
    private ImString simulation_name = new ImString();
    private ImBoolean override_default_size = new ImBoolean(true);
    private ImInt simulation_width = new ImInt();
    private ImInt simulation_height = new ImInt();

    // Variables to use for this simulation instance
    private Class<? extends World> world_template = DefaultWorld3D.class;


    public NodeGraph() {

    }

    public void addNode(Node node){
        // Disallow adding null nodes.
        if (node == null) {
            return;
        }

        this.nodes.put(node.getReferenceID(), node);
        this.node_id_to_reference.put(node.getID(), node.getReferenceID());

        // Check if we have this type
        if (!this.typed_nodes.containsKey(node.getClass())) {
            this.typed_nodes.put((Class<Node>) node.getClass(), new LinkedList<>());
        }
        this.typed_nodes.get(node.getClass()).add(node);
    }

    public <T extends Node> boolean hasNodesOfType(Class<T> node){
        return this.typed_nodes.containsKey(node);
    }

    public <T extends Node> LinkedList<Node> getNodesOfType(Class<T> node){
        return this.typed_nodes.getOrDefault(node, null);
    }

    public void render(){
        for(Node node : nodes.values()){
            node.renderNode();
        }
        for(Node node : nodes.values()){
            node.renderLinks();
        }
    }

    // These functions are used from the editor to render information about this simulation
    public void renderSimulationParamEditor(){
        if(ImGui.inputText("Simulation Name", simulation_name)){

        }
        if(ImGui.inputInt("Simulation Width", simulation_width)){

        }
        if(ImGui.inputInt("Simulation Height", simulation_height)){

        }
        String variable_name = (world_template != null) ? world_template.getSimpleName() : "Select a World Template";
        if (ImGui.beginCombo("##world_template", variable_name, ImGuiComboFlags.None)){
            for (Class<? extends World> world_class : SimulationManager.getInstance().getWorldTemplates()) {
                boolean is_selected = world_class.getSimpleName().equals(variable_name);
                if (ImGui.selectable(world_class.getSimpleName(), is_selected)){
                    this.world_template = world_class;
                }
                if (is_selected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
    }

    public JsonObject serialize(){
        JsonObject out = new JsonObject();

//        for(Node node : this.getNodesOfType(InitializationNode.class)){
//            JsonArray test = new JsonArray();
//            node.generateIntermediate(test);
//            System.out.println(test);
//        }

        // World definition
        JsonObject world = new JsonObject();
        // Add our world properties
        world.add("name", new JsonPrimitive("Conway's Game of Life"));
        world.add("template", new JsonPrimitive(this.world_template.getName()));
        // If the user indicated to override the default size, use those overriden sizes.
        if (override_default_size.get()) {
            if(this.overridesWidth()) {
                world.add("width", new JsonPrimitive(simulation_width.get()));
            }
            if(simulation_height.get() > 0) {
                world.add("height", new JsonPrimitive(simulation_height.get()));
            }
        }

        JsonObject arguments = new JsonObject();
        if(hasNodesOfType(FragmentLogicNode.class)){
            // Serialize the node.
            LinkedList<Node> fragment_nodes = getNodesOfType(FragmentLogicNode.class);
            // There should only be one fragment node.
            FragmentLogicNode fragment_logic_node = (FragmentLogicNode) fragment_nodes.getFirst();
            Node initializer = getSource(fragment_logic_node);
            if(initializer instanceof InitializationNode){
                // We have an initialization node which leads to the fragment logic, lets serialize that data.
                JsonArray fragment_logic = new JsonArray();
                initializer.generateIntermediate(fragment_logic);
                arguments.add("fragment_logic", fragment_logic);
            }
        }

        if(hasNodesOfType(VertexLogicNode.class)){
            // Serialize the node.
            LinkedList<Node> vertex_nodes = getNodesOfType(VertexLogicNode.class);
            // There should only be one fragment node.
            if(vertex_nodes != null && vertex_nodes.size() > 0) {
                VertexLogicNode vertex_logic_node = (VertexLogicNode) vertex_nodes.getFirst();
                Node initializer = getSource(vertex_logic_node);
                if (initializer instanceof InitializationNode) {
                    // We have an initialization node which leads to the fragment logic, lets serialize that data.
                    JsonArray vertex_logic = new JsonArray();
                    initializer.generateIntermediate(vertex_logic);
                    arguments.add("vertex_logic", vertex_logic);
                }
            }
        }
        world.add("arguments", arguments);

        out.add("world", world);

        // Agents definitions
        JsonObject agents = new JsonObject();
        Collection<Node> agent_nodes = this.getNodesOfType(AgentNode.class);
        if (agent_nodes != null) {
            for (Node node : agent_nodes) {
                AgentNode agent_node = ((AgentNode) node);
                JsonArray agent_initialization_data = new JsonArray();
                agent_node.transpile(agent_initialization_data);
                // Get the first element
                agents.add(agent_node.title, agent_initialization_data.get(0));
            }
        }
        out.add("agents", agents);

        // Steps definitions
        JsonArray steps = new JsonArray();
        LinkedList<Node> step_nodes = this.getNodesOfType(StepNode.class);
        if (step_nodes != null) {
            step_nodes.sort(new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return (((StepNode) o1).getStepIndex() > ((StepNode) o2).getStepIndex()) ? 1 : -1;
                }
            });
            // TODO we need to sort this collection by step index.
            for (Node node : step_nodes) {
                StepNode step_node = ((StepNode) node); // Cast to StepNode

                // Create a json object to hold the data we use to represent this step.
                JsonObject step_object = new JsonObject();

                // Compute how many times we need to perform this logic loop
                if (step_node.hasIterationCountSet()) {
                    if (step_node.useAgent()) {
                        step_object.addProperty("for_each", step_node.getSelectedAgent());
                    } else {
                        step_object.addProperty("for_each", step_node.getIterationCount());
                    }
                }

                // Generate the logic that is required to perform this step.
                JsonArray step_logic = new JsonArray();
                step_node.generateIntermediate(step_logic);
                step_object.add("logic", step_logic);

                // Add this object to our list of steps.
                steps.add(step_object);
            }
        }
        out.add("steps", steps);

        return out;
    }

    public Pin getPinFromID(int hovered_pin) {
        for(Node node : nodes.values()){
            if(!node.hasPinWithID(hovered_pin)){
                continue;
            }
            // We found the node with the pin that we hovered.

            return node.getPinFromID(hovered_pin);
        }

        return null;
    }

    public JsonObject serializeNodes (Collection<Node> nodes) {
        JsonObject serialization_object = new JsonObject();

        JsonArray nodes_array = new JsonArray();
        JsonArray links_array = new JsonArray();

        if (override_default_size.get()) {
            if(this.overridesWidth()) {
                serialization_object.add("width", new JsonPrimitive(simulation_width.get()));
            }
            if(simulation_height.get() > 0) {
                serialization_object.add("height", new JsonPrimitive(simulation_height.get()));
            }
        }

        HashMap<Node, Integer> node_ids = new HashMap<>();
        int node_index = 0;
        for(Node node : nodes){
            int id = ++node_index;
            node_ids.put(node, id);
            JsonObject node_save_data = node.serializeBase();
            node_save_data.addProperty("id", id);
            nodes_array.add(node_save_data);
        }
        serialization_object.add("nodes", nodes_array);
        // Now that we have added all of the nodes and we know the domain of nodes which exist in the save file, we can generate the links.

        for(Node node : nodes){
            // We are only looking at inflows here.
            if(node.getInflow().isConnected()){
                JsonObject link_serialization = serializeLink(node.getInflow(), node_ids);
                if (link_serialization != null) {
                    links_array.add(link_serialization);
                }
            }

            for (InflowPin inflow : node.getNodeInflowPins()) {
                JsonObject link_serialization = serializeLink(inflow, node_ids);
                if (link_serialization != null) {
                    links_array.add(link_serialization);
                }
            }
        }

        serialization_object.add("links", links_array);

        return serialization_object;
    }

    public Node getNodeFromID(int node_id) {
        return this.nodes.getOrDefault(this.node_id_to_reference.get(node_id), null);
    }

    public Node getNodeFromReference(int node_id) {
        return this.nodes.getOrDefault(node_id, null);
    }

    public Collection<Node> getNodesFromIDs(int[] node_ids) {
        LinkedList<Node> out = new LinkedList<>();
        for(int node_id : node_ids){
            int node_reference = this.node_id_to_reference.get(node_id);
            if(this.nodes.containsKey(node_reference)){
                out.push(this.nodes.get(node_reference));
            }
        }
        return out;
    }

    public void saveToFile (String file_path) {
        JsonObject save_object = serializeNodes(this.nodes.values());
        StringUtils.write(JsonUtils.prettyPrint(save_object), file_path);
        this.clearNodes();
        load(file_path);
    }

    private JsonObject serializeLink(InflowPin dest_node, HashMap<Node, Integer> node_ids){
        if(dest_node.isConnected()){
            OutflowPin source_node = dest_node.getLink();
            JsonObject link = new JsonObject();

            JsonObject src = new JsonObject();
            src.addProperty("node_id", node_ids.get(source_node.getParent()));
            if(node_ids.get(source_node.getParent()) == null){
                return null;
            }
            src.addProperty("attribute_name", source_node.getAttributeName());
            link.add("src", src);

            JsonObject dst = new JsonObject();
            dst.addProperty("node_id", node_ids.get(dest_node.getParent()));
            if(node_ids.get(dest_node.getParent()) == null){
                return null;
            }
            dst.addProperty("attribute_name", dest_node.getAttributeName());
            link.add("dst", dst);

            return link;
        }
        return null;
    }

    public Collection<Node> load (String file_path) {
        // Clear this file
        this.clearNodes();

        // Load the new file
        JsonObject save_data = JsonUtils.loadJson(file_path);
        if(save_data == null){
            save_data = new JsonObject();
        }
        return load(save_data, false);
    }

    public Collection<Node> load (JsonObject serialized_node_data, boolean is_clone) {

        if(serialized_node_data.has("width")) {
            simulation_width.set(serialized_node_data.get("width").getAsInt());
        }
        if(serialized_node_data.has("height")) {
            simulation_height.set(serialized_node_data.get("height").getAsInt());
        }

        /**
         *  Since ImNodes uses an internal representation for the nodes that we don't have access to we need to give each node a random ID each time it is generated
         *  to avoid the problem of linking to arbitrary IDs we construct a linear list of incremental IDs at save time. At load time we reference this map of IDs but
         *  they dont represent the true IDs of nodes. Those are ~random depending on which nodes have been created or deleted.
         */
        HashMap<Integer, Node> id_to_node = new HashMap<>();
        if(serialized_node_data.has("nodes")){
            JsonElement nodes = serialized_node_data.get("nodes");
            if(nodes.isJsonArray()){
                JsonArray nodes_array = nodes.getAsJsonArray();
                for(int i = 0; i < nodes_array.size(); i++){
                    JsonObject initialization_data = nodes_array.get(i).getAsJsonObject();
                    int reference_id = initialization_data.get("id").getAsInt();
                    if(is_clone){
                        initialization_data.remove("id");
                    }
                    Node node = NodeRegistry.getInstance().getNodeFromClass(initialization_data);
                    if(is_clone){
                        initialization_data.addProperty("id", reference_id);
                    }
                    id_to_node.put(reference_id, node);
                    this.addNode(node);
                }
                // After all the nodes have been added we can call the onLoad function.
                for(int i = 0; i < nodes_array.size(); i++){
                    JsonObject initialization_data = nodes_array.get(i).getAsJsonObject();
                    id_to_node.get(initialization_data.get("id").getAsInt()).init(initialization_data);
                }
            }
        }
        if(serialized_node_data.has("links")){
            JsonElement links = serialized_node_data.get("links");
            if(links.isJsonArray()){
                JsonArray links_array = links.getAsJsonArray();
                for(int i = 0; i < links_array.size(); i++){
                    // Link data example
                    // {"src":{"node_id":1,"attribute_name":"Add"},"dst":{"node_id":2,"attribute_name":"x_pos"}}
                    JsonObject link_data = links_array.get(i).getAsJsonObject();
                    // Get the node from the link
                    int source_id = link_data.get("src").getAsJsonObject().get("node_id").getAsInt();
                    String source_attribute_name = link_data.get("src").getAsJsonObject().get("attribute_name").getAsString();
                    OutflowPin source_pin = (OutflowPin) id_to_node.get(source_id).getPinFromName(source_attribute_name);

                    if (source_pin == null) {
                        if(id_to_node.containsKey(source_id)){
                            System.err.println(String.format("Error: Could not find an attribute on %s:id:%s with name:%s", id_to_node.get(source_id).toString(), source_id+"", source_attribute_name));
                        }else{
                            System.err.println(String.format("Error: Could not find a node with the id:%s", source_id+""));
                        }
                        continue;
                    }

                    int dest_id = link_data.get("dst").getAsJsonObject().get("node_id").getAsInt();
                    String dest_attribute_name = link_data.get("dst").getAsJsonObject().get("attribute_name").getAsString();
                    InflowPin dest_pin = (InflowPin) id_to_node.get(dest_id).getPinFromName(dest_attribute_name);

                    if (dest_pin == null) {
                        if(id_to_node.containsKey(dest_id)){
                            System.err.println(String.format("Error: Could not find an attribute on %s:id:%s with name %s", id_to_node.get(dest_id).toString(), dest_id+"", dest_attribute_name));
                        }else{
                            System.err.println(String.format("Error: Could not find a node with the id:%s", dest_id+""));
                        }
                        continue;
                    }

                    // Bidirectional link
                    source_pin.link(dest_pin);
                    dest_pin.link(source_pin);
                }
            }
        }

        return id_to_node.values();
    }

    public void removeNodes(Collection<Node> nodes) {
        for(Node node : new LinkedList<>(nodes)){
            this.typed_nodes.get(node.getClass()).remove(node);
            this.nodes.remove(node.getReferenceID());
            node.onRemove();
        }
    }

    private void clearNodes(){
        this.removeNodes(this.nodes.values());
    }

    private Node getSource(Node other){
        Node parent = other;
        while(true){
            if(!parent.getInflow().isConnected()){
                return parent;
            }
            parent = parent.getInflow().getLink().getParent();
        }
    }

    public boolean overridesWidth() {
        return simulation_width.get() > 0;
    }

    public int getSimulationWidth(){
        return simulation_width.get();
    }

    public boolean overridesHeight() {
        return simulation_height.get() > 0;
    }

    public int getSimulationHeight(){
        return simulation_height.get();
    }
}
