package editor;

import core.Apiary;
import imgui.ImColor;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import input.Mouse;
import nodes.Node;
import nodes.NodeInstance;
import nodes.NodeManager;
import nodes.Nodes;

import java.awt.*;
import java.net.URI;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;

public class Editor {

    private static Editor instance;

    // Mouse cursors provided by GLFW
    private static final long[] MOUSE_CURSORS = new long[ImGuiMouseCursor.COUNT];

    private final ImGuiIO io;
    private final ImGuiImplGl3 imgui_impl;

    private final ImNodesContext CONTEXT = new ImNodesContext();
    private static final String URL = "https://github.com/Nelarius/imnodes/tree/857cc86";
    private static final ImBoolean SHOW = new ImBoolean(true);

    //This is used for ID information
    private int imgui_element_id = 0;

    NodeInstance test_0 = new NodeInstance(Nodes.ADD);
    NodeInstance test_1 = new NodeInstance(Nodes.ON_SCREEN);

    private Editor(){
        // When we initialize the editor we want to inject into the current window.
        ImGui.createContext();
        ImNodes.createContext();

        //Now that we have our instance
        // Initialize ImGuiIO config
        io = ImGui.getIO();

        io.setIniFilename(null); // We don't want to save .ini file
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Navigation with keyboard
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);    // Enable Multi-Viewport / Platform Windows
        io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleFonts);
        io.setBackendFlags(ImGuiBackendFlags.HasMouseCursors); // Mouse cursors to display while resizing windows etc.

        io.setBackendPlatformName("imgui_java_impl_glfw"); // For clarity reasons
        io.setBackendRendererName("imgui_java_impl_lwjgl"); // For clarity reason

        //Link our context to this GL window.
        imgui_impl = new ImGuiImplGl3();
        imgui_impl.init();

        // Keyboard mapping. ImGui will use those indices to peek into the io.KeysDown[] array.
        final int[] keyMap = new int[ImGuiKey.COUNT];
        keyMap[ImGuiKey.Tab] = GLFW_KEY_TAB;
        keyMap[ImGuiKey.LeftArrow] = GLFW_KEY_LEFT;
        keyMap[ImGuiKey.RightArrow] = GLFW_KEY_RIGHT;
        keyMap[ImGuiKey.UpArrow] = GLFW_KEY_UP;
        keyMap[ImGuiKey.DownArrow] = GLFW_KEY_DOWN;
        keyMap[ImGuiKey.PageUp] = GLFW_KEY_PAGE_UP;
        keyMap[ImGuiKey.PageDown] = GLFW_KEY_PAGE_DOWN;
        keyMap[ImGuiKey.Home] = GLFW_KEY_HOME;
        keyMap[ImGuiKey.End] = GLFW_KEY_END;
        keyMap[ImGuiKey.Insert] = GLFW_KEY_INSERT;
        keyMap[ImGuiKey.Delete] = GLFW_KEY_DELETE;
        keyMap[ImGuiKey.Backspace] = GLFW_KEY_BACKSPACE;
        keyMap[ImGuiKey.Space] = GLFW_KEY_SPACE;
        keyMap[ImGuiKey.Enter] = GLFW_KEY_ENTER;
        keyMap[ImGuiKey.Escape] = GLFW_KEY_ESCAPE;
        keyMap[ImGuiKey.KeyPadEnter] = GLFW_KEY_KP_ENTER;
        keyMap[ImGuiKey.A] = GLFW_KEY_A;
        keyMap[ImGuiKey.C] = GLFW_KEY_C;
        keyMap[ImGuiKey.V] = GLFW_KEY_V;
        keyMap[ImGuiKey.X] = GLFW_KEY_X;
        keyMap[ImGuiKey.Y] = GLFW_KEY_Y;
        keyMap[ImGuiKey.Z] = GLFW_KEY_Z;

        io.setKeyMap(keyMap);

        // Mouse cursors mapping
        MOUSE_CURSORS[ImGuiMouseCursor.Arrow]       = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        MOUSE_CURSORS[ImGuiMouseCursor.TextInput]   = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR);
        MOUSE_CURSORS[ImGuiMouseCursor.ResizeAll]   = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        MOUSE_CURSORS[ImGuiMouseCursor.ResizeNS]    = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        MOUSE_CURSORS[ImGuiMouseCursor.ResizeEW]    = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
        MOUSE_CURSORS[ImGuiMouseCursor.ResizeNESW]  = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        MOUSE_CURSORS[ImGuiMouseCursor.ResizeNWSE]  = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        MOUSE_CURSORS[ImGuiMouseCursor.Hand]        = glfwCreateStandardCursor(GLFW_HAND_CURSOR);
        MOUSE_CURSORS[ImGuiMouseCursor.NotAllowed]  = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);


        Mouse.getInstance().addMouseEventCallback((button, action, mods) -> {
            final boolean[] mouseDown = new boolean[5];

            mouseDown[0] = button == GLFW_MOUSE_BUTTON_1 && action != GLFW_RELEASE;
            mouseDown[1] = button == GLFW_MOUSE_BUTTON_2 && action != GLFW_RELEASE;
            mouseDown[2] = button == GLFW_MOUSE_BUTTON_3 && action != GLFW_RELEASE;
            mouseDown[3] = button == GLFW_MOUSE_BUTTON_4 && action != GLFW_RELEASE;
            mouseDown[4] = button == GLFW_MOUSE_BUTTON_5 && action != GLFW_RELEASE;

            io.setMouseDown(mouseDown);

            if (!io.getWantCaptureMouse() && mouseDown[1]) {
                ImGui.setWindowFocus(null);
            }
        });

    }

    public static void initialize(){
        if(instance == null){
            instance = new Editor();
        }
    }

    public static Editor getInstance(){
        return instance;
    }

    public void update(double delta){
        final ImGuiIO io = ImGui.getIO();
        io.setDisplaySize(Apiary.getWindowWidth(), Apiary.getWindowHeight());
        io.setDisplayFramebufferScale((float) 1, (float) 1);

        //Mouse input
        io.setMousePos((float) Mouse.getInstance().getMousePosition().x(), (float) Mouse.getInstance().getMousePosition().y());
        io.setDeltaTime((float) delta);

        // Update mouse cursor
        glfwSetCursor(Apiary.getWindowPointer(), MOUSE_CURSORS[ImGui.getMouseCursor()]);
        glfwSetInputMode(Apiary.getWindowPointer(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public void render(){
        ImGui.newFrame();

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

            test_0.renderNode();
            test_1.renderNode();

            if(ImGui.button("to IR")){
                test_0.toIR();
                test_1.toIR();
            }

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

        ImGui.render();
        imgui_impl.renderDrawData(ImGui.getDrawData());

        ImGui.updatePlatformWindows();
        ImGui.renderPlatformWindowsDefault();
        imgui_element_id = 0;
    }

    public void onShutdown() {
        imgui_impl.dispose();
        ImGui.destroyContext();
    }

    public int nextID(){
        return ++imgui_element_id;
    }
}
