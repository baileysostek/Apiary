package nodegraph;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import editor.Editor;
import graphics.GLDataType;
import graphics.GLPrimitive;
import graphics.GLStruct;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import nodegraph.link.Link;
import nodegraph.pin.Pin;
import nodegraph.pin.PinDirection;
import nodegraph.pin.PinType;
import util.BidirectionalLinkedHashedSet;

import java.util.*;

public abstract class Node{

    @FunctionalInterface
    public interface AttributeOverride{
        void render();
    }

    private static final String INFLOW = "inflow";
    private static final String OUTFLOW = "outflow";
    private static final HashSet<String> RESERVED_NAMES = new HashSet<>(Arrays.asList(new String[]{
        INFLOW,
        OUTFLOW,
    }));

    private static int next_id = 0;

    protected String title = "";
    protected int width = 256;

    private final int id;
    private int inflow_id;
    private int outflow_id;

    private HashMap<Integer, Integer> colors = new HashMap<>();

    private final LinkedHashMap<String, GLStruct> input_values  = new LinkedHashMap<>();
    private final LinkedHashMap<String, GLStruct> output_values = new LinkedHashMap<>();

    private final BidirectionalLinkedHashedSet<String, Integer> attribute_ids = new BidirectionalLinkedHashedSet<>();
    private final LinkedHashMap<String, Pin> pins = new LinkedHashMap<>();

    private LinkedHashSet<Link> links = new LinkedHashSet<>();

    public Node(){
        this.id = ++next_id;

        this.inflow_id = -1;
        this.outflow_id = -1;

        // Push some nice colors
        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.CODE_NODE_TITLE);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.CODE_NODE_TITLE_HOVER);

        // Reserve some Pins
        addPin(INFLOW, PinType.FLOW, PinDirection.DESTINATION);
        addPin(OUTFLOW, PinType.FLOW, PinDirection.SOURCE);
    }

    private void addPin(String pin_name, PinType type, PinDirection direction){
        pins.put(pin_name, new Pin(this, pin_name, type, direction));
    }

    // Helper functions to add and remove attributes
    public void addInputAttribute(String name, GLStruct value){
        if(!hasAttributeWithName(name)) {
            addPin(name, PinType.DATA, PinDirection.DESTINATION);
        }
        this.input_values.put(name, value);
    }

    public void addInputAttribute(String name, GLDataType type){
        this.addInputAttribute(name, new GLPrimitive(name, type));
    }

    public void addOutputAttribute(String name, GLStruct value){
        if(!hasAttributeWithName(name)) {
            addPin(name, PinType.DATA, PinDirection.SOURCE);
        }
        this.output_values.put(name, value);
    }

    public void addOutputAttribute(String name, GLDataType type){
        this.addOutputAttribute(name, new GLPrimitive(name, type));
    }

    public boolean hasAttributeWithName(String attribute_name){
        return (this.input_values.containsKey(attribute_name)) || (this.output_values.containsKey(attribute_name) || RESERVED_NAMES.contains(attribute_name));
    }

    public Collection<String> getInputNames() {
        return input_values.keySet();
    }

    public void removeAttribute(String name){
        if(hasAttributeWithName(name)) {
            this.output_values.remove(name);
            this.input_values.remove(name);

            this.attribute_ids.remove(name);

            System.out.println("Removed");
        }
    }

    public Collection<String> getOutputNames() {
        return output_values.keySet();
    }

    public Collection<GLStruct> getInputValues() {
        return input_values.values();
    }

    public Collection<GLStruct> getOutputValues() {
        return output_values.values();
    }

    public JsonElement getInputValue(String parameter_name) {
//        return this.input_values.get(parameter_name);
        if(this.input_values.containsKey(parameter_name)){
            this.input_values.get(parameter_name).serialize();
        }
        return new JsonNull();
    }

    public GLStruct getInputType(String parameter_name) {
        if(this.input_values.containsKey(parameter_name)){
            return this.input_values.get(parameter_name);
        }
        return null;
    }

    public JsonElement getOutputValue(String output_name) {
        if(this.output_values.containsKey(output_name)){
            this.output_values.get(output_name).serialize();
        }
        return new JsonNull();
    }

    public GLStruct getOutputType(String parameter_name) {
        if(this.output_values.containsKey(parameter_name)){
            return this.output_values.get(parameter_name);
        }
        return null;
    }

    public void applyStyle(int style, NodeColors color){
        this.colors.put(style, color.getColor());
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public final void renderNode(){
        // Set ourInflow and outflow IDS
        this.inflow_id = Editor.getInstance().getNextAvailableID();
        this.attribute_ids.add(INFLOW, inflow_id);
        this.outflow_id = Editor.getInstance().getNextAvailableID();
        this.attribute_ids.add(OUTFLOW, outflow_id);

        // First we need to allocate a bunch of IDS for the future
        for(String parameter_name : input_values.keySet()){
            int id = Editor.getInstance().getNextAvailableID(); // Fixed with ++next_id;
            this.attribute_ids.add(parameter_name, id);
        }
        for(String output_name : output_values.keySet()){
            int id = Editor.getInstance().getNextAvailableID();
            this.attribute_ids.add(output_name, id);
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

    public void render(){
        int index = 0;
        String[] output_names = this.output_values.keySet().toArray(new String[0]);
        for(String input_name : this.input_values.keySet()){
            renderInputAttribute(input_name);
            if(index < output_values.size()){
                String out_name = output_names[index];
                ImGui.sameLine();
                renderOutputAttribute(out_name);
            }
            index++;
        }
        // Render the output nodes.
        int output_index = 0;
        for(String output_name : output_names){
            if(output_index >= index) {
                renderOutputAttribute(output_name);
            }
            output_index++;
        }
    }

    public int getAttributeByName(String attribute_name){
        return this.attribute_ids.getValueOrDefault(attribute_name, -1);
    }

    public int getID() {
        return id;
    }

    public int getInflowID() {
        return inflow_id;
    }

    public int getOutflowID() {
        return outflow_id;
    }

    //TODO custom function override.
    protected final void renderInputAttribute(String param_name) {
        if(this.attribute_ids.containsKey(param_name)) {
            ImNodes.beginInputAttribute(this.attribute_ids.get(param_name), ImNodesPinShape.Circle);
            ImGui.text(param_name);
            ImNodes.endInputAttribute();
        }
    }

    protected final void renderInputAttribute(String param_name, AttributeOverride render_override) {
        if(this.attribute_ids.containsKey(param_name)) {
            GLStruct value = this.input_values.get(param_name);
            boolean push_color = (value instanceof GLPrimitive);
            if(push_color) {
                ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.getTypeColor(((GLPrimitive)value).getPrimitiveType()));
            }
            ImNodes.beginInputAttribute(this.attribute_ids.get(param_name), ImNodesPinShape.Circle);
            render_override.render();
            ImNodes.endInputAttribute();
            if (push_color) {
                ImNodes.popColorStyle();
            }
        }
    }

    protected final void renderOutputAttribute(String out_name) {
        if(this.attribute_ids.containsKey(out_name)) {
            GLStruct value = this.output_values.get(out_name);
            boolean push_color = (value instanceof GLPrimitive);
            if(push_color) {
                ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.getTypeColor(((GLPrimitive)value).getPrimitiveType()));
            }
            ImNodes.beginOutputAttribute(this.attribute_ids.get(out_name), ImNodesPinShape.Circle);
            ImGui.text(out_name);
            ImNodes.endOutputAttribute();
            if (push_color) {
                ImNodes.popColorStyle();
            }
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
        if(input_values.size() > 0) {
            ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.WHITE);
            ImNodes.beginInputAttribute(inflow_id, ImNodesPinShape.Triangle);
            ImNodes.endInputAttribute();
            ImNodes.popColorStyle();
        }
        ImGui.sameLine();
        ImGui.image(0, this.width, 0);
        ImGui.sameLine();
        if(output_values.size() > 0) {
            ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.WHITE);
            ImNodes.beginOutputAttribute(outflow_id, ImNodesPinShape.Triangle);
            ImNodes.endOutputAttribute();
            ImNodes.popColorStyle();
        }
    }

    // Links
    public void renderLinks(){
        // Now we can render all of our links
        for(Link link : links){
            link.renderLink();
        }
    }

    public void link(Pin source, Pin destination){
        this.links.add(new Link(source, destination));
    }

    //TODO
    protected void renameAttribute(String initial_name, String new_name) {
        // Disallow renaming names on the reserved list.
        if(RESERVED_NAMES.contains(initial_name)){
            // TODO: Throw error
            System.err.println(String.format("Error: Cannot rename the reserved attribute [%s] to the new name [%s] because this name is reserved.", initial_name, new_name));
            return;
        }

        if(RESERVED_NAMES.contains(new_name)){
            // TODO: Throw error
            System.err.println(String.format("Error: Cannot rename [%s] to the reserved name [%s].", initial_name, new_name));
            return;
        }

        // Disallow renaming to a name we already have.
        if(hasAttributeWithName(new_name)){
            //TODO: Throw error
            System.err.println(String.format("Error: Cannot rename [%s] to [%s] because an attribute with that name already exists on this node.", initial_name, new_name));
            return;
        }

        // Get the attribute we are renaming
        if(hasAttributeWithName(initial_name)){
            if(input_values.containsKey(initial_name)){
                // Rename the input
                GLStruct type = this.input_values.get(initial_name);
                this.input_values.remove(initial_name);
                this.input_values.put(new_name, type);
            }else if(output_values.containsKey(initial_name)){
                // Rename the output
                GLStruct type = this.output_values.get(initial_name);
                this.output_values.remove(initial_name);
                this.output_values.put(new_name, type);
            }
        }

        // Rename the pin
        if(pins.containsKey(initial_name)){
            Pin pin = pins.get(initial_name);
            pin.rename(new_name);
            pins.remove(initial_name);
            pins.put(new_name, pin);
        }

        // Update the input name

        // Update the Cached ID.
        int id = this.attribute_ids.get(initial_name);
        this.attribute_ids.add(new_name, id);
    }

    // Slow, maybe a different datastructure would be better.
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

    public JsonElement serialize(){
        JsonObject save_data = new JsonObject();
        return save_data;
    }

    public Node deserialize(JsonElement element){
        return this;
    }

    @Deprecated
    public String getPinNameFromID(int pin_id) {
        return this.attribute_ids.getKey(pin_id);
    }

    public boolean hasPinWithID(int pin_id) {
        return this.attribute_ids.containsValue(pin_id) || inflow_id == pin_id || outflow_id == pin_id;
    }

    public Pin getPinFromID(int hovered_pin) {
        String pin_name = this.attribute_ids.getKeyOrDefault(hovered_pin, "");
        if(pin_name.isEmpty()){
            // Check if this is the inflow or outflow pin
            if(hovered_pin == inflow_id){
                pin_name = INFLOW;
            }
            if(hovered_pin == outflow_id){
                pin_name = OUTFLOW;
            }
        }
        return this.pins.getOrDefault(pin_name, null);
    }

    public int getPinIDFromName(String attribute_name){
        return this.attribute_ids.getValueOrDefault(attribute_name, -1);
    }

}