package nodes;

import editor.Editor;
import imgui.extension.imnodes.ImNodes;

// Links are directed
public class Link {
    Node source;
    String source_element_name;
    Node dest;
    String dest_element_name;

    public Link(Node source, String source_element_name, Node dest, String dest_element_name) {
        this.source = source;
        this.source_element_name = source_element_name;
        this.dest = dest;
        this.dest_element_name = dest_element_name;
    }

    public void renderLink(){
        int source_id = source.getAttributeByName(source_element_name);
        int dest_id = dest.getAttributeByName(dest_element_name);
        if(source_id >= 0 && dest_id >= 0) {
            ImNodes.link(Editor.getInstance().getNextAvailableID(), source_id, dest_id);
        }
    }
}
