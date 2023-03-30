package core;

import editor.Editor;
import graphics.ShaderManager;
import graphics.texture.TextureManager;
import graphics.ui.FontLoader;
import input.Keyboard;
import input.Mouse;
import compiler.GLSLCompiler;
import nodegraph.NodeRegistry;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import simulation.SimulationManager;
import util.FileManager;
import util.JsonUtils;
import util.ThreadUtils;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;

// Accelerating Agent Based Simulations with Modern OpenGL

public class Apiary {

    // Our Window
    private static Window window;

    // Variables used
    private static boolean RUNNING = false;

    private static int FRAMES = 0;

    private static String FPS = "";

    public void run() {

        System.out.println("LWJGL:" + Version.getVersion());

        init();
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
        FileManager.initialize();
        JsonUtils.initialize();
        ShaderManager.initialize();
        TextureManager.initialize();
        GLSLCompiler.initialize();
        SimulationManager.initialize();
        FontLoader.initialize();
//        FontLoader.getInstance().loadFont("font/Roboto/Roboto-Regular.ttf", "roboto");
//        FontLoader.getInstance().loadFont("font/Roboto_Mono/static/RobotoMono-Regular.ttf", "roboto_mono");

        Mouse.initialize();
        Keyboard.initialize();

        // Init our NodeManager TODO API for external jars being included in the reflection search.
        NodeRegistry.initialize();

        Editor.initialize();

//        SimulationManager.getInstance().load("simulations/gol.json");
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
                        FPS = "Frames: " + FRAMES;
                        System.out.println(FPS);
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

    public static String getFPS(){
        return FPS;
    }

    public static void close(){
        RUNNING = false;
    }

    public static void main(String[] args) {
        new Apiary().run();
    }

}