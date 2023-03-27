package editor;

import com.google.gson.JsonObject;
import core.Apiary;
import graphics.texture.FilterOption;
import graphics.texture.TextureManager;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiViewport;
import imgui.ImVec2;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import input.Keyboard;
import input.Mouse;
import nodegraph.Node;
import nodegraph.NodeGraph;
import nodegraph.NodeRegistry;
import nodegraph.nodes.math.AddNode;
import nodegraph.pin.Pin;
import org.lwjgl.opengl.GL43;
import simulation.SimulationManager;
import util.StringUtils;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Editor {

    private static Editor instance;

    // We need to buffer our keypresses
    private LinkedList<Integer> key_presses = new LinkedList<>();

    // Mouse cursors provided by GLFW
    private static final long[] MOUSE_CURSORS = new long[ImGuiMouseCursor.COUNT];

    private final ImGuiIO io;
    private final ImGuiImplGl3 imgui_impl;

    private final ImNodesContext CONTEXT = new ImNodesContext();

    private static final ImBoolean SHOW = new ImBoolean(true);

    //This is used for ID information
    private int imgui_element_id = 0;

    NodeGraph graph = new NodeGraph();

//    Node test_0 = new TemplateNode(FunctionDirectives.ON_SCREEN);
//    Node test_1 = new TemplateNode(FunctionDirectives.CONDITIONAL);

    Pin start_pin = null;

    float node_editor_width  = 1;
    float node_editor_height = 1;

    boolean initialize = true;

    final int APIARY_TEXTURE_ID;

    private AddNodePopup add_node_popup;

//    int[] selected_nodes  = new int[128];
    private LinkedHashSet<Node> selected_nodes = new LinkedHashSet<>();
    private LinkedHashSet<Node> to_select = new LinkedHashSet<>();
    JsonObject serialized_clipboard_data = null;

    private Editor(){
        // Initialize the other singletons that we need
        UniformManager.initialize();

        // When we initialize the editor we want to inject into the current window.
        ImGui.createContext();
        ImNodes.createContext();

        // Load our textures
        APIARY_TEXTURE_ID = TextureManager.getInstance().load(StringUtils.getPathToResources() + "/textures/" + "apiary.png", FilterOption.NEAREST);

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

        glfwSetCharCallback(Apiary.getWindowPointer(), (w, c) -> {
            if (!((c == GLFW_KEY_DELETE))){
                io.addInputCharacter(c);
            }
        });

        Mouse.getInstance().addScrollEvent(((scroll_x, scroll_y) -> {
            io.setMouseWheelH(io.getMouseWheelH() + scroll_x);
            io.setMouseWheel(io.getMouseWheel() + scroll_y);
        }));

        io.setSetClipboardTextFn(new ImStrConsumer() {
            @Override
            public void accept(final String s) {
                glfwSetClipboardString(Apiary.getWindowPointer(), s);
            }
        });

        io.setGetClipboardTextFn(new ImStrSupplier() {
            @Override
            public String get() {
                return glfwGetClipboardString(Apiary.getWindowPointer());
            }
        });

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

//                    if(ImNodes.getHoveredNode() >= 0){
//                        ImNodes.editorMoveToNode(ImNodes.getHoveredNode());
//                        ImVec2 pan = new ImVec2();
//                        ImNodes.editorContextGetPanning(pan);
//                        ImNodes.editorResetPanning(pan.x + node_editor_width / 2f, pan.y + node_editor_height / 2f);
//                        System.out.println(pan);
//                    }

                    Pin clicked_pin = graph.getPinFromID(ImNodes.getHoveredPin());

                    if (!(clicked_pin == null)) {
                        if (Keyboard.getInstance().isKeyPressed(GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL)) {
                            if (clicked_pin.isConnected()) {
                                clicked_pin.disconnect();
                            }

                            start_pin = null;
                            return;
                        }
                    }

                    this.start_pin = clicked_pin;
                    System.out.println(start_pin);
                }
                if(action == GLFW_RELEASE) {
                    System.out.println("Pin:" + ImNodes.getHoveredPin());
                    System.out.println("Link:" + ImNodes.getHoveredLink());
                    System.out.println("Node:" + ImNodes.getHoveredNode());
                    if(start_pin != null){
                        // Check if we are dragging onto a pin.
                        Pin dest_pin = graph.getPinFromID(ImNodes.getHoveredPin());
                        if(dest_pin != null) {
                            // Bidirectional link.
                            this.start_pin.link(dest_pin);
                            dest_pin.link(this.start_pin);
                        }

                    }
                }
            }

            if(button == GLFW_MOUSE_BUTTON_RIGHT){
                onRightClick();
            }
        });


        // Delete
        Keyboard.getInstance().addPressCallback(GLFW_KEY_DELETE, () -> {
            Editor.instance.graph.removeNodes(this.selected_nodes);
            this.deselect();
        });

        // Copy
        Keyboard.getInstance().addPressCallback(GLFW_KEY_C, () -> {
            if(Keyboard.getInstance().isKeyPressed(GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL)){
                int[] clipboard_node_ids = new int[ImNodes.numSelectedNodes()];
                Arrays.fill(clipboard_node_ids, -1);
                ImNodes.getSelectedNodes(clipboard_node_ids);
                serialized_clipboard_data = this.graph.serializeNodes(this.graph.getNodesFromIDs(clipboard_node_ids));
                //TODO trigger event / snackbar.
                System.out.println(serialized_clipboard_data);
            }
        });

        // Paste
        Keyboard.getInstance().addPressCallback(GLFW_KEY_V, () -> {
            if(Keyboard.getInstance().isKeyPressed(GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL)){
                this.pasteClipboard();
            }
        });

        Keyboard.getInstance().addPressCallback(GLFW_KEY_SPACE, () -> {
            try {

            }catch(Exception e) {

            }
        });

        add_node_popup = new AddNodePopup(graph);

    }

    public static void initialize(){
        if(instance == null){
            instance = new Editor();
            instance.graph.load("simulations/gol_test.jsonc");



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
            add_node_popup.open();
        }else{

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

        // TODO collection
        add_node_popup.update(delta);
    }

    public void render(){
        ImGui.newFrame();

        for(int key : key_presses){
//            io.addInputCharactersUTF8(String.valueOf((char)key));
        }
        key_presses.clear();

        GL43.glClearColor(0f, 0f, 0f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);

        renderEditorWindow();

        // Actually render the frame
        ImGui.render();
        imgui_impl.renderDrawData(ImGui.getDrawData());

        ImGui.updatePlatformWindows();
        ImGui.renderPlatformWindowsDefault();
        imgui_element_id = 0;

        initialize = false;

        // After a frame release the input keys.
        io.clearInputKeys();
    }

    private void renderEditorWindow(){
        ImGuiViewport viewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(viewport.getWorkPosX(), viewport.getWorkPosY());
        ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY());
        ImGui.setNextWindowViewport(viewport.getParentViewportId());

//        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        if (ImGui.begin("Apiary", SHOW, ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoResize)) {

            ImGui.columns(2, "Editor");
            ImGui.setColumnWidth(0, Math.max(256, ImGui.getColumnWidth()));
            if(initialize){
                ImGui.setColumnWidth(0, 256);
            }
            ImGui.beginChild("Simulation Editor", -1, -1, false, ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoCollapse| ImGuiWindowFlags.AlwaysAutoResize);
            renderSimulationEditor();
            ImGui.endChild();
            ImGui.nextColumn();
            ImGui.beginChild("Node Editor", -1, -1, false, ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoCollapse);
            node_editor_width = ImGui.getWindowWidth();
            node_editor_height = ImGui.getWindowHeight();
            renderNodeEditor();
            ImGui.image(SimulationManager.getInstance().getSimulationTexture(), ImGui.getWindowWidth(), ImGui.getWindowHeight());
            ImGui.endChild();

            ImGui.end();
        }
    }

    private void renderSimulationEditor(){
//        renderNodeEditor();
        // First we have the Apiary logo and some file options.
        ImGui.image(APIARY_TEXTURE_ID, 32, 32);
//        if(ImGui.button("Open File")){
//            Promise promise = new Promise(() -> {
//                String resource = FileManager.getInstance().openFilePicker("", new String[]{"png", "jpg"});
//                this.test = TextureManager.getInstance().load(resource);
//            });
//        }
        ImGui.separator();
        // Render All of the different Agents which we have in simulation.

        ImGui.separator();
        // Render our uniforms
        UniformManager.getInstance().render(null);
    }

    private void renderNodeEditor(){
        ImNodes.editorContextSet(CONTEXT);
        ImNodes.beginNodeEditor();

        graph.render();

        final boolean isEditorHovered = ImNodes.isEditorHovered();

        ImNodes.miniMap(.1f, ImNodesMiniMapLocation.BottomRight);

        ImNodes.endNodeEditor();

        int[] selected_nodes_ids = new int[ImNodes.numSelectedNodes()];
        HashSet<Integer> selected_nodes_hashed = new HashSet<>(selected_nodes_ids.length);
        ImNodes.getSelectedNodes(selected_nodes_ids);
        ImNodes.clearNodeSelection();
        this.selected_nodes.clear();
        for(int node_id : selected_nodes_ids) {
            if (node_id >= 0){
                this.select(this.graph.getNodeFromID(node_id));
            }
        }
        for(Node node : to_select) {
            if (node.getID() >= 0){
                this.select(node);
            }
        }
        to_select.clear();
        // After our graph renders we know that our nodes are in the node editor so this code can be executed without crashing.
        for (Node selected_node : selected_nodes) {
            try {
                if (!selected_nodes_hashed.contains(selected_node.getID())) {
                    ImNodes.selectNode(selected_node.getID());
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        add_node_popup.render();
    }

    public void load(String file_path){

    }

    public void save(String file_path){

    }

    public void onShutdown() {
        imgui_impl.dispose();
        ImGui.destroyContext();
    }

    public int getNextAvailableID(){
        return ++imgui_element_id;
    }

    public void processKeyEvent(int key, int scancode, int action, int mods) {
        io.setKeysDown(key, action == GLFW_PRESS);

        io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
        io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
        io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
        io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));
    }

    public NodeGraph getNodeGraph() {
        return this.graph;
    }

    public void deselect(){
        this.selected_nodes.clear();
        ImNodes.clearNodeSelection();
    }

    public void deselect(Node node){
        if(this.selected_nodes.contains(node)){
            this.selected_nodes.remove(node);
            ImNodes.clearNodeSelection(node.getID());
        }
    }

    public void select(Node node){
        this.selected_nodes.add(node);
    }

    public void pasteClipboard(){
        if(serialized_clipboard_data != null) {
            this.deselect();
            Collection<Node> new_nodes = Editor.instance.graph.load(serialized_clipboard_data, true);
            to_select.addAll(new_nodes);
        }
    }
}
