package util;

public class ThreadUtils {
    public static boolean isMainThread(){
        return Thread.currentThread().getName().equals("main");
    }
}