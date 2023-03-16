package nodegraph.pin;

import editor.Editor;
import graphics.GLDataType;
import graphics.GLPrimitive;
import graphics.GLStruct;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesStyleVar;
import nodegraph.Node;
import nodegraph.NodeColors;

import java.util.*;

public class InflowPin extends Pin {

    private OutflowPin link;

    // An inflow pin can have any number of datatypes in it.
    private LinkedHashSet<GLDataType> accepted_types = new LinkedHashSet<GLDataType>();

    public InflowPin(Node parent, String attribute_name, PinType type, GLDataType ... accepted_types) {
        super(parent, attribute_name, type, PinDirection.DESTINATION);
        this.accepted_types.addAll(List.of(accepted_types));
    }

    @Override
    public void link(Pin other) {
        // We can only allow a connection to an outflow.
        if(!(other instanceof OutflowPin)){
            return;
        }

        if(canLink(other)){
            OutflowPin outflowPin = ((OutflowPin)other);
            // If we are linked to something now, we are becoming disconnected so reflect that change.
            if (!(link == null)) {
                link.disconnect(this);
            }
            this.link = outflowPin;
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

        // If we are already linked to the other pin do not allow duplicate links.
        if(isConnectedTo(other)){
            return false;
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

    public void renderLinks() {
        if (!(link == null)) {
            //TODO smooth fade between colors.
            int color = NodeColors.WHITE;

            if(this.isConnected()){
                color = NodeColors.getTypeColor(this.getLink().getDataType());
            }

            ImNodes.pushColorStyle(ImNodesColorStyle.Link, color);
            ImNodes.pushColorStyle(ImNodesColorStyle.LinkHovered, color);
            ImNodes.pushColorStyle(ImNodesColorStyle.LinkSelected, color);
            ImNodes.link(Editor.getInstance().getNextAvailableID(), link.getID(), this.getID());
            ImNodes.popColorStyle();
            ImNodes.popColorStyle();
            ImNodes.popColorStyle();
        }
    }

    public void setType(GLDataType type){
        this.accepted_types.clear();
        this.accepted_types.add(type);
    }

    public int getColor(){
        if(this.isConnected()){
            return NodeColors.getTypeColor(this.getLink().getDataType());
        }
        return NodeColors.getTypeColor((GLDataType) this.accepted_types.toArray()[(int) Math.floor(this.accepted_types.size() * Math.random())]);
    }
}
