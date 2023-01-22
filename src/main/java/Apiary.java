import graphics.GLDataType;
import graphics.SSBO;
import graphics.ShaderManager;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import simulation.Simulation;
import simulation.SimulationManager;
import util.JsonUtils;
import util.StringUtils;

import java.nio.*;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

// Accelerating Agent Based Simulations with Modern OpenGL

public class Apiary {

    // The window handle
    private long window;

    // Variables used
    private static boolean RUNNING = false;

    private static int FRAMES = 0;

    public void run() {

        System.out.println("LWJGL:" + Version.getVersion());

        init();
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
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);


        // Create the window
        window = glfwCreateWindow(1920, 1080, "Apiary Simulation Engine", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
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

        // Create our capabilities
        GL.createCapabilities();

        // Initialize all of our singleton instances here
        JsonUtils.initialize();
        ShaderManager.initialize();
        SimulationManager.initialize();

//        SimulationManager.getInstance().load("simulations/gol.json");
//        System.out.println(SimulationManager.getInstance());


        String vertex_source =
                "#version 460\n" +
                "layout(location = 0) in vec3 position;\n" +
                "void main(void){\n" +
                "gl_Position = vec4(position, 1);\n" +
                "}";

        String fragment_source =
                "#version 460\n" +
                "out vec4 out_Color; \n" +
                "void main(void){\n" +
                "out_Color = vec4(1, 0, 0, 1);\n" +
                "}";

        int vertex   = ShaderManager.getInstance().compileShader(GL46.GL_VERTEX_SHADER, vertex_source);
        int fragment = ShaderManager.getInstance().compileShader(GL46.GL_FRAGMENT_SHADER, fragment_source);


        System.out.println(vertex+","+fragment);
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

                if(GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) == 1) {
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
        if(SimulationManager.getInstance().hasActiveSimulation()){
            SimulationManager.getInstance().update(delta);
        }
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

    public static void main(String[] args) {
        new Apiary().run();
    }

}