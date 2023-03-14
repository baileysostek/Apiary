package editor;

import compiler.FunctionDirective;
import core.Apiary;
import graphics.GLDataType;
import graphics.texture.FilterOption;
import graphics.texture.TextureManager;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiViewport;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import input.Keyboard;
import input.Mouse;
import nodegraph.*;
import nodegraph.nodes.agent.AgentNode;
import nodegraph.nodes.controlflow.InitializationNode;
import nodegraph.nodes.controlflow.StepNode;
import nodegraph.nodes.data.Vec3Node;
import nodegraph.nodes.math.IncrementNode;
import nodegraph.nodes.random.RandomBoolNode;
import nodegraph.nodes.random.RandomFloatNode;
import nodegraph.nodes.variables.DefineNode;
import nodegraph.nodes.variables.common.XPosBuiltInNode;
import nodegraph.nodes.variables.common.YPosBuiltInNode;
import nodegraph.pin.Pin;
import org.lwjgl.opengl.GL43;
import simulation.SimulationManager;
import util.StringUtils;

import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;

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
    Pin end_pin = null;

    // IDS of popups and stuff
    String ADD_NODE_POPUP = "Add Node";
    boolean should_open_popup = false;

    boolean initialize = true;

    final int APIARY_TEXTURE_ID;

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
                            //TODO logic
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

//        graph.addNode(this.test_0);
//        graph.addNode(this.test_1);

//        graph.addNode(new DefineNode());


//        this.test_0.link("out", this.test_1, "Predicate");

    }

    public static void initialize(){
        if(instance == null){
            instance = new Editor();

            Node init = new InitializationNode();

            AgentNode cell = new AgentNode("cell");
            cell.addInputPin("color", GLDataType.VEC3);
            cell.addInputPin("alive", GLDataType.BOOL);
            instance.graph.addNode(cell);

//            init.getOutflow().link(cell.getInflow());

            instance.graph.addNode(init);

            Node vec3 = new Vec3Node();
            instance.graph.addNode(vec3);

            Node r = new RandomFloatNode();
//            r.getPinFromName("random_float").link(vec3.getPinFromName("x"));
            Node g = new RandomFloatNode();
//            g.getPinFromName("random_float").link(vec3.getPinFromName("y"));
            Node b = new RandomFloatNode();
//            b.getPinFromName("random_float").link(vec3.getPinFromName("z"));
            instance.graph.addNode(r);
            instance.graph.addNode(g);
            instance.graph.addNode(b);

//            vec3.getPinFromName("vec3").link(cell.getPinFromName("color"));

            Node alive = new RandomBoolNode();
            instance.graph.addNode(alive);

            // Now we will add the steps
            StepNode gol_step = new StepNode();
            instance.graph.addNode(gol_step);

            DefineNode define_neighbors = new DefineNode();
            instance.graph.addNode(define_neighbors);

            instance.graph.addNode(new XPosBuiltInNode());
            instance.graph.addNode(new YPosBuiltInNode());

            instance.graph.addNode(new SequenceNode());

            instance.graph.addNode(new IncrementNode());
            instance.graph.addNode(new IncrementNode());
            instance.graph.addNode(new IncrementNode());
            instance.graph.addNode(new IncrementNode());
            instance.graph.addNode(new IncrementNode());
            instance.graph.addNode(new IncrementNode());
            instance.graph.addNode(new IncrementNode());
            instance.graph.addNode(new IncrementNode());
            instance.graph.addNode(new IncrementNode());


//            alive.getPinFromName("random_bool").link(cell.getPinFromName("alive"));

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
            for(FunctionDirective node : FunctionDirective.values()){
                if(node.name().contains(seachData.get())) {
                    if(ImGui.button(node.name())){
//                        graph.addNode(new TemplateNode(FunctionDirectives.valueOf(node.name())));
                        ImGui.closeCurrentPopup();
                    }
                    ImGui.newLine();
                }
            }
            ImGui.endChildFrame();
            ImGui.endPopup();
        }
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

        ImNodes.endNodeEditor();

        renderAddNodePopup();

        if(should_open_popup){
            ImGui.openPopup(ADD_NODE_POPUP);
            should_open_popup = false;
        }
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
}
