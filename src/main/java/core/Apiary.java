package core;

import graphics.ShaderManager;
import input.Keyboard;
import input.Mouse;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.*;
import simulation.SimulationManager;
import util.JsonUtils;
import util.StringUtils;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

// Accelerating Agent Based Simulations with Modern OpenGL

public class Apiary {

    // The window handle
    private static long window;
    private static int window_width = 2560;
    private static int window_height = 1440;
    private static float aspect_ratio = (float)window_width / (float)window_height;

    // Variables used
    private static boolean RUNNING = false;

    private static int FRAMES = 0;

    public void run() {

        System.out.println("LWJGL:" + Version.getVersion());

        init();

        // Setup our exit callback
        Keyboard.getInstance().addPressCallback(GLFW_KEY_ESCAPE, () -> {
            RUNNING = false;
        });

        loop();
        clean();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, ShaderManager.GL_MAJOR_VERSION);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, ShaderManager.GL_MINOR_VERSION);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        //Figure out the monitor size
        GLFWVidMode video_mode = glfwGetVideoMode(glfwGetPrimaryMonitor());

//        window_width = 1024;
//        window_height = 1024;

        window_width = video_mode.width();
        window_height = video_mode.height();
        aspect_ratio = (float)window_width / (float)window_height;

        // Create the window
        window = glfwCreateWindow(window_width, window_height, "Apiary Simulation Engine", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long windowId, int width, int height) {
                // Update the window size
                if(width > 0 && height > 0){
                    Apiary.this.window_width  = width;
                    Apiary.this.window_height = height;
//                    glfwSetWindowSize(window, width, height);
                    Apiary.this.aspect_ratio = (float)width / (float)height;
                    GL43.glViewport(0, 0, width, height);
//                    Renderer.getInstance().resize(width, height);
//                    Renderer.getInstance().setScreenSize(width, height);
                    System.out.println("The window changed size, the aspect ratio is " + aspect_ratio);
                }
            }
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                window,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(0);

        // Make the window visible
        glfwShowWindow(window);

        // Set the window icon
        setWindowIcon("textures/apiary.png");

        // Create our capabilities
        GL.createCapabilities();

        // Initialize all of our singleton instances here
        JsonUtils.initialize();
        ShaderManager.initialize();
        SimulationManager.initialize();

        Mouse.initialize();
        Keyboard.initialize();

        Keyboard.getInstance().addPressCallback(GLFW.GLFW_KEY_P, () -> {
            SimulationManager.getInstance().unloadSimulation();
            SimulationManager.getInstance().load("simulations/gol.json");
        });

//        SimulationManager.getInstance().load("simulations/gol.json");
        SimulationManager.getInstance().load("simulations/physarum.jsonc");
//        SimulationManager.getInstance().load("simulations/screen_test.json");
//        SimulationManager.getInstance().load("simulations/3boids.json");
    }

    private void loop() {

        if(!RUNNING){

            RUNNING = true;

            double last = System.nanoTime();
            double runningDelta = 0;
            double frameDelta = 0;
            int frames = 0;

            final boolean[] closeRequested = {false};

            while (!closeRequested[0]) {

                if(glfwWindowShouldClose(window) || (!RUNNING)){
                    closeRequested[0] = true;
                }

                double now = System.nanoTime();
                frameDelta = ((now - last) / (double) 1000000000);
                runningDelta += frameDelta;

                if(GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == 1 || true) {
                    update(frameDelta);

                    if (runningDelta > 1) {
                        FRAMES = frames;
                        frames = 0;
                        runningDelta -= 1;
                        System.out.println("Frames: " + FRAMES);
                    }
                    render();

                    glfwSwapBuffers(window); // swap the color buffer //Actual render call
                }

                glfwPollEvents();

                frames++;

                last = now;
            }

            //Set running to false.
            RUNNING = false;

            //Close all managers that we have instantiated
            shutdown();
        }

    }

    private void update(double delta){
        // Here we have all Singletons which need to update.
        Mouse.getInstance().update(delta);
        Keyboard.getInstance().update(delta);
        ShaderManager.getInstance().update(delta);

        // Simulation manager should update last. This ensures that every other singleton which has uniforms has the chance to update those uniforms.
        SimulationManager.getInstance().update(delta);
    }

    private void render(){
        SimulationManager.getInstance().render();
    }

    private void shutdown(){

    }

    private void clean(){
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static int getWindowWidth(){
        return window_width;
    }

    public static int getWindowHeight(){
        return window_height;
    }

    public static float getAspectRatio() {
        return aspect_ratio;
    }

    public static long getWindowPointer(){
        return window;
    }

    //Taken from:https://gamedev.stackexchange.com/questions/105555/setting-window-icon-using-glfw-lwjgl-3
    public void setWindowIcon(String path){
        IntBuffer w = memAllocInt(1);
        IntBuffer h = memAllocInt(1);
        IntBuffer comp = memAllocInt(1);

        // Icons
        {
            ByteBuffer icon16;
            ByteBuffer icon32;
            try {
                icon16 = StringUtils.loadRaw(StringUtils.getPathToResources() + path, 2048);
                icon32 = StringUtils.loadRaw(StringUtils.getPathToResources() + path, 4096);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            try ( GLFWImage.Buffer icons = GLFWImage.malloc(2) ) {
                ByteBuffer pixels16 = STBImage.stbi_load_from_memory(icon16, w, h, comp, 4);
                icons
                        .position(0)
                        .width(w.get(0))
                        .height(h.get(0))
                        .pixels(pixels16);

                ByteBuffer pixels32 = STBImage.stbi_load_from_memory(icon32, w, h, comp, 4);
                icons
                        .position(1)
                        .width(w.get(0))
                        .height(h.get(0))
                        .pixels(pixels32);

                icons.position(0);
                glfwSetWindowIcon(window, icons);

                STBImage.stbi_image_free(pixels32);
                STBImage.stbi_image_free(pixels16);
            }
        }

        memFree(comp);
        memFree(h);
        memFree(w);
    }


    public static void main(String[] args) {
        new Apiary().run();
    }

}