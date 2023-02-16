package nodes;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;

public class NodeGraph {
    private LinkedHashMap<Integer, Node> nodes = new LinkedHashMap<>();

    public NodeGraph() {

    }

    public void addNode(Node node){
        this.nodes.put(node.getID(), node);
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

    public NodeAttributePair getNodeAndPinFromID(int pin_id){
        for(Node node : nodes.values()){
            String pin_name = node.getPinNameFromID(pin_id);
            if(pin_name != null){
                return new NodeAttributePair(node, pin_name, node.getID(), pin_id);
            }
        }
        return null;
    }

}
