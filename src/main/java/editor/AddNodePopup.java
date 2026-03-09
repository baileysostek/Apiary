package editor;

import com.google.gson.JsonObject;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import nodegraph.Node;
import nodegraph.NodeGraph;
import nodegraph.NodeRegistry;

import java.util.Locale;

public class AddNodePopup implements EditorComponent {

    // Properties to help with rendering.
    private String ADD_NODE_POPUP = "Add Node";
    private ImVec2 mouse_pos = new ImVec2();
    private ImString search_data = new ImString();
    private boolean popup_opened_this_frame = false;

    private float parent_window_pos_x = 0;
    private float parent_window_pos_y = 0;

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
            parent_window_pos_x = ImGui.getWindowPosX();
            parent_window_pos_y = ImGui.getWindowPosY();

            ImGui.getMousePosOnOpeningCurrentPopup(mouse_pos);

            ImGui.openPopup(ADD_NODE_POPUP);
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

                        // Calculate the position nodes should be created at.
                        ImVec2 camera_pos = new ImVec2();
                        ImNodes.editorContextGetPanning(camera_pos);

                        float node_pos_x = (mouse_pos.x - parent_window_pos_x) - camera_pos.x; // Translate by window pos.
                        float node_pos_y = (mouse_pos.y - parent_window_pos_y) - camera_pos.y;

                        initialization_data.addProperty("pos_x", node_pos_x);
                        initialization_data.addProperty("pos_y", node_pos_y);
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
