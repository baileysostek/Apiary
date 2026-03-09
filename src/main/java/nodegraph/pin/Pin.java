package nodegraph.pin;

import com.google.gson.JsonElement;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import nodegraph.Node;

public abstract class Pin {

    private final Node parent;

    private int id;

    private String attribute_name;
    private final int shape_connected_data = ImNodesPinShape.CircleFilled;
    private final int shape_disconnected_data = ImNodesPinShape.Circle;

    private final int shape_connected_flow = ImNodesPinShape.TriangleFilled;
    private final int shape_disconnected_flow = ImNodesPinShape.Triangle;

    private final PinType type;
    private final PinDirection direction;

    protected boolean rendered_this_frame = false;

    public Pin(Node parent, String attribute_name, PinType type, PinDirection direction) {
        this.parent = parent;
        this.attribute_name = attribute_name;

        this.type = type;
        this.direction = direction;

        this.id = 0;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getID(){
        return this.id;
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

    public abstract boolean canLink(Pin other);

    public void rename(String new_name) {
        this.attribute_name = new_name;
    }

    public Node getParent() {
        return this.parent;
    }


    public abstract void render();
    public abstract void link(Pin other);
    public abstract void disconnect();

    public abstract boolean isConnected();
    public abstract boolean isConnectedTo(Pin other);
    public abstract boolean hasNonDefaultValue();

    public abstract JsonElement getValue();

    public abstract int getColor();

    public boolean renderedThisFrame() {
        return rendered_this_frame;
    }

    public void setRenderedThisFrame(boolean rendered_this_frame) {
        this.rendered_this_frame = rendered_this_frame;
    }
}
