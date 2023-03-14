package nodegraph;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import editor.Editor;
import graphics.GLDataType;
import graphics.GLPrimitive;
import graphics.GLStruct;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import nodegraph.nodes.OutflowNode;
import nodegraph.pin.*;
import util.BidirectionalLinkedHashedSet;
import util.ObservableLinkedHashMap;

import java.util.*;

public abstract class Node{

    private boolean flow_controls_enabled = true;
    private boolean force_render_inflow = false;
    private boolean force_render_outflow = false;

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
    private final ObservableLinkedHashMap<String, Pin> pins = new ObservableLinkedHashMap<>();

    public Node(){
        this.id = ++next_id;

        this.inflow_id = -1;
        this.outflow_id = -1;

        // Push some nice colors
        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.CODE_NODE_TITLE);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.CODE_NODE_TITLE_HOVER);

        // Reserve some Pins
        this.addPin(new InflowPin(this, INFLOW, PinType.FLOW));
        this.addPin(new OutflowPin(this, OUTFLOW, PinType.FLOW));
    }

    private Pin addPin(Pin to_add){
        pins.put(to_add.getAttributeName(), to_add);
        return to_add;
    }

    private Pin addPin(String pin_name, PinType type, PinDirection direction){
        Pin to_add = (direction.equals(PinDirection.SOURCE)) ? new OutflowPin(this, pin_name, type) : new InflowPin(this, pin_name, type);
        pins.put(pin_name, to_add);
        return to_add;
    }

    // Helper functions to add and remove attributes
    public InflowPin addInputPin(String name, GLStruct value){
        if(!hasAttributeWithName(name)) {
            addPin(name, PinType.DATA, PinDirection.DESTINATION);
        }
        this.input_values.put(name, value);
        Pin pin =  pins.getOrDefault(name, null);
        if(pin instanceof OutflowPin){
            System.err.println(String.format("Error: Trying to cast an outflow pin to an inflow pin. This is probably because %s was already defined to be an output pin.", name));
        }
        return (InflowPin)pin;
    }

    public InflowPin addInputPin(String name, GLDataType type){
        return this.addInputPin(name, new GLPrimitive(name, type));
    }

    public OutflowPin addOutputPin(String name, GLStruct value){
        if(!hasAttributeWithName(name)) {
            addPin(name, PinType.DATA, PinDirection.SOURCE);
        }
        this.output_values.put(name, value);
        return (OutflowPin) pins.getOrDefault(name, null);
    }

    public OutflowPin addOutputPin(String name, GLDataType type){
        return this.addOutputPin(name, new GLPrimitive(name, type));
    }

    public OutflowPin addOutflowPin(String  name){
        OutflowPin pin = new OutflowPin(this, name, PinType.FLOW);
        this.addPin(pin);
        this.output_values.put(name, null);
        return pin;
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

    public void applyStyle(int style, int color){
        this.colors.put(style, color);
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
        renderFlowControls();
        ImGui.textColored(255, 255, 255, 255, this.title);
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

    public InflowPin getInflow() {
        return (InflowPin) getPinFromID(getPinIDFromName(INFLOW));
    }

    public OutflowPin getOutflow() {
        return (OutflowPin) getPinFromID(getPinIDFromName(OUTFLOW));
    }

    //TODO custom function override.
    protected final void renderInputAttribute(String param_name) {
        if(this.attribute_ids.containsKey(param_name)) {
            Pin pin = getPinFromName(param_name);
            GLStruct value = this.input_values.get(param_name);
            boolean push_color = (value instanceof GLPrimitive);
            if(push_color) {
                ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.getTypeColor(((GLPrimitive)value).getPrimitiveType()));
            }
            ImNodes.beginInputAttribute(this.attribute_ids.get(param_name), pin.getShape());
            ImGui.text(param_name);
            ImNodes.endInputAttribute();
            if (push_color) {
                ImNodes.popColorStyle();
            }
        }
    }

    protected final void renderInputAttribute(String param_name, AttributeOverride render_override) {
        if(this.attribute_ids.containsKey(param_name)) {
            Pin pin = getPinFromName(param_name);
            GLStruct value = this.input_values.get(param_name);
            boolean push_color = (value instanceof GLPrimitive);
            if(push_color) {
                ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.getTypeColor(((GLPrimitive)value).getPrimitiveType()));
            }
            ImNodes.beginInputAttribute(this.attribute_ids.get(param_name), pin.getShape());
            render_override.render();
            ImNodes.endInputAttribute();
            if (push_color) {
                ImNodes.popColorStyle();
            }
        }
    }

    protected final void renderOutputAttribute(String out_name) {
        if(this.attribute_ids.containsKey(out_name)) {
            Pin pin = getPinFromName(out_name);
            GLStruct value = this.output_values.get(out_name);
            boolean push_color = (value instanceof GLPrimitive);
            if(push_color) {
                ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.getTypeColor(((GLPrimitive)value).getPrimitiveType()));
            }
            ImNodes.beginOutputAttribute(this.attribute_ids.get(out_name), pin.getShape());
            ImGui.text(out_name);
            ImNodes.endOutputAttribute();
            if (push_color) {
                ImNodes.popColorStyle();
            }
        }
    }

    protected final void renderOutputAttribute(String out_name, AttributeOverride render_override) {
        if(this.attribute_ids.containsKey(out_name)) {
            Pin pin = getPinFromName(out_name);
            GLStruct value = this.output_values.get(out_name);
            boolean push_color = (value instanceof GLPrimitive);
            if(push_color) {
                ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.getTypeColor(((GLPrimitive)value).getPrimitiveType()));
            }
            ImNodes.beginOutputAttribute(this.attribute_ids.get(out_name), pin.getShape());
            render_override.render();
            ImNodes.endOutputAttribute();
            if (push_color) {
                ImNodes.popColorStyle();
            }
        }
    }

    private void renderFlowControls(){
        if((input_values.size() > 0 || force_render_inflow) && flow_controls_enabled) {
            ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.WHITE);
            ImNodes.beginInputAttribute(inflow_id, getInflow().isConnected() ? ImNodesPinShape.TriangleFilled : ImNodesPinShape.Triangle);
            ImNodes.endInputAttribute();
            ImNodes.popColorStyle();
            ImGui.sameLine();
        }
        if(this.width > 0) {
            ImGui.image(0, this.width, 0);
        }
        if((output_values.size() > 0 || force_render_outflow) && flow_controls_enabled) {
            ImGui.sameLine();
            ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.WHITE);
            ImNodes.beginOutputAttribute(outflow_id, getOutflow().isConnected() ? ImNodesPinShape.TriangleFilled : ImNodesPinShape.Triangle);
            ImNodes.endOutputAttribute();
            ImNodes.popColorStyle();
        }
    }

    // Links
    public void renderLinks(){
        for(Pin pin : this.pins.getValues()){
            pin.renderLinks();
        }
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

    public final void generateIntermediate(JsonArray evaluation_stack){
        // First we are going to push our serialization onto the stack.
        // This serialization is anything we want to
         serialize(evaluation_stack);

        LinkedList<OutflowPin> outflows = new LinkedList<>();
        for(Pin outflow : this.pins.getValues()){
            if(outflow instanceof OutflowPin){
                if(outflow.getType().equals(PinType.FLOW)) {
                    outflows.add((OutflowPin) outflow);
                }
            }
        }

        if(outflows.size() == 1){
            OutflowPin outflow = outflows.getFirst();
            if(outflow.isConnected()) {
                outflow.getConnection().getParent().generateIntermediate(evaluation_stack);
            }
        }else{
            JsonArray multi_out = new JsonArray();
            for(OutflowPin outflow : outflows){
                outflow.getConnection().getParent().generateIntermediate(multi_out);
            }
            evaluation_stack.add(multi_out);
        }
    }

    // Default serialize dose nothing.
    public void serialize(JsonArray evaluation_stack){};
    public abstract JsonElement getValueOfPin(OutflowPin outflow);

    @Deprecated
    public String getPinNameFromID(int pin_id) {
        return this.attribute_ids.getKey(pin_id);
    }

    public boolean hasPinWithID(int pin_id) {
        return this.attribute_ids.containsValue(pin_id) || inflow_id == pin_id || outflow_id == pin_id;
    }

    public Pin getPinFromName(String pin_name){
        return this.getPinFromID(this.getPinIDFromName(pin_name));
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

    public int getNumDataInflows(){
        return this.input_values.size();
    }

    public int getNumDataOutflows(){
        return this.output_values.size();
    }

    public Collection<InflowPin> getNodeInflowPins(){
        //TODO optimize later
        LinkedList<InflowPin> inflow_pins = new LinkedList<>();

        for(Pin pin : pins.getValues()){
            if(pin.getType().equals(PinType.DATA) && pin instanceof InflowPin){
                inflow_pins.add((InflowPin) pin);
            }
        }

        return inflow_pins;
    }

    public OutflowPin getPinSource(String inflow_pin_name){
        if(this.pins.containsKey(inflow_pin_name)){
            Pin pin = this.pins.get(inflow_pin_name);
            if(pin instanceof InflowPin){
                return getPinSource((InflowPin) pin);
            }
        }
        return null;
    }

    public OutflowPin getPinSource(InflowPin inflow){
        if(inflow.isConnected()){
            return inflow.getLink();
        }
        return null;
    }

    // Rendering stuff
    public void disableFlowControls(){
        this.flow_controls_enabled = false;
    }

    public void forceRenderInflow(){
        this.force_render_inflow = true;
    }

    public void forceRenderOutflow(){
        this.force_render_outflow = true;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

}