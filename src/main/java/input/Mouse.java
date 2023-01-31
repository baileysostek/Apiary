package input;

import core.Apiary;
import graphics.GLDataType;
import graphics.ShaderManager;
import graphics.Uniform;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import java.nio.DoubleBuffer;
import java.util.HashMap;

public class Mouse {
    private static Mouse instance;

    // Variables used to track the mouse position
    private int mouse_x = 0;
    private int mouse_y = 0;

    private float mouse_scroll_x = 1f;
    private float mouse_scroll_y = 1f;

    // These are the frame deltas
    private int mouse_delta_x = 0;
    private int mouse_delta_y = 0;

    // Buffers


    // Map used to monitor mouse buttons
    private HashMap<Integer, Boolean> mouseKeys = new HashMap<>();

    // Uniform variables
    private Uniform u_mouse_pos_pixels = ShaderManager.getInstance().createUniform("u_mouse_pos_pixels", GLDataType.VEC2);
    private Uniform u_mouse_scroll = ShaderManager.getInstance().createUniform("u_mouse_scroll", GLDataType.VEC2);
    private Uniform u_mouse_pressed = ShaderManager.getInstance().createUniform("u_mouse_pressed", GLDataType.VEC4);

    private Mouse(){
        // Init our keymap
        mouseKeys.put(0, false);
        mouseKeys.put(1, false);
        mouseKeys.put(2, false);
        mouseKeys.put(3, false);

        GLFW.glfwSetMouseButtonCallback(Apiary.getWindowPointer(), (long window, int button, int action, int mods) -> {
            processMouse(button, action);
        });

        GLFW.glfwSetScrollCallback(Apiary.getWindowPointer(), (long window, double xoffset, double yoffset) -> {
            // Default for Engine opperation
            mouse_scroll_x += (float) xoffset;
            mouse_scroll_x = Math.max(mouse_scroll_x, 1f);
            mouse_scroll_y += (float) yoffset;
            mouse_scroll_y = Math.max(mouse_scroll_y, 1f);
        });
    }

    private void processMouse(int button, int action){
        if(action == GLFW.GLFW_PRESS){
            mouseKeys.put(button, true);
        }

        if(action == GLFW.GLFW_RELEASE){
            mouseKeys.put(button, false);
        }
    }

    public void update(double delta){

        // Calculate how much the mouse has moved
        DoubleBuffer buffer_x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer buffer_y = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(Apiary.getWindowPointer(), buffer_x, buffer_y);
        buffer_x.rewind();
        buffer_y.rewind();

        int new_mouse_x = (int) buffer_x.get();
        int new_mouse_y = (int) buffer_y.get();

        // Check if the mouse has moved
        if(mouse_x != new_mouse_x || mouse_y != new_mouse_y){
            mouse_delta_x = (new_mouse_x - mouse_x);
            mouse_delta_y = (new_mouse_y - mouse_y);
        }

        // Set the mouse position
        mouse_x = new_mouse_x;
        mouse_y = new_mouse_y;

//        ray = calculateMouseRay();

        // Update our mouse pos uniform
        float screen_pos_normalized_device_coords_x = (((mouse_x / (float)Apiary.getWindowWidth()) * 2.0f) - 1.0f);
        float screen_pos_normalized_device_coords_y = (((((float)Apiary.getWindowHeight() - mouse_y) / (float)Apiary.getWindowHeight()) * 2.0f) - 1.0f);
        // Scale by the zoom level
        // Convert back to screenspace.
        screen_pos_normalized_device_coords_x = (((screen_pos_normalized_device_coords_x / mouse_scroll_y) + 1.0f) / 2.0f) * Apiary.getWindowWidth();
        screen_pos_normalized_device_coords_y = (((screen_pos_normalized_device_coords_y / mouse_scroll_y) + 1.0f) / 2.0f) * Apiary.getWindowHeight();
        u_mouse_pos_pixels.set((int)screen_pos_normalized_device_coords_x, (int)screen_pos_normalized_device_coords_y);

        u_mouse_scroll.set(mouse_scroll_x, mouse_scroll_y);

        u_mouse_pressed.set(
            mouseKeys.get(0) ? 1.0f : 0.0f,
            mouseKeys.get(1) ? 1.0f : 0.0f,
            mouseKeys.get(2) ? 1.0f : 0.0f,
            mouseKeys.get(3) ? 1.0f : 0.0f
        );
    }

    public void bindUniforms(){
        this.u_mouse_pos_pixels.bind();
    }

    public static void initialize(){
        if(instance == null){
            instance = new Mouse();
        }
    }

    public static Mouse getInstance(){
        return instance;
    }
}
