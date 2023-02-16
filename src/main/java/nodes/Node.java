package nodes;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import editor.Editor;
import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesPinShape;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class Node{

    private static int next_id = 0;

    private final int id;
    private final Nodes node_type;
    private final LinkedHashMap<String, JsonPrimitive> parameter_values = new LinkedHashMap<>();
    private final LinkedHashMap<String, JsonPrimitive> output_values    = new LinkedHashMap<>();
    private final LinkedHashMap<String, Integer> attribute_ids = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, String> inverse_attribute_ids = new LinkedHashMap<>();

    private LinkedHashSet<Link> links = new LinkedHashSet<>();

    public Node(Nodes node_type){
        this.node_type = node_type;
        // Set the ID
        this.id = ++next_id;

        // Populate our parameter map based off our our template.
        for(String param_name : this.node_type.getParameterNames()){
            this.parameter_values.put(param_name, new JsonPrimitive(""));
        }
        for(String output_name : this.node_type.getOutputNames()){
            this.output_values.put(output_name, new JsonPrimitive(""));
        }
    }

    public JsonElement getParameterValue(String parameter_name) {
        return this.parameter_values.get(parameter_name);
    }

    public JsonElement getOutputValue(String output_name) {
        return this.output_values.get(output_name);
    }

    public final void renderNode(){
        // First we need to allocate a bunch of IDS for the future
        for(String parameter_name : this.node_type.getParameterNames()){
            int id = Editor.getInstance().getNextAvailableID();
            this.attribute_ids.put(parameter_name, id);
            this.inverse_attribute_ids.put(id, parameter_name);
        }
        for(String output_name : this.node_type.getOutputNames()){
            int id = Editor.getInstance().getNextAvailableID();
            this.attribute_ids.put(output_name, id);
            this.inverse_attribute_ids.put(id, output_name);
        }

        // set the titlebar color of an individual node
        // TODO get from lookup
        ImNodes.pushColorStyle(ImNodesColorStyle.TitleBar, ImColor.rgba(11, 109, 191, 255));
        ImNodes.pushColorStyle(ImNodesColorStyle.TitleBar, ImColor.rgba(81, 148, 204, 255));

        ImNodes.beginNode(Editor.getInstance().getNextAvailableID());
        // Title Bar
        ImNodes.beginNodeTitleBar();
        ImGui.text(this.node_type.getNodeID());
        ImNodes.endNodeTitleBar();

        // Input
        int index = 0;
        Object[] out_names = this.node_type.getOutputNames().toArray();
        for(String param_name : this.node_type.getParameterNames()){
            ImNodes.beginInputAttribute(attribute_ids.get(param_name), ImNodesPinShape.Triangle);
            ImGui.text(param_name+ " " + this.getParameterValue(param_name));
            ImNodes.endInputAttribute();
            if(index < this.node_type.getOutputNames().size()){
                String out_name = (String) out_names[index];
                ImGui.sameLine();
                ImNodes.beginOutputAttribute(attribute_ids.get(out_name), ImNodesPinShape.Triangle);
                ImGui.text(out_name);
                ImNodes.endOutputAttribute();
            }
            index++;
        }
        int output_index = 0;
        for(String output_name : this.node_type.getOutputNames()){
            if(output_index >= index) {
                ImNodes.beginOutputAttribute(attribute_ids.get(output_name), ImNodesPinShape.Triangle);
                ImGui.text(output_name);
                ImNodes.endOutputAttribute();
            }
            output_index++;
        }

        ImNodes.endNode();

        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
    }

    public void renderLinks(){
        // Now we can render all of our links
        for(Link link : links){
            link.renderLink();
        }
    }

    public void toIR() {
        JsonElement out = this.node_type.toIR(this);
        String out_glsl = NodeManager.getInstance().transpile(out);
        System.out.println(out_glsl);
    }


    public int link(String element_name, Node destination, String destination_element_name){
        // We can make this link!
        if(this.node_type.getOutputNames().contains(element_name) && destination.node_type.getParameterNames().contains(destination_element_name)){
            this.links.add(new Link(this, element_name, destination, destination_element_name));
        }
        return -1;
    }

    public int getAttributeByName(String attribute_name){
        if(this.attribute_ids.containsKey(attribute_name)) {
            return this.attribute_ids.get(attribute_name);
        }
        return -1;
    }

    public int getID() {
        return id;
    }

    public String getPinNameFromID(int pin_id){
        return inverse_attribute_ids.getOrDefault(pin_id, null);
    }
}
