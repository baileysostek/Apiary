package nodegraph.pin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import editor.Editor;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesStyle;
import imgui.extension.imnodes.flag.ImNodesCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import nodegraph.Node;
import nodegraph.NodeColors;
import simulation.Attribute;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class InflowPin extends Pin {

    @FunctionalInterface
    public interface InflowPinConnectionCallback{
        void onConnect(OutflowPin other);
    }
    private InflowPinConnectionCallback callback;

    private OutflowPin link;

    private ImString value = new ImString();

    // An inflow pin can have any number of datatypes in it.
    private LinkedHashSet<GLDataType> accepted_types = new LinkedHashSet<GLDataType>();

    public InflowPin(Node parent, String attribute_name, PinType type, GLDataType ... accepted_types) {
        super(parent, attribute_name, type, PinDirection.DESTINATION);
        this.accepted_types.addAll(Arrays.asList(accepted_types));
    }

    @Override
    public void render() {
        ImNodes.pushColorStyle(ImNodesCol.Pin, getColor());
        ImNodes.beginInputAttribute(getID(), getShape());
        if(this.isConnected()){
            // Render the name
            ImGui.text(this.getAttributeName());
        }else{
            ImGui.setNextItemWidth(this.getParent().getWidth()  / 2);
            if(ImGui.inputTextWithHint("##_"+this.getID()+"_value", "value", value, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll)){
                System.out.println("Value:"+value.get());
            }
        }
        ImNodes.endInputAttribute();
        ImNodes.popColorStyle();
    }

    @Override
    public void link(Pin other) {
        // We can only allow a connection to an outflow.
        if(!(other instanceof OutflowPin)){
            return;
        }

        if(canLink(other) && !isConnectedTo(other)){
            OutflowPin outflow_pin = ((OutflowPin)other);
            // If we are linked to something now, we are becoming disconnected so reflect that change.
            if (!(link == null)) {
                link.disconnect(this);
            }
            this.link = outflow_pin;
            if(this.callback != null){
                this.callback.onConnect(outflow_pin);
            }
        }
    }

    @Override
    public void disconnect(){
        if(!(this.link == null)) {
            this.link.disconnect(this);
        }
        this.link = null;
    }

    public OutflowPin getLink(){
        return this.link;
    }

    @Override
    public boolean canLink(Pin other) {
        if(other == null){
            return false; // Cant link to nothing.
        }

        if(!(this.getType().equals(other.getType()))){
            return false;
        }

        // Ensure that we are connecting to an outflow
        if(other instanceof OutflowPin){
            OutflowPin outflow = (OutflowPin) other;
            if(this.getType().equals(PinType.DATA)) {
                return this.accepted_types.contains(outflow.getDataType());
            } else {
                // Flows can always connect.
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isConnected() {
        return this.link != null;
    }

    @Override
    public boolean isConnectedTo(Pin other) {
        if (link == null) {
            return false;
        }
        return link.equals(other);
    }

    @Override
    public boolean hasNonDefaultValue() {
        return isConnected() || !value.get().isEmpty();
    }

    public void renderLinks() {
        if (!(link == null)) {
            // Make sure that the ids are > 0 and that the pins we are linking to rendered this frame.
//            System.out.println("link.renderedThisFrame()" + link.renderedThisFrame() + " this.renderedThisFrame()" + this.renderedThisFrame());
            if(true) {

                //TODO smooth fade between colors.
                int color = NodeColors.WHITE;

                if(this.isConnected()){
                    color = NodeColors.getTypeColor(this.getLink().getDataType());
                }

                ImNodes.pushColorStyle(ImNodesCol.Link, color);
                ImNodes.pushColorStyle(ImNodesCol.LinkHovered, color);
                ImNodes.pushColorStyle(ImNodesCol.LinkSelected, color);
                ImNodes.link(Editor.getInstance().getNextAvailableID(), link.getID(), this.getID());
                ImNodes.popColorStyle();
                ImNodes.popColorStyle();
                ImNodes.popColorStyle();
            }
        }
    }

    public void setType(GLDataType ... types){
        this.accepted_types.clear();
        this.accepted_types.addAll(Arrays.asList(types));
        refreshConnections();
    }

    // Re-evaluate any connections
    private void refreshConnections(){
        if(this.isConnected()){
            if(!this.link.canLink(this)){
                this.link.disconnect(this);
                this.disconnect();
            }
        }
    }

    public int getColor(){
        if(this.isConnected()){
            return NodeColors.getTypeColor(this.getLink().getDataType());
        }
        if(this.accepted_types.size() > 0) {
            return NodeColors.getTypeColor((GLDataType) this.accepted_types.toArray()[(int) Math.floor(this.accepted_types.size() * Math.random())]);
        }else{
            return NodeColors.WHITE;
        }
    }

    public LinkedList<GLDataType> getAcceptedTypes(){
        LinkedList<GLDataType> out = new LinkedList<>();
        out.addAll(this.accepted_types);
        return out;
    }

    @Override
    public JsonElement getValue(){
        if(isConnected()){
            return this.link.getValue();
        } else {
            return new JsonPrimitive(this.value.get());
        }
    }

    public void setValue(String value){
        this.value.set(value);
    }

    public void onConnect(InflowPinConnectionCallback callback){
        this.callback = callback;
    }
}
