package nodes;

import editor.Editor;
import imgui.extension.imnodes.ImNodes;

// Links are directed
public class Link {

    Pin source;
    Pin dest;

    public Link(Pin source, Pin dest) {
        this.source = source;
        this.dest = dest;
    }

    public void renderLink(){
        ImNodes.link(Editor.getInstance().getNextAvailableID(), source.getID(), dest.getID());
    }
}
