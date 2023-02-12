import core.Apiary;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec4;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.imnodes.ImNodesStyle;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.flag.ImGuiBackendFlags;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import nodes.NodeManager;
import nodes.vector.NodeMix;

import java.awt.*;
import java.net.URI;


public class Main extends Application {

    private static final ImNodesContext CONTEXT = new ImNodesContext();
    private static final String URL = "https://github.com/Nelarius/imnodes/tree/857cc86";

    private static final ImInt LINK_A = new ImInt();
    private static final ImInt LINK_B = new ImInt();
    private static final ImBoolean SHOW = new ImBoolean(true);

    static {
        ImNodes.createContext();
    }
    @Override
    protected void configure(Configuration config) {
        config.setTitle("Dear ImGui is Awesome!");
        config.setFullScreen(true);
    }

    @Override
    public void process() {
        ImGui.setNextWindowSize(500, 400, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("ImNodes Demo", SHOW)) {
            ImGui.text("This a demo graph editor for ImNodes");

            ImGui.alignTextToFramePadding();
            ImGui.text("Repo:");
            ImGui.sameLine();
            if (ImGui.button(URL)) {
                try {
                    Desktop.getDesktop().browse(new URI(URL));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ImNodes.editorContextSet(CONTEXT);
            ImNodes.beginNodeEditor();


            String key = "@map";
            int inputs = 2;
            int outputs = 1;

            // set the titlebar color of an individual node
            ImNodes.pushColorStyle(ImNodesColorStyle.TitleBar, ImColor.rgba(11, 109, 191, 255));
            ImNodes.pushColorStyle(ImNodesColorStyle.TitleBar, ImColor.rgba(81, 148, 204, 255));

            ImNodes.beginNode(1);
            // Title Bar
            ImNodes.beginNodeTitleBar();
            ImGui.text(key);
            ImNodes.endNodeTitleBar();

            // Input
            ImNodes.beginInputAttribute(1, ImNodesPinShape.Quad);
            ImGui.text("Label?");
            ImNodes.endInputAttribute();
            ImGui.sameLine();
            // Output
            ImNodes.beginOutputAttribute(3, ImNodesPinShape.Quad);
            ImGui.text("Label?");
            ImNodes.endOutputAttribute();

            ImNodes.beginInputAttribute(2, ImNodesPinShape.Quad);
            ImGui.text("Label?");
            ImNodes.endInputAttribute();





            ImNodes.endNode();

            ImNodes.popColorStyle();
            ImNodes.popColorStyle();

//            NodeManager.getInstance().getNode("@mix").renderNode();

//            for (Graph.GraphNode node : graph.nodes.values()) {
//                ImNodes.beginNode(node.nodeId);
//
//                ImNodes.beginNodeTitleBar();
//                ImGui.text(node.getName());
//                ImNodes.endNodeTitleBar();
//
//                ImNodes.beginInputAttribute(node.getInputPinId(), ImNodesPinShape.CircleFilled);
//                ImGui.text("In");
//                ImNodes.endInputAttribute();
//
//                ImGui.sameLine();
//
//                ImNodes.beginOutputAttribute(node.getOutputPinId());
//                ImGui.text("Out");
//                ImNodes.endOutputAttribute();
//
//                ImNodes.endNode();
//            }
//
//            int uniqueLinkId = 1;
//            for (Graph.GraphNode node : graph.nodes.values()) {
//                if (graph.nodes.containsKey(node.outputNodeId)) {
//                    ImNodes.link(uniqueLinkId++, node.getOutputPinId(), graph.nodes.get(node.outputNodeId).getInputPinId());
//                }
//            }
//
            final boolean isEditorHovered = ImNodes.isEditorHovered();
//
            ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.BottomRight);
            ImNodes.endNodeEditor();
//
//            if (ImNodes.isLinkCreated(LINK_A, LINK_B)) {
//                final Graph.GraphNode source = graph.findByOutput(LINK_A.get());
//                final Graph.GraphNode target = graph.findByInput(LINK_B.get());
//                if (source != null && target != null && source.outputNodeId != target.nodeId) {
//                    source.outputNodeId = target.nodeId;
//                }
//            }
////
//            if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
//                final int hoveredNode = ImNodes.getHoveredNode();
//                if (hoveredNode != -1) {
//                    ImGui.openPopup("node_context");
//                    ImGui.getStateStorage().setInt(ImGui.getID("delete_node_id"), hoveredNode);
//                } else if (isEditorHovered) {
//                    ImGui.openPopup("node_editor_context");
//                }
//            }
//
//            if (ImGui.isPopupOpen("node_context")) {
//                final int targetNode = ImGui.getStateStorage().getInt(ImGui.getID("delete_node_id"));
//                if (ImGui.beginPopup("node_context")) {
//                    if (ImGui.button("Delete " + graph.nodes.get(targetNode).getName())) {
//                        graph.nodes.remove(targetNode);
//                        ImGui.closeCurrentPopup();
//                    }
//                    ImGui.endPopup();
//                }
//            }
//
//            if (ImGui.beginPopup("node_editor_context")) {
//                if (ImGui.button("Create New Node")) {
//                    final Graph.GraphNode node = graph.createGraphNode();
//                    ImNodes.setNodeScreenSpacePos(node.nodeId, ImGui.getMousePosX(), ImGui.getMousePosY());
//                    ImGui.closeCurrentPopup();
//                }
//                ImGui.endPopup();
//            }
        }
        ImGui.end();
    }

    public static void main(String[] args) {
        launch(new Main());
    }
}