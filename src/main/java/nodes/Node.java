package nodes;

import com.google.gson.JsonElement;
import editor.Editor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.extension.imnodes.flag.ImNodesStyleVar;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public abstract class Node{

    @FunctionalInterface
    public interface AttributeOverride{
        void render();
    }

    private static int next_id = 0;

    protected String title = "";
    protected int width = 256;

    private final int id;
    private int input_id;
    private int output_id;

    private HashMap<Integer, Integer> colors = new HashMap<>();

    protected final LinkedHashMap<String, JsonElement> input_values  = new LinkedHashMap<>();
    protected final LinkedHashMap<String, JsonElement> output_values = new LinkedHashMap<>();

    private final LinkedHashMap<String, Integer> attribute_ids = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, String> inverse_attribute_ids = new LinkedHashMap<>();

    private LinkedHashSet<Link> links = new LinkedHashSet<>();

    public Node(){
        this.id = ++next_id;

        this.input_id = -1;
        this.output_id = -1;

        // Push some nice colors
        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.CODE_NODE_TITLE);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.CODE_NODE_TITLE_HOVER);

    }

    public JsonElement getParameterValue(String parameter_name) {
        return this.input_values.get(parameter_name);
    }

    public JsonElement getOutputValue(String output_name) {
        return this.output_values.get(output_name);
    }

    public void applyStyle(int style, NodeColors color){
        this.colors.put(style, color.getColor());
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public final void renderNode(){
        // First we need to allocate a bunch of IDS for the future
        for(String parameter_name : input_values.keySet()){
            int id = Editor.getInstance().getNextAvailableID(); // Fixed with ++next_id;
            this.attribute_ids.put(parameter_name, id);
            this.inverse_attribute_ids.put(id, parameter_name);
        }
        for(String output_name : output_values.keySet()){
            int id = Editor.getInstance().getNextAvailableID();
            this.attribute_ids.put(output_name, id);
            this.inverse_attribute_ids.put(id, output_name);
        }

        // Push all of our style attributes onto the stack.
        this.colors.forEach((style, color) -> {
            ImNodes.pushColorStyle(style, color);
        });

        // Begin a node.
        ImNodes.beginNode(this.id);
        ImGui.beginGroup();
        // Title Bar
        ImNodes.beginNodeTitleBar();
        ImGui.textColored(255, 255, 255, 255, this.title);
        renderFlowControls();
        ImNodes.endNodeTitleBar();

        render();

        ImGui.endGroup();

        ImNodes.endNode();

        this.colors.forEach((style, color) -> {
            ImNodes.popColorStyle();
        });
    }

    public abstract void render();

    public void renderLinks(){
        // Now we can render all of our links
        for(Link link : links){
            link.renderLink();
        }
    }

    //Any nodes can be linked together based on field names.
    public int link(String element_name, Node destination, String destination_element_name){
        // We can make this link!
        if(this.output_values.containsKey(element_name) && destination.input_values.containsKey(destination_element_name)){
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

    public String getPinNameFromID(Integer pin_id){
        return this.inverse_attribute_ids.getOrDefault(pin_id, null);
    }

    public int getID() {
        return id;
    }

    //TODO custom function override.
    protected final void renderInputAttribute(String param_name) {
        if(this.attribute_ids.containsKey(param_name)) {
            ImNodes.beginInputAttribute(this.attribute_ids.get(param_name), ImNodesPinShape.Circle);
            ImGui.text(param_name + " " + this.getParameterValue(param_name));
            ImNodes.endInputAttribute();
        }
    }

    protected final void renderInputAttribute(String param_name, AttributeOverride render_override) {
        if(this.attribute_ids.containsKey(param_name)) {
            ImNodes.beginInputAttribute(this.attribute_ids.get(param_name), ImNodesPinShape.Circle);
            render_override.render();
            ImNodes.endInputAttribute();
        }
    }

    protected final void renderOutputAttribute(String out_name) {
        if(this.attribute_ids.containsKey(out_name)) {
            ImNodes.beginOutputAttribute(this.attribute_ids.get(out_name), ImNodesPinShape.Circle);
            ImGui.text(out_name);
            ImNodes.endOutputAttribute();
        }
    }

    protected final void renderOutputAttribute(String out_name, AttributeOverride render_override) {
        if(this.attribute_ids.containsKey(out_name)) {
            ImNodes.beginOutputAttribute(this.attribute_ids.get(out_name), ImNodesPinShape.Circle);
            render_override.render();
            ImNodes.endOutputAttribute();
        }
    }

    private void renderFlowControls(){
        ImGui.newLine();
        this.input_id = Editor.getInstance().getNextAvailableID();
        if(input_values.size() > 0) {
            ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.WHITE);
            ImNodes.beginInputAttribute(input_id, ImNodesPinShape.Triangle);
            ImNodes.endInputAttribute();
            ImNodes.popColorStyle();
        }
        ImGui.sameLine();
        ImGui.image(0, this.width, 0);
        ImGui.sameLine();
        this.output_id = Editor.getInstance().getNextAvailableID();
        if(output_values.size() > 0) {
            ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.WHITE);
            ImNodes.beginOutputAttribute(output_id, ImNodesPinShape.Triangle);
            ImNodes.endOutputAttribute();
            ImNodes.popColorStyle();
        }
    }

    public int getInputId() {
        return input_id;
    }

    public int getOutputId() {
        return output_id;
    }

    protected void renameOutput(String initial_name, String s) {

    }

    // Slow maybe a different datastructure would be better.
    private <T> void rename(LinkedHashMap<String, T> map, String old_name, String new_name){
        if(map.containsKey(old_name)) {
            // Find the index
            int index = 0;
            for (String key : map.keySet()) {
                if(key.equals(old_name)){
                    // Dequeue the rest of the map
                    LinkedList<T> rest = new LinkedList<>();
                    int rest_index = 0;
                    for(String key_rest : map.keySet()){
                        if(rest_index > index){
                            // We are past the insertion point
                            rest.addLast(map.get(key_rest));
                            map.remove(key_rest);
                        }
                        rest_index++;
                    }
                    // Insert here
                }
                index++;
            }
        }
    }
}
