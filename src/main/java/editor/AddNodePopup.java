package editor;

import com.google.gson.JsonObject;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import nodegraph.Node;
import nodegraph.NodeGraph;
import nodegraph.NodeRegistry;

import java.util.Locale;

public class AddNodePopup implements EditorComponent {

    // Properties to help with rendering.
    private String ADD_NODE_POPUP = "Add Node";
    private ImVec2 popup_screen_pos = new ImVec2();
    private ImString search_data = new ImString();
    private boolean popup_opened_this_frame = false;

    // Members taken from parent.
    private NodeGraph graph;

    public AddNodePopup(NodeGraph graph) {
        this.graph = graph;
    }

    public void open(){
        this.popup_opened_this_frame = true;
    }

    @Override
    public void update(double delta) {

    }

    @Override
    public void render() {
        if(popup_opened_this_frame){
            ImGui.getMousePosOnOpeningCurrentPopup(popup_screen_pos);
            ImGui.openPopup(ADD_NODE_POPUP);
            popup_screen_pos.x -= ImGui.getWindowPosX() + 100; // Translate by window pos.
            popup_screen_pos.y -= ImGui.getWindowPosY() + 100;
        }

        // if the popup is open we can render the content.
        if(ImGui.beginPopup(ADD_NODE_POPUP)){

            // Focus the text input on open.
            if(popup_opened_this_frame){
                ImGui.setKeyboardFocusHere();
                popup_opened_this_frame = false;
            }

            ImGui.inputTextWithHint("##add_node_search", "Search", search_data, ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll);
            ImGui.beginChildFrame(Editor.getInstance().getNextAvailableID(), 256, 512);
            for(Class<? extends Node> node_class : NodeRegistry.getInstance().getRegisteredNodes()){
                String node_name = node_class.getSimpleName().toLowerCase(Locale.ROOT);
                if(node_name.contains(search_data.get().toLowerCase(Locale.ROOT))) {
                    if(ImGui.button(node_class.getSimpleName())){
                        JsonObject initialization_data = new JsonObject();
                        initialization_data.addProperty("pos_x", popup_screen_pos.x);
                        initialization_data.addProperty("pos_y", popup_screen_pos.y);
                        graph.addNode(NodeRegistry.getInstance().getNodeFromClass(node_class, initialization_data));
                        ImGui.closeCurrentPopup();
                    }
                    ImGui.newLine();
                }
            }
            ImGui.endChildFrame();
            ImGui.endPopup();
        }
    }
}
