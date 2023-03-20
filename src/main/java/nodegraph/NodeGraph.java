package nodegraph;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import input.Keyboard;
import nodegraph.nodes.agent.AgentNode;
import nodegraph.nodes.controlflow.InitializationNode;
import nodegraph.nodes.controlflow.StepNode;
import nodegraph.nodes.pipeline.FragmentLogicNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;
import nodegraph.pin.Pin;
import org.lwjgl.glfw.GLFW;
import util.JsonUtils;
import util.ObservableLinkedHashMap;
import util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class NodeGraph {
    private LinkedHashMap<Integer, Node> nodes = new LinkedHashMap<>();
    private LinkedHashMap<Class<Node>, LinkedList<Node>> typed_nodes = new LinkedHashMap<>();

    public NodeGraph() {
        Keyboard.getInstance().addPressCallback(GLFW.GLFW_KEY_F1, () -> {
            JsonObject object = NodeGraph.this.serialize();
            System.out.println(object);
        });

        Keyboard.getInstance().addPressCallback(GLFW.GLFW_KEY_F2, () -> {
            saveToFile("simulations/gol_test.jsonc");
        });
    }

    public void addNode(Node node){
        // Disallow adding null nodes.
        if (node == null) {
            return;
        }

        this.nodes.put(node.getID(), node);

        // Check if we have this type
        if (!this.typed_nodes.containsKey(node.getClass())) {
            this.typed_nodes.put((Class<Node>) node.getClass(), new LinkedList<>());
        }
        this.typed_nodes.get(node.getClass()).add(node);
    }

    public <T extends Node> boolean hasNodesOfType(Class<T> node){
        return this.typed_nodes.containsKey(node);
    }

    public <T extends Node> Collection<Node> getNodesOfType(Class<T> node){
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

    public JsonObject serialize(){
        JsonObject out = new JsonObject();

        for(Node node : this.getNodesOfType(InitializationNode.class)){
            JsonArray test = new JsonArray();
            node.generateIntermediate(test);
            System.out.println(test);
        }

//        // World definition
//        JsonObject world = new JsonObject();
//        // Add our world properties
//        world.add("name", new JsonPrimitive("Conway's Game of Life"));
//        world.add("type", new JsonPrimitive("AgentGrid2D"));
//
//        JsonObject arguments = new JsonObject();
//        if(hasNodesOfType(FragmentLogicNode.class)){
//            // Serialize the node.
//        }
//        world.add("arguments", arguments);
//
//        out.add("world", world);
//
//        // Agents definitions
//        Collection<Node> agent_nodes = this.getNodesOfType(AgentNode.class);
//        JsonObject agents = new JsonObject();
//        for(Node node : agent_nodes){
//            AgentNode agent_node = ((AgentNode) node);
//
//            agents.add(agent_node.title, agent_node.generateIntermediate());
//        }
//        out.add("agents", agents);
//
//        // Steps definitions
//        Collection<Node> step_nodes = this.getNodesOfType(StepNode.class);
//        JsonArray steps = new JsonArray(agent_nodes.size());
//        out.add("steps", steps);

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

    public void saveToFile (String file_path) {
        JsonObject save_object = new JsonObject();

        JsonArray nodes_array = new JsonArray();
        JsonArray links_array = new JsonArray();

        HashMap<Node, Integer> node_ids = new HashMap<>();
        int node_index = 0;
        for(Node node : nodes.values()){
            int id = ++node_index;
            node_ids.put(node, id);
            JsonObject node_save_data = node.generateSaveData();
            node_save_data.addProperty("id", id);
            nodes_array.add(node_save_data);
        }
        save_object.add("nodes", nodes_array);
        // Now that we have added all of the nodes and we know the domain of nodes which exist in the save file, we can generate the links.

        for(Node node : nodes.values()){
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

        save_object.add("links", links_array);

        System.out.println(save_object);

        StringUtils.write(save_object.toString(), file_path);
        this.nodes.clear();
        load(file_path);
    }

    private JsonObject serializeLink(InflowPin dest_node, HashMap<Node, Integer> node_ids){
        if(dest_node.isConnected()){
            OutflowPin source_node = dest_node.getLink();
            JsonObject link = new JsonObject();

            JsonObject src = new JsonObject();
            src.addProperty("node_id", node_ids.get(source_node.getParent()));
            src.addProperty("attribute_name", source_node.getAttributeName());
            link.add("src", src);

            JsonObject dst = new JsonObject();
            dst.addProperty("node_id", node_ids.get(dest_node.getParent()));
            dst.addProperty("attribute_name", dest_node.getAttributeName());
            link.add("dst", dst);

            return link;
        }
        return null;
    }

    public void load (String file_path) {
        JsonObject save_data = JsonUtils.loadJson(file_path);
        if(save_data.has("nodes")){
            JsonElement nodes = save_data.get("nodes");
            if(nodes.isJsonArray()){
                JsonArray nodes_array = nodes.getAsJsonArray();
                for(int i = 0; i < nodes_array.size(); i++){
                    Node node = NodeRegistry.getInstance().getNodeFromClass(nodes_array.get(i).getAsJsonObject());
                    this.addNode(node);
                }
            }
        }
    }

    public void removeNodes(int[] selected_nodes) {
        for(int node_id : selected_nodes){
            if ( node_id >= 0 ) {
                this.nodes.remove(node_id);
            }
        }
    }
}
