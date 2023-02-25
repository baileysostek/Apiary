package core;

import editor.Editor;
import graphics.ShaderManager;
import graphics.texture.TextureManager;
import graphics.ui.FontLoader;
import input.Keyboard;
import input.Mouse;
import nodes.NodeManager;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.*;
import org.lwjgl.util.nfd.NFDPathSet;
import org.lwjgl.util.nfd.NativeFileDialog;
import org.lwjgl.util.nfd.NativeFileDialog.*;
import simulation.SimulationManager;
import util.JsonUtils;
import util.StringUtils;
import util.ThreadUtils;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

// Accelerating Agent Based Simulations with Modern OpenGL

public class Apiary {

    // Our Window
    private static Window window;

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

//        openSingle();
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        this.window = new Window();

        // Set the window icon
        this.window.setIcon("textures/apiary.png");

        // Create our capabilities
        GL.createCapabilities();

        // Initialize all of our singleton instances here
        JsonUtils.initialize();
        ShaderManager.initialize();
        TextureManager.initialize();
        NodeManager.initialize();
        SimulationManager.initialize();
        FontLoader.initialize();
        FontLoader.getInstance().loadFont("font/Roboto/Roboto-Regular.ttf", "roboto");
        FontLoader.getInstance().loadFont("font/Roboto_Mono/static/RobotoMono-Regular.ttf", "roboto_mono");

        Mouse.initialize();
        Keyboard.initialize();

        Editor.initialize();

        SimulationManager.getInstance().load("simulations/gol.json");
//        SimulationManager.getInstance().load("simulations/physarum.jsonc");
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

                if(glfwWindowShouldClose(window.getWindowPointer()) || (!RUNNING)){
                    closeRequested[0] = true;
                }

                double now = System.nanoTime();
                frameDelta = ((now - last) / (double) 1000000000);
                runningDelta += frameDelta;

                if(GLFW.glfwGetWindowAttrib(window.getWindowPointer(), GLFW.GLFW_FOCUSED) == 1 || true) {
                    update(frameDelta);

                    if (runningDelta > 1) {
                        FRAMES = frames;
                        frames = 0;
                        runningDelta -= 1;
                        System.out.println("Frames: " + FRAMES);
                    }
                    render();

                    glfwSwapBuffers(window.getWindowPointer()); // swap the color buffer //Actual render call
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

    private static void openSingle() {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);

            String filterList = "png,jpg";
            String defaultPath = "";

            checkResult(NativeFileDialog.NFD_OpenDialog(filterList, defaultPath, pp), pp);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void checkResult(int result, PointerBuffer path) {
        switch (result) {
            case NativeFileDialog.NFD_OKAY:
                System.out.println("Success!");
                System.out.println(path.getStringUTF8(0));
//                NativeFileDialog.NFD_FreePath(path.get(0));
                break;
            case NativeFileDialog.NFD_CANCEL:
                System.out.println("User pressed cancel.");
                break;
            default: // NFD_ERROR
                System.err.format("Error: %s\n", NativeFileDialog.NFD_GetError());
        }
    }

    private void update(double delta){
        // Here we have all Singletons which need to update.
        Mouse.getInstance().update(delta);
        Keyboard.getInstance().update(delta);

        // Process all tasks enqueued from other threads
        ThreadUtils.update(delta);

        ShaderManager.getInstance().update(delta);

        // Simulation manager should update last. This ensures that every other singleton which has uniforms has the chance to update those uniforms.
        SimulationManager.getInstance().update(delta);

        Editor.getInstance().update(delta);
    }

    private void render(){
        SimulationManager.getInstance().render();

        Editor.getInstance().render();
    }

    private void shutdown(){

    }

    private void clean(){
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window.getWindowPointer());
        glfwDestroyWindow(window.getWindowPointer());

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static int getWindowWidth(){
        return window.getWidth();
    }

    public static int getWindowHeight(){
        return window.getHeight();
    }

    public static float getAspectRatio() {
        return window.getAspectRatio();
    }

    public static long getWindowPointer(){
        return window.getWindowPointer();
    }

    public static void main(String[] args) {
        new Apiary().run();
    }

}