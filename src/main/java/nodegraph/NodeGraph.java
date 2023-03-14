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
import nodegraph.pin.Pin;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
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
}
