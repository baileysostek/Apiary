package nodegraph.pin;

import graphics.GLStruct;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import nodegraph.Node;

import java.util.LinkedHashSet;

public abstract class Pin {

    private final Node parent;

    private String attribute_name;
    private final int shape_connected_data = ImNodesPinShape.CircleFilled;
    private final int shape_disconnected_data = ImNodesPinShape.Circle;

    private final int shape_connected_flow = ImNodesPinShape.TriangleFilled;
    private final int shape_disconnected_flow = ImNodesPinShape.Triangle;

    private final PinType type;
    private final PinDirection direction;

    public Pin(Node parent, String attribute_name, PinType type, PinDirection direction) {
        this.parent = parent;
        this.attribute_name = attribute_name;

        this.type = type;
        this.direction = direction;
    }

    public String getAttributeName() {
        return attribute_name;
    }

    public int getShape() {
        return isConnected() ? ((type.equals(PinType.FLOW)) ? shape_connected_flow : shape_connected_data) :  ((type.equals(PinType.FLOW)) ? shape_disconnected_flow : shape_disconnected_data);
    }

    public PinType getType() {
        return type;
    }

    public int getID(){
        return this.parent.getPinIDFromName(attribute_name);
    }

    public boolean canLink(Pin other){
        // Disallow pins from linking to themselves
        if(this.equals(other)){
            return false;
        }
        // If we are already linked to the other pin do not allow duplicate links.
        if(isConnectedTo(other)){
            return false;
        }

        // In order to link the types need to align.
        if(this.type.equals(other.type)){
            // The directions need to be different
            if(!this.direction.equals(other.direction)){
                if (this.type.equals(PinType.DATA)) {
                    GLStruct type_pin_input = this.getDataType();
                    if (type_pin_input == null) {
                        return false;
                    }
                    GLStruct type_pin_output = other.getDataType();
                    if (type_pin_output == null) {
                        return false;
                    }
                    return type_pin_input.typeEquals(type_pin_output);
                } else {
                    // Flows can always connect
                    return true;
                }
            }
        }

        return false;
    }

    public GLStruct getDataType(){
        if(type.equals(PinType.DATA)){
            if(direction.equals(PinDirection.DESTINATION)){
                return this.parent.getInputType(this.attribute_name);
            }
            if(direction.equals(PinDirection.SOURCE)){
                return this.parent.getOutputType(this.attribute_name);
            }
        }
        return null;
    }

    public void rename(String new_name) {
        this.attribute_name = new_name;
    }

    public Node getParent() {
        return this.parent;
    }



    public abstract void link(Pin other);
    public abstract void disconnect();

    public abstract boolean isConnected();
    public abstract boolean isConnectedTo(Pin other);

    public abstract void renderLinks();
}
