package core;

import graphics.ShaderManager;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL43;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.MemoryUtil.memFree;

public class Window {
    @FunctionalInterface
    public interface WindowResizeCallback<Width extends Integer, Height extends Integer>{
        void apply(Width width, Height height);
    }

    private long window_pointer;

    private int width;
    private int height;
    private float aspect_ratio;

    protected Window(){
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

        width = video_mode.width();
        height = video_mode.height();
        aspect_ratio = (float)width / (float)height;

        // Create the window
        window_pointer = glfwCreateWindow(width, height, "Apiary Simulation Engine", NULL, NULL);
        if ( window_pointer == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window_pointer, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        glfwSetFramebufferSizeCallback(window_pointer, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long windowId, int width, int height) {
                // Update the window size
                if(width > 0 && height > 0){
                    Window.this.width  = width;
                    Window.this.height = height;

                    Window.this.aspect_ratio = (float)width / (float)height;
                    GL43.glViewport(0, 0, width, height);

                    System.out.println("The window changed size, the aspect ratio is " + aspect_ratio);
                }
            }
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window_pointer, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window_pointer,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window_pointer);
        // Enable v-sync
        glfwSwapInterval(0);

        // Make the window visible
        glfwShowWindow(window_pointer);
    }

    //Taken from:https://gamedev.stackexchange.com/questions/105555/setting-window-icon-using-glfw-lwjgl-3
    public void setIcon(String path){
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
                glfwSetWindowIcon(window_pointer, icons);

                STBImage.stbi_image_free(pixels32);
                STBImage.stbi_image_free(pixels16);
            }
        }

        memFree(comp);
        memFree(h);
        memFree(w);
    }

    public long getWindowPointer() {

        return window_pointer;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getAspectRatio() {
        return aspect_ratio;
    }
}
