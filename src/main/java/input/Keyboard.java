package input;

import core.Apiary;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntConsumer;

public class Keyboard {

    @FunctionalInterface
    public interface KeyEventCallback<KeyName extends String, KeyCode extends Integer, Action extends Integer> {
        void apply(KeyName key_name,KeyCode key_code, Action action);
    }

    @FunctionalInterface
    public interface KeyPressCallback {
        void apply();
    }

    @FunctionalInterface
    public interface KeyHoldCallback {
        void apply();
    }

    @FunctionalInterface
    public interface KeyReleaseCallback {
        void apply();
    }

    private static Keyboard keyboard;
    private boolean[] keys = new boolean[512]; // No out of bounds
    private LinkedList<KeyPressCallback>[] pressedCallbacks = new LinkedList[keys.length];
    private LinkedList<KeyHoldCallback>[] holdCallbacks = new LinkedList[keys.length];
    private LinkedList<KeyReleaseCallback>[] releasedCallbacks = new LinkedList[keys.length];
    private LinkedList<KeyEventCallback> anyKeyCallback = new LinkedList<>();

    //Hashing to store where a callback lives
    private HashMap<Integer, Integer> key_lookup = new HashMap<>();

    //Key List
    public static final int A = KeyEvent.VK_A;
    public static final int B = KeyEvent.VK_B;
    public static final int C = KeyEvent.VK_C;
    public static final int D = KeyEvent.VK_D;
    public static final int E = KeyEvent.VK_E;
    public static final int F = KeyEvent.VK_F;
    public static final int G = KeyEvent.VK_G;
    public static final int H = KeyEvent.VK_H;
    public static final int I = KeyEvent.VK_I;
    public static final int J = KeyEvent.VK_J;
    public static final int K = KeyEvent.VK_K;
    public static final int L = KeyEvent.VK_L;
    public static final int M = KeyEvent.VK_M;
    public static final int N = KeyEvent.VK_N;
    public static final int O = KeyEvent.VK_O;
    public static final int P = KeyEvent.VK_P;
    public static final int Q = KeyEvent.VK_Q;
    public static final int R = KeyEvent.VK_R;
    public static final int S = KeyEvent.VK_S;
    public static final int T = KeyEvent.VK_T;
    public static final int U = KeyEvent.VK_U;
    public static final int V = KeyEvent.VK_V;
    public static final int W = KeyEvent.VK_W;
    public static final int X = KeyEvent.VK_X;
    public static final int Y = KeyEvent.VK_Y;
    public static final int Z = KeyEvent.VK_Z;

    //Function keys
    public static final int F1  = GLFW.GLFW_KEY_F1;
    public static final int F2  = GLFW.GLFW_KEY_F2;
    public static final int F3  = GLFW.GLFW_KEY_F3;
    public static final int F4  = GLFW.GLFW_KEY_F4;
    public static final int F5  = GLFW.GLFW_KEY_F5;
    public static final int F6  = GLFW.GLFW_KEY_F6;
    public static final int F7  = GLFW.GLFW_KEY_F7;
    public static final int F8  = GLFW.GLFW_KEY_F8;
    public static final int F9  = GLFW.GLFW_KEY_F9;
    public static final int F10 = GLFW.GLFW_KEY_F10;
    public static final int F11 = GLFW.GLFW_KEY_F11;
    public static final int F12 = GLFW.GLFW_KEY_F12;

    //Numbers
    public static final int ONE     = GLFW.GLFW_KEY_1;
    public static final int TWO     = GLFW.GLFW_KEY_2;
    public static final int THREE   = GLFW.GLFW_KEY_3;
    public static final int FOUR    = GLFW.GLFW_KEY_4;
    public static final int FIVE    = GLFW.GLFW_KEY_5;
    public static final int SIX     = GLFW.GLFW_KEY_6;
    public static final int SEVEN   = GLFW.GLFW_KEY_7;
    public static final int EIGHT   = GLFW.GLFW_KEY_8;
    public static final int NINE    = GLFW.GLFW_KEY_9;
    public static final int ZERO    = GLFW.GLFW_KEY_0;


    //SPECIALTY
    public static final int ESCAPE    = 0x100;
    public static final int DELETE    = 261;
    public static final int BACKSPACE = 259;
    public static final int SPACE     = GLFW.GLFW_KEY_SPACE;

    public static final int TILDE     = GLFW.GLFW_KEY_GRAVE_ACCENT;


    public static final int CONTROL_LEFT   = GLFW.GLFW_KEY_LEFT_CONTROL;
    public static final int CONTROL_RIGHT   = GLFW.GLFW_KEY_RIGHT_CONTROL;
    public static final int ALT_LEFT  = GLFW.GLFW_KEY_LEFT_ALT;
    public static final int SHIFT_LEFT = GLFW.GLFW_KEY_LEFT_SHIFT;
    public static final int SHIFT_RIGHT = GLFW.GLFW_KEY_RIGHT;

    //Lock for locking our entity set
    private Lock lock;

    private Keyboard(){
        //Array init
        for(int i = 0; i < keys.length; i++){
            keys[i] = false;
            pressedCallbacks[i] = new LinkedList<KeyPressCallback>();
            holdCallbacks[i] = new LinkedList<KeyHoldCallback>();
            releasedCallbacks[i] = new LinkedList<KeyReleaseCallback>();
        }
        //Callback stuff
        GLFW.glfwSetKeyCallback(Apiary.getWindowPointer(), new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                System.out.println(key);
//                if(Reactor.isDev()){
//                    if(!ImGui.getIO().getWantTextInput()) {
//                        processKeyEvent(window, key, scancode, action, mods);
//                    }
//                    Editor.getInstance().sendKeyEvent(window, key, scancode, action, mods);
//                }else{
//                    processKeyEvent(window, key, scancode, action, mods);
//                }
                processKeyEvent(key, scancode, action, mods);
            }
        });

        lock = new ReentrantLock();
    }

    private void processKeyEvent(int key, int scancode, int action, int mods){
        if (key < keys.length && key >= 0) {
            if (action == GLFW.GLFW_RELEASE) {
                keys[key] = false;
                for (KeyReleaseCallback release_callback : releasedCallbacks[key]) {
                    release_callback.apply();
                }
            } else {
                if (action == GLFW.GLFW_PRESS) {
                    if(!keys[key]){
                        for (KeyPressCallback callback : pressedCallbacks[key]) {
                            callback.apply();
                        }
                    }
                }
                keys[key] = true;

                for(KeyEventCallback callback : anyKeyCallback){
                    callback.apply(GLFW.glfwGetKeyName(key, scancode), key, action);
                }
            }
        }
    }

    public void update(double delta){
        int key_code = 0;
        for(boolean key_pressed : keys) {
            if(key_pressed) {
                for (KeyHoldCallback hold_callback : holdCallbacks[key_code]) {
                    hold_callback.apply();
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
        this.pressedCallbacks[key].add(key_press_callback);
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

    public boolean isKeyPressed(float key){
        int iKey = (int)key;
        return keys[iKey];
    }

    public void removeCallback(IntConsumer callback) {
        if(key_lookup.containsKey(callback)) {
            int key = key_lookup.get(callback);

            if (pressedCallbacks[key].contains(callback)) {
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
