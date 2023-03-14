package nodegraph.pin;

import editor.Editor;
import graphics.GLPrimitive;
import graphics.GLStruct;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesStyleVar;
import nodegraph.Node;
import nodegraph.NodeColors;

import java.util.Collection;

public class InflowPin extends Pin {

    private OutflowPin link;

    public InflowPin(Node parent, String attribute_name, PinType type) {
        super(parent, attribute_name, type, PinDirection.DESTINATION);
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
            GLStruct type = this.getDataType();
            int color = (type instanceof GLPrimitive) ? NodeColors.getTypeColor(((GLPrimitive)type).getPrimitiveType()) : (this.getType().equals(PinType.FLOW) ? NodeColors.WHITE : NodeColors.PINK);
            ImNodes.pushColorStyle(ImNodesColorStyle.Link, color);
            ImNodes.pushColorStyle(ImNodesColorStyle.LinkHovered, color);
            ImNodes.pushColorStyle(ImNodesColorStyle.LinkSelected, color);
            ImNodes.link(Editor.getInstance().getNextAvailableID(), link.getID(), this.getID());
            ImNodes.popColorStyle();
            ImNodes.popColorStyle();
            ImNodes.popColorStyle();
        }
    }
}
