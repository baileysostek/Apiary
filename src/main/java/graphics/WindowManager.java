package graphics;

public class WindowManager {

    // Singleton Instance
    private static WindowManager singleton;
    // Singleton variables

    private WindowManager() {

    }

    // Singleton initializer and getter
    public static void initialize() {
        if (singleton == null) {
            singleton = new WindowManager();
        }
    }

    public static WindowManager getInstance() {
        return singleton;
    }

}
