package editor;

import core.Apiary;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiTextFilter;
import imgui.ImGuiViewport;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import input.Mouse;
import nodes.Node;
import nodes.NodeAttributePair;
import nodes.NodeGraph;
import nodes.Nodes;
import org.lwjgl.opengl.GL43;
import simulation.SimulationManager;

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

    NodeGraph graph = new NodeGraph();

    Node test_0 = new Node(Nodes.ON_SCREEN);
    Node test_1 = new Node(Nodes.CONDITIONAL);

    NodeAttributePair link = null;

    // IDS of popups and stuff
    String ADD_NODE_POPUP = "Add Node";
    boolean should_open_popup = false;

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

            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if(action == GLFW_PRESS) {
                    System.out.println("Pin:" + ImNodes.getHoveredPin());
                    System.out.println("Link:" + ImNodes.getHoveredLink());
                    System.out.println("Node:" + ImNodes.getHoveredNode());
                    this.link = graph.getNodeAndPinFromID(ImNodes.getHoveredPin());
                }
                if(action == GLFW_RELEASE) {
                    System.out.println("Pin:" + ImNodes.getHoveredPin());
                    System.out.println("Link:" + ImNodes.getHoveredLink());
                    System.out.println("Node:" + ImNodes.getHoveredNode());
                    if(link != null){
                        NodeAttributePair destination_node_pin = graph.getNodeAndPinFromID(ImNodes.getHoveredPin());
                        if(destination_node_pin != null) {
                            this.link.node.link(this.link.attribute, destination_node_pin.node, destination_node_pin.attribute);
                        }
                    }
                    link = null;
                }
            }

            if(button == GLFW_MOUSE_BUTTON_RIGHT){
                onRightClick();
            }
        });

        graph.addNode(this.test_0);
        graph.addNode(this.test_1);

        this.test_0.link("out", this.test_1, "Predicate");

    }

    public static void initialize(){
        if(instance == null){
            instance = new Editor();
        }
    }

    public static Editor getInstance(){
        return instance;
    }

    // Mouse methids
    private void onRightClick(){
        // Check if anything is hovered
        int pin = ImNodes.getHoveredPin();
        int link = ImNodes.getHoveredLink();
        int node = ImNodes.getHoveredNode();

        if(pin < 0 && link < 0 && node < 0){
            should_open_popup = true;
        }else{
            should_open_popup = false;
        }

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

    private void renderAddNodePopup(){
        if(ImGui.beginPopup(ADD_NODE_POPUP)){
            ImString seachData = new ImString();
            ImGui.inputText("Search", seachData);
            ImGui.beginChildFrame(getNextAvailableID(), 256, 512);
            for(Nodes node : Nodes.values()){
                if(node.name().contains(seachData.get())) {
                    ImGui.text(node.name());
                    ImGui.newLine();
                }
            }
            ImGui.endChildFrame();
            ImGui.endPopup();
        }
    }

    public void render(){
        ImGui.newFrame();

        GL43.glClearColor(0f, 0f, 0f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);


        ImGuiViewport viewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(viewport.getWorkPosX(), viewport.getWorkPosY());
        ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY());
        ImGui.setNextWindowViewport(viewport.getParentViewportId());

//        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        if (ImGui.begin("Apiary Node Editor", SHOW, ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoBringToFrontOnFocus)) {

            ImNodes.editorContextSet(CONTEXT);
            ImNodes.beginNodeEditor();

            graph.render();

            final boolean isEditorHovered = ImNodes.isEditorHovered();

            ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.BottomRight);
            ImNodes.endNodeEditor();

        }

        renderAddNodePopup();

        if(should_open_popup){
            ImGui.openPopup(ADD_NODE_POPUP);
        }

        ImGui.end();

        if (ImGui.begin("Test", SHOW)) {
            ImGui.image(SimulationManager.getInstance().getSimulationTexture(), 512, 512);
        }
        ImGui.end();

        ImGui.showDemoWindow();

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

    public int getNextAvailableID(){
        return ++imgui_element_id;
    }
}
