package nodegraph;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import editor.Editor;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;
import nodegraph.pin.Pin;
import nodegraph.pin.PinType;

import java.util.*;

public abstract class Node{

    // Static interfaces and constants used so nodes dont break easily.
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

    // Variables for each node.

    private boolean inflow_controls_enabled = true;
    private boolean outflow_controls_enabled = true;

    private boolean force_render_inflow = false;
    private boolean force_render_outflow = false;

    private boolean node_processes_own_flow = false;

    private final float initial_node_pos_x;
    private final float initial_node_pos_y;

    private float node_pos_x = 0.0f;
    private float node_pos_y = 0.0f;

    protected String title = "";
    protected int width = 256;

    private final int id; // A unique ID used by ImNodes to guarantee that this node is unique.
    private final int reference_id; // An ID used internally for addressing an ID when loading data.
    private int inflow_id;
    private InflowPin inflow;
    private int outflow_id;
    private OutflowPin outflow;

    private HashMap<Integer, Integer> colors = new HashMap<>();

    private LinkedHashMap<String, InflowPin> inputs = new LinkedHashMap<>();
    private LinkedHashMap<String, OutflowPin> outputs = new LinkedHashMap<>();

    private LinkedHashMap<Integer, Pin> pin_ids = new LinkedHashMap<>();

    public Node(JsonObject initialization_data){
        // Determine ID
        this.id = ++next_id;
        if(initialization_data.has("id")){
            this.reference_id = initialization_data.get("id").getAsInt();
        }else{
            this.reference_id = this.id;
        }

        if(initialization_data.has("pos_x")){
            this.initial_node_pos_x = initialization_data.get("pos_x").getAsFloat();
        }else{
            this.initial_node_pos_x = 0.0f;
        }
        setPositionX(this.initial_node_pos_x);

        if(initialization_data.has("pos_y")){
            this.initial_node_pos_y = initialization_data.get("pos_y").getAsFloat();
        }else{
            this.initial_node_pos_y = 0.0f;
        }
        setPositionY(this.initial_node_pos_y);

        this.inflow_id = -1;
        this.outflow_id = -1;

        // Push some nice colors
        this.applyStyle(ImNodesColorStyle.TitleBar, NodeColors.CODE_NODE_TITLE);
        this.applyStyle(ImNodesColorStyle.TitleBarHovered, NodeColors.CODE_NODE_TITLE_HOVER);

        // Reserve some Pins
        this.inflow = new InflowPin(this, INFLOW, PinType.FLOW);
        this.outflow = new OutflowPin(this, OUTFLOW, PinType.FLOW, null);

        // Do a render just so the node exists in the internal state?
//        this.render();
    }

    // Guarenteed to run after all nodes and links have been loaded.
    // Any data which relies on other nodes to exist should be intereacted with at this point.
    public void onLoad(JsonObject initialization_data){}

    public final JsonObject generateSaveData(){
        JsonObject save_object = nodeSpecificSaveData();
        // Add the ID

        // Add the pins

        // We dont need to add inflow and outflow IDS because they are implicit.
        save_object.addProperty("class", this.getClass().getName());

        // We are going to encode the screen position of this object.
        save_object.addProperty("pos_x", this.node_pos_x + ImNodes.getNodeEditorSpacePosX(id) - 100); // The 100 here is kindof a magic number. For whatever reason when you set a node to 0,0 it appears at 100,100
        save_object.addProperty("pos_y", this.node_pos_y + ImNodes.getNodeEditorSpacePosY(id) - 100);

        return save_object;
    }

    public JsonObject nodeSpecificSaveData(){
        return new JsonObject();
    }

    // Helper functions to add and remove attributes
    public InflowPin addInputPin(String name, GLDataType ... accepted_types){
        if(!hasPinWithName(name)) {
            this.inputs.put(name, new InflowPin(this, name, PinType.DATA, accepted_types));
        }else{
            this.inputs.get(name).setType(accepted_types);
        }
        return inputs.getOrDefault(name, null);
    }

    public InflowPin addInflowPin(String  name){
        if(!hasPinWithName(name)) {
            this.inputs.put(name, new InflowPin(this, name, PinType.FLOW));
        }
        return inputs.getOrDefault(name, null);
    }

    public OutflowPin addOutputPin(String name, GLDataType type){
        if(!hasPinWithName(name)) {
            this.outputs.put(name, new OutflowPin(this, name, PinType.DATA, type));
        }
        return outputs.getOrDefault(name, null);
    }

    public OutflowPin addOutflowPin(String  name){
        if(!hasPinWithName(name)) {
            this.outputs.put(name, new OutflowPin(this, name, PinType.FLOW, null));
        }
        return outputs.getOrDefault(name, null);
    }

    public boolean hasPinWithName(String attribute_name){
        return (this.inputs.containsKey(attribute_name)) || (this.outputs.containsKey(attribute_name) || RESERVED_NAMES.contains(attribute_name));
    }

    public Collection<String> getInputNames() {
        return this.inputs.keySet();
    }

    public Collection<String> getOutputNames() {
        return this.outputs.keySet();
    }

    public void removePin(String name){
        if(RESERVED_NAMES.contains(name)){
            return; // Cannot remove a reserved name.
        }

        // Remove from input or output.
        String predicate = "inflow";
        loop:{
            if (this.inputs.containsKey(name)) {
                this.inputs.remove(name);
                break loop;
            }
            if (this.outputs.containsKey(name)) {
                this.outputs.remove(name);
                predicate = "outflow";
            }
        }

        System.out.println(String.format("Removed %s node named %s.", predicate, name));
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

    private void regeneratePinIDs(){
        pin_ids.clear();
        // Set ourInflow and outflow IDS
        this.inflow_id = Editor.getInstance().getNextAvailableID();
        this.inflow.setId(inflow_id);
        this.inflow.setRenderedThisFrame(false);
        pin_ids.put(inflow_id, inflow);
        this.outflow_id = Editor.getInstance().getNextAvailableID();
        this.outflow.setId(outflow_id);
        this.outflow.setRenderedThisFrame(false);
        pin_ids.put(outflow_id, outflow);

        // First we need to allocate a bunch of IDS for the future
        for(InflowPin inflow : inputs.values()){
            int id = Editor.getInstance().getNextAvailableID(); // Fixed with ++next_id;
            inflow.setId(id);
            this.inflow.setRenderedThisFrame(false);
            pin_ids.put(id, inflow);
        }
        for(OutflowPin outflow : outputs.values()){
            int id = Editor.getInstance().getNextAvailableID();
            outflow.setId(id);
            this.outflow.setRenderedThisFrame(false);
            pin_ids.put(id, outflow);
        }
    }

    public void setPositionX(float position_x){
        this.node_pos_x = position_x;
    }
    public void setPositionY(float position_y){
        this.node_pos_y = position_y;
    }

    public float getPositionX() {
        return node_pos_x;
    }

    public float getPositionY() {
        return node_pos_y;
    }

    public final void renderNode(){

        // associate each pin with a unique id
        regeneratePinIDs();

        // Push all of our style attributes onto the stack.
        this.colors.forEach((style, color) -> {
            ImNodes.pushColorStyle(style, color);
        });

        // Set the position of this node
        ImNodes.editorResetPanning(node_pos_x, node_pos_y);

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
        // Reset the camera.
        ImNodes.editorResetPanning(0, 0);
    }

    public void render(){
        int index = 0;
        String[] output_names = this.outputs.keySet().toArray(new String[0]);
        for(String input_name : this.inputs.keySet()){
            renderInputAttribute(input_name);
            if(index < this.outputs.size()){
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

    public String getTitle() {
        return this.title;
    }

    public int getID() {
        return id;
    }

    public int getReferenceID() {
        return reference_id;
    }

    public InflowPin getInflow() {
        return inflow;
    }

    public OutflowPin getOutflow() {
        return outflow;
    }

    //TODO custom function override.
    protected final void renderInputAttribute(String param_name) {
        if(this.inputs.containsKey(param_name)) {
            InflowPin pin = this.inputs.get(param_name);
            pin.render();
        }
    }

    protected final void renderInputAttribute(String param_name, AttributeOverride render_override) {
        if(this.inputs.containsKey(param_name)) {
            InflowPin pin = this.inputs.get(param_name);
            pin.setRenderedThisFrame(true);
            ImNodes.pushColorStyle(ImNodesColorStyle.Pin, pin.getColor());
            ImNodes.beginInputAttribute(pin.getID(), pin.getShape());
            render_override.render();
            ImNodes.endInputAttribute();
            ImNodes.popColorStyle();
        }
    }

    protected final void renderOutputAttribute(String out_name) {
        if(this.outputs.containsKey(out_name)) {
            OutflowPin outflow = this.outputs.get(out_name);
            outflow.render();
        }
    }

    protected final void renderOutputAttribute(String out_name, AttributeOverride render_override) {
        if(this.outputs.containsKey(out_name)) {
            OutflowPin outflow = this.outputs.get(out_name);
            outflow.setRenderedThisFrame(true);
            ImNodes.pushColorStyle(ImNodesColorStyle.Pin, outflow.getColor());
            ImNodes.beginOutputAttribute(outflow.getID(), outflow.getShape());
            render_override.render();
            ImNodes.endOutputAttribute();
            ImNodes.popColorStyle();
        }
    }

    private void renderFlowControls(){
        if((inputs.size() > 0 || force_render_inflow) && inflow_controls_enabled) {
            ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.WHITE);
            ImNodes.beginInputAttribute(inflow_id, getInflow().isConnected() ? ImNodesPinShape.TriangleFilled : ImNodesPinShape.Triangle);
            ImNodes.endInputAttribute();
            ImNodes.popColorStyle();
            ImGui.sameLine();
        }
        // This is a hack to set the width of the nodes.
        if(this.width > 0) {
            ImGui.image(0, this.width, 0);
        }
        if((outputs.size() > 0 || force_render_outflow) && outflow_controls_enabled) {
            ImGui.sameLine();
            ImNodes.pushColorStyle(ImNodesColorStyle.Pin, NodeColors.WHITE);
            ImNodes.beginOutputAttribute(outflow_id, getOutflow().isConnected() ? ImNodesPinShape.TriangleFilled : ImNodesPinShape.Triangle);
            ImNodes.endOutputAttribute();
            ImNodes.popColorStyle();
        }
    }

    // Links
    public void renderLinks(){
        this.inflow.renderLinks();

        for(InflowPin inflow : this.inputs.values()){
            inflow.renderLinks();
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
        if(hasPinWithName(new_name)){
            //TODO: Throw error
            System.err.println(String.format("Error: Cannot rename [%s] to [%s] because an attribute with that name already exists on this node.", initial_name, new_name));
            return;
        }

        // Rename the pin
        if(inputs.containsKey(initial_name)){
            InflowPin pin = inputs.get(initial_name);
            pin.rename(new_name);
            inputs.remove(initial_name);
            inputs.put(new_name, pin);
        }
        if(outputs.containsKey(initial_name)){
            OutflowPin pin = outputs.get(initial_name);
            pin.rename(new_name);
            outputs.remove(initial_name);
            outputs.put(new_name, pin);
        }
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

        if(!node_processes_own_flow) {
            LinkedList<OutflowPin> outflows = new LinkedList<>();

            if (outflow.isConnected()) {
                outflows.add(outflow);
            }

            for (OutflowPin outflow : this.outputs.values()) {
                if (outflow instanceof OutflowPin) {
                    if (outflow.getType().equals(PinType.FLOW)) {
                        outflows.add((OutflowPin) outflow);
                    }
                }
            }

            if (outflows.size() == 1) {
                OutflowPin outflow = outflows.getFirst();
                if (outflow.isConnected()) {
                    outflow.getConnection().getParent().generateIntermediate(evaluation_stack);
                }
            } else {
                JsonArray multi_out = new JsonArray();
                for (OutflowPin outflow : outflows) {
                    if (outflow.isConnected()) {
                        outflow.getConnection().getParent().generateIntermediate(multi_out);
                    }
                }
                if(multi_out.size() == 1){
                    if(multi_out.get(0).isJsonArray()) {
                        JsonArray multi_out_element_array = multi_out.get(0).getAsJsonArray();
                        for(int i = 0; i < multi_out_element_array.size(); i++) {
                            evaluation_stack.add(multi_out_element_array.get(i));
                        }
                    }else{
                        evaluation_stack.add(multi_out.get(0));
                    }
                }else if(multi_out.size() > 1) {
                    evaluation_stack.add(multi_out);
                }
            }
        }
    }

    // Default serialize dose nothing.
    public void serialize(JsonArray evaluation_stack){};
    public abstract JsonElement getValueOfPin(OutflowPin outflow);

//    public boolean hasPinWithID(int pin_id) {
//        return this.attribute_ids.containsValue(pin_id) || inflow_id == pin_id || outflow_id == pin_id;
//    }

    public Pin getPinFromName(String pin_name){
        if(pin_name.equals(INFLOW)){
            return inflow;
        }

        if(pin_name.equals(OUTFLOW)){
            return outflow;
        }

        if(this.inputs.containsKey(pin_name)){
            return this.inputs.get(pin_name);
        }

        if(this.outputs.containsKey(pin_name)){
            return this.outputs.get(pin_name);
        }

        return null;
    }

    public boolean hasPinWithID(int pin_id){
        return this.pin_ids.containsKey(pin_id);
    }

    public Pin getPinFromID(int id){
        return this.pin_ids.getOrDefault(id, null);
    }

    public Collection<InflowPin> getNodeInflowPins(){
        //TODO optimize later
        LinkedList<InflowPin> inflow_pins = new LinkedList<>();

        for(InflowPin pin : this.inputs.values()){
            if(pin.getType().equals(PinType.DATA)){
                inflow_pins.add(pin);
            }
        }

        return inflow_pins;
    }

    public Collection<OutflowPin> getNodeOutflowPins(){
        //TODO optimize later
        LinkedList<OutflowPin> outflow_pins = new LinkedList<>();

        for(OutflowPin pin : this.outputs.values()){
            if(pin.getType().equals(PinType.DATA)){
                outflow_pins.add(pin);
            }
        }

        return outflow_pins;
    }

    public void clearInflowPins(){
        for(InflowPin pin : new LinkedHashSet<>(this.inputs.values())){
            if(pin.getType().equals(PinType.DATA)){
                this.inputs.remove(pin);
            }
        }
    }

    public void clearOutflowPins(){
        for(OutflowPin pin : new LinkedHashSet<>(this.outputs.values())){
            if(pin.getType().equals(PinType.DATA)){
                this.outputs.remove(pin);
            }
        }
    }

//    public OutflowPin getPinSource(String inflow_pin_name){
//        if(this.inputs.containsKey(inflow_pin_name)){
//            return getPinSource(this.inputs.get(inflow_pin_name));
//        }
//        return null;
//    }
//
//    public OutflowPin getPinSource(InflowPin inflow){
//        if(inflow.isConnected()){
//            return inflow.getLink();
//        }
//        return null;
//    }

    // Rendering stuff
    public void disableFlowControls(){
        this.inflow_controls_enabled = false;
        this.outflow_controls_enabled = false;
    }

    public void disableInflowControl(){
        this.inflow_controls_enabled = false;
    }

    public void disableOutflowControls(){
        this.outflow_controls_enabled = false;
    }

    public void forceRenderInflow(){
        this.force_render_inflow = true;
    }

    public void forceRenderOutflow(){
        this.force_render_outflow = true;
    }

    public void setNodeProcessesOwnFlow(boolean process_own_flow){
        this.node_processes_own_flow = process_own_flow;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    public Node clone(){
        JsonObject initialization_data = this.generateSaveData();
        return NodeRegistry.getInstance().getNodeFromClass(initialization_data);
    }

    public final void onRemove(){
        LinkedList<Pin> pin_buffer = new LinkedList<>();
        pin_buffer.addAll(this.outputs.values());
        for (Pin pin : pin_buffer) {
            OutflowPin outflow_pin = (OutflowPin) pin;
            Collection<InflowPin> connections = new LinkedList<>(outflow_pin.getConnections());
            for(InflowPin link : connections){
                link.disconnect();
            }
        }
        pin_buffer.clear();
        pin_buffer.addAll(this.inputs.values());
        for(Pin pin : pin_buffer){
            ((InflowPin)pin).disconnect();
        }
        this.inflow.disconnect();
        this.outflow.disconnect();
    }
}