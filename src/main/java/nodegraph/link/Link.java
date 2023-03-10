package nodegraph.link;

import editor.Editor;
import graphics.GLPrimitive;
import graphics.GLStruct;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import nodegraph.NodeColors;
import nodegraph.pin.Pin;
import nodegraph.pin.PinType;

// Links are directed
public class Link {

    Pin source;
    Pin dest;

    public Link(Pin source, Pin dest) {
        this.source = source;
        this.dest = dest;
    }

    public void renderLink(){
        GLStruct type = source.getDataType();
        int color = (type instanceof GLPrimitive) ? NodeColors.getTypeColor(((GLPrimitive)type).getPrimitiveType()) : (source.getType().equals(PinType.FLOW) ? NodeColors.WHITE : NodeColors.PINK);
        ImNodes.pushColorStyle(ImNodesColorStyle.Link, color);
        ImNodes.pushColorStyle(ImNodesColorStyle.LinkHovered, color);
        ImNodes.pushColorStyle(ImNodesColorStyle.LinkSelected, color);
        ImNodes.link(Editor.getInstance().getNextAvailableID(), source.getID(), dest.getID());
        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
    }
}
