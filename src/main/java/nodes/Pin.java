package nodes;

import imgui.extension.imnodes.flag.ImNodesPinShape;

public class Pin {

    private final Node parent;

    private final String attribute_name;
    private final int shape = ImNodesPinShape.Circle;

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
        return shape;
    }

    public PinType getType() {
        return type;
    }

    public int getID(){
        return this.parent.getPinIDFromName(attribute_name);
    }

    public void link(Pin other){
        // We can make this link!
        if(canLink(other)){
            this.parent.link(this, other);
        }
//        if(this.output_values.containsKey(element_name) && destination.input_values.containsKey(destination_element_name)){
//            this.links.add(new Link(this, element_name, destination, destination_element_name));
//        }
//        return -1;
    }

    private boolean canLink(Pin other){
        return true;
    }
}
