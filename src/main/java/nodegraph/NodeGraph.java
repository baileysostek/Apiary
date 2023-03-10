package nodegraph;

import com.google.gson.JsonObject;
import nodegraph.pin.Pin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class NodeGraph {
    private LinkedHashMap<Integer, Node> nodes = new LinkedHashMap<>();
    private LinkedHashMap<Class<Node>, LinkedList<Node>> typed_nodes = new LinkedHashMap<>();

    public NodeGraph() {

    }

    public void addNode(Node node){
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
}
