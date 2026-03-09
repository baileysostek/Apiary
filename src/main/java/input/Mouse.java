package input;

import core.Apiary;
import graphics.GLDataType;
import graphics.ShaderManager;
import graphics.Uniform;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import util.EasingUtils;
import util.EnumInterpolation;
import util.MathUtil;

import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Mouse {

    // Functional Interfaces defined to be used in callback functions later
    @FunctionalInterface
    public interface MouseEventCallback<Button extends Integer, Action extends Integer, Modifiers extends Integer> {
        void apply(Button button, Action action, Modifiers modifiers);
    }

    @FunctionalInterface
    public interface MouseScrollEvent{
        void onScroll(int scroll_x, int scroll_y);
    }

    // Our Singleton Instance
    private static Mouse instance;

    // Variables used to track the mouse position
    private int mouse_x = 0;
    private int mouse_y = 0;

    private float mouse_scroll_target_x = 1f;
    private float mouse_scroll_target_y = 1f;
    private float mouse_scroll_x = 1f;
    private float mouse_scroll_y = 1f;

    private float easing_delta = 0;

    private final Vector2i vec_mouse_position = new Vector2i();

    // Map used to monitor mouse buttons
    private HashMap<Integer, Boolean> mouseKeys = new HashMap<>();

    // Hold onto a set of mouse Callbacks
    private HashSet<MouseEventCallback> mouse_event_callbacks = new HashSet<>();
    private LinkedList<MouseScrollEvent> mouse_scroll_events = new LinkedList<>();

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
            for(MouseEventCallback callback : mouse_event_callbacks){
                callback.apply(button, action, mods);
            }
            processMouse(button, action);
        });

        GLFW.glfwSetScrollCallback(Apiary.getWindowPointer(), (long window, double xoffset, double yoffset) -> {
            // Default for Engine operation
            mouse_scroll_target_x += (float) xoffset;
            mouse_scroll_target_x = Math.max(mouse_scroll_target_x, 1f);
            mouse_scroll_target_y += (float) yoffset;
            mouse_scroll_target_y = Math.max(mouse_scroll_target_y, 1f);

            easing_delta = 0.0f;

            // Process our scroll events
            for(MouseScrollEvent event : mouse_scroll_events){
                event.onScroll((int) xoffset, (int) yoffset);
            }
        });
    }

    public void addScrollEvent(MouseScrollEvent event){
        this.mouse_scroll_events.add(event);
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

        // Update easing
        easing_delta += delta / 100.0f;
        easing_delta = (float) Math.min(1.0, easing_delta);

        // Calculate how much the mouse has moved
        DoubleBuffer buffer_x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer buffer_y = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(Apiary.getWindowPointer(), buffer_x, buffer_y);
        buffer_x.rewind();
        buffer_y.rewind();

        int new_mouse_x = (int) buffer_x.get();
        int new_mouse_y = (int) buffer_y.get();

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

        // Update our Uniform Variables
        u_mouse_pos_pixels.set((int)screen_pos_normalized_device_coords_x, (int)screen_pos_normalized_device_coords_y);

        // Smoothly lerp towards destination
        mouse_scroll_x = EasingUtils.easeBetween(mouse_scroll_x, mouse_scroll_target_x, easing_delta, EnumInterpolation.EASE_OUT_CUBIC);
        mouse_scroll_y = EasingUtils.easeBetween(mouse_scroll_y, mouse_scroll_target_y, easing_delta, EnumInterpolation.EASE_OUT_CUBIC);

        u_mouse_scroll.set(mouse_scroll_x, mouse_scroll_y);

        u_mouse_pressed.set(
            mouseKeys.get(0) ? 1.0f : 0.0f,
            mouseKeys.get(1) ? 1.0f : 0.0f,
            mouseKeys.get(2) ? 1.0f : 0.0f,
            mouseKeys.get(3) ? 1.0f : 0.0f
        );

        // Update our vector
        vec_mouse_position.set(mouse_x, mouse_y);
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

    public Vector2i getMousePosition() {
        return this.vec_mouse_position;
    }

    public void addMouseEventCallback(MouseEventCallback callback){
        this.mouse_event_callbacks.add(callback);
    }

    public Uniform getScrollUniform() {
        return this.u_mouse_scroll;
    }

    public Uniform getPositionUniform() {
        return this.u_mouse_pos_pixels;
    }
}
