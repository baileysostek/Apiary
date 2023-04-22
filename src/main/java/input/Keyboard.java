package input;

import core.Apiary;
import editor.Editor;
import imgui.ImGui;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntConsumer;

public class Keyboard {

    @FunctionalInterface
    public interface KeyEventCallback<KeyName extends String, KeyCode extends Integer, Action extends Integer> {
        void callback(KeyName key_name, KeyCode key_code, Action action);
    }

    @FunctionalInterface
    public interface KeyPressCallback {
        void callback();
    }

    @FunctionalInterface
    public interface KeyHoldCallback {
        void callback();
    }

    @FunctionalInterface
    public interface KeyReleaseCallback {
        void callback();
    }

    private static Keyboard keyboard;
    private boolean[] keys = new boolean[512]; // No out of bounds
    private LinkedHashMap<KeyPressCallback, Boolean>[] pressedCallbacks = new LinkedHashMap[keys.length];
    private LinkedList<KeyHoldCallback>[] holdCallbacks = new LinkedList[keys.length];
    private LinkedList<KeyReleaseCallback>[] releasedCallbacks = new LinkedList[keys.length];
    private LinkedList<KeyEventCallback> anyKeyCallback = new LinkedList<>();

    //Hashing to store where a callback lives
    private HashMap<Integer, Integer> key_lookup = new HashMap<>();

    //Lock for locking our entity set
    private Lock lock;

    private Keyboard(){
        //Array init
        for(int i = 0; i < keys.length; i++){
            keys[i] = false;
            pressedCallbacks[i] = new LinkedHashMap<KeyPressCallback, Boolean>();
            holdCallbacks[i] = new LinkedList<KeyHoldCallback>();
            releasedCallbacks[i] = new LinkedList<KeyReleaseCallback>();
        }
        //Callback stuff
        GLFW.glfwSetKeyCallback(Apiary.getWindowPointer(), new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {

                if(ImGui.getIO().getWantTextInput()) {
                    Editor.getInstance().processKeyEvent(key, scancode, action, mods);
                }else{
                    // Editor does not want the action so we will process it ourselves.
                    processKeyEvent(key, scancode, action, mods);
                }
            }
        });

        lock = new ReentrantLock();
    }

    private void processKeyEvent(int key, int scancode, int action, int mods){
        if (key < keys.length && key >= 0) {
            if (action == GLFW.GLFW_RELEASE) {
                keys[key] = false;
                for (KeyReleaseCallback release_callback : releasedCallbacks[key]) {
                    release_callback.callback();
                }
                for (KeyPressCallback callback : pressedCallbacks[key].keySet()) {
                    pressedCallbacks[key].put(callback, true);
                }
            } else {
                if (action == GLFW.GLFW_PRESS) {
                    if(!keys[key]){
                        for (KeyPressCallback callback : pressedCallbacks[key].keySet()) {
                            if(pressedCallbacks[key].get(callback)) {
                                callback.callback();
                                pressedCallbacks[key].put(callback, false);
                            }
                        }
                    }
                }
                keys[key] = true;

                for(KeyEventCallback callback : anyKeyCallback){
                    callback.callback(GLFW.glfwGetKeyName(key, scancode), key, action);
                }
            }
        }
    }

    public void update(double delta){
        int key_code = 0;
        for(boolean key_pressed : keys) {
            if(key_pressed) {
                for (KeyHoldCallback hold_callback : holdCallbacks[key_code]) {
                    hold_callback.callback();
                }
            }
            key_code++;
        }
    }

    public void onShutdown() {

    }

    public static void initialize(){
        if(keyboard == null){
            keyboard = new Keyboard();
        }
    }

    public static Keyboard getInstance(){
        return keyboard;
    }

    public void addPressCallback(int key, KeyPressCallback key_press_callback){
        //index where this callback lives
        key_lookup.put(key_press_callback.hashCode(), key);
        //Set the callback
        this.pressedCallbacks[key].put(key_press_callback, true);
    }

    public void addHoldCallback(int key, KeyHoldCallback key_hold_callback){
        //index where this callback lives
        key_lookup.put(key_hold_callback.hashCode(), key);
        //Set the callback
        this.holdCallbacks[key].add(key_hold_callback);
    }

    public void addReleaseCallback(int key, KeyReleaseCallback key_release_callback){
        //index where this callback lives
        key_lookup.put(key_release_callback.hashCode(), key);
        //Set the callback
        this.releasedCallbacks[key].add(key_release_callback);
    }

    public void addKeyEventCallback(KeyEventCallback callback){
        this.anyKeyCallback.add(callback);
    }

    public boolean isKeyPressed(int ... keys){
        for(int a_key : keys){
            if(this.keys[a_key]){
                return true;
            }
        }
        return false;
    }

    public boolean isKeyPressed(int key){
        return keys[key];
    }

    public void removeCallback(IntConsumer callback) {
        if(key_lookup.containsKey(callback)) {
            int key = key_lookup.get(callback);

            if (pressedCallbacks[key].containsKey(callback)) {
                pressedCallbacks[key].remove(callback);
            }

            if (holdCallbacks[key].contains(callback)) {
                holdCallbacks[key].remove(callback);
            }

            if (releasedCallbacks[key].contains(callback)) {
                releasedCallbacks[key].remove(callback);
            }
        }

        if(anyKeyCallback.contains(callback)){
            anyKeyCallback.remove(callback);
        }
    }
}
