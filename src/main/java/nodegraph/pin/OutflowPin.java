package nodegraph.pin;

import com.google.gson.JsonElement;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import nodegraph.Node;
import nodegraph.NodeColors;

import java.util.Collection;
import java.util.LinkedHashSet;

public class OutflowPin extends Pin{

    private LinkedHashSet<InflowPin> links = new LinkedHashSet<>();

    private GLDataType type;

    public OutflowPin(Node parent, String attribute_name, PinType type, GLDataType data_type) {
        super(parent, attribute_name, type, PinDirection.SOURCE);
        this.type = data_type;
    }

    public GLDataType getDataType(){
        return this.type;
    }

    @Override
    public void link(Pin other){
        // We can only allow a connection to an InflowPin.
        if(!(other instanceof InflowPin)){
            return;
        }

        if(canLink(other)){
            // Check if this is a flow
            if(this.getType().equals(PinType.FLOW)){
                if(isConnected()){
                    // We cant be connected to more than one thing as a flow
                    this.links.clear();
                }
            }

            // Add this new connection
            this.links.add((InflowPin) other);
        }
    }

    @Override
    public void disconnect(){
        for(Pin connection : new LinkedHashSet<>(this.links)){
            connection.disconnect();
        }
        this.links.clear();
    }

    public void disconnect(InflowPin pin){
        if(this.links.contains(pin)){
            this.links.remove(pin);
        }
    }

    public Collection<InflowPin> getConnections(){
        return this.links;
    }

    public InflowPin getConnection(){
        return this.links.stream().findFirst().get();
    }

    public void setType(GLDataType type) {
        this.type = type;
    }

    @Override
    public boolean canLink(Pin other) {
        if(other == null){
            return false; // Cant link to nothing.
        }

        // If we are already linked to the other pin do not allow duplicate links.
        if(isConnectedTo(other)){
            return false;
        }

        if(!(this.getType().equals(other.getType()))){
            return false;
        }

        // Ensure that we are connecting to an outflow
        if(other instanceof InflowPin){
            InflowPin inflow = (InflowPin) other;
            return inflow.canLink(this);
        }

        return false;
    }

    @Override
    public void render() {
        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, getColor());
        ImNodes.beginOutputAttribute(getID(), getShape());
        ImGui.text(getAttributeName());
        ImNodes.endOutputAttribute();
        ImNodes.popColorStyle();
    }

    @Override
    public boolean isConnected() {
        return this.links.size() > 0;
    }

    @Override
    public boolean isConnectedTo(Pin other) {
        return links.contains(other);
    }

    @Override
    public boolean hasNonDefaultValue() {
        return isConnected();
    }

    @Override
    public JsonElement getValue(){
        return this.getParent().getValueOfPin(this);
    }

    public int getColor(){
        return NodeColors.getTypeColor(this.type);
    }
}
