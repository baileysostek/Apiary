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

    // These are the frame deltas
    private int mouse_delta_x = 0;
    private int mouse_delta_y = 0;

    // Buffers


    // Map used to monitor mouse buttons
    private HashMap<Integer, Boolean> mouseKeys = new HashMap<>();

    // Uniform variables
    private Uniform u_mouse_pos_pixels = ShaderManager.getInstance().createUniform("u_mouse_pos_pixels", GLDataType.VEC2);

    private Mouse(){
        GLFW.glfwSetMouseButtonCallback(Apiary.getWindowPointer(), (long window, int button, int action, int mods) -> {
            processMouse(button, action);
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
        u_mouse_pos_pixels.set(mouse_x, Apiary.getWindowHeight() - mouse_y);
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
