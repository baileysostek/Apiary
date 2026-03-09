package util;

import org.lwjgl.opengl.GL46;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadUtils {

    @FunctionalInterface
    public interface OnMainThread{
        Object run();
    }

    // Used to help Govern when a thread is allowed to do work
    private static Queue<DeferredTask> tasks = new LinkedList<>();
    //Lock for locking our task set
    private static Lock lock = new ReentrantLock();

    public static boolean isMainThread(){
        return Thread.currentThread().getName().equals("main");
    }

    public static Object runOnMainThread(OnMainThread callback){
        if(ThreadUtils.isMainThread()){
            return callback.run();
        }else{
            DeferredTask task = performTask(callback);
            while(!task.isDone()){
                Thread.yield();
            }
            return task.getResults();
        }
    }

    public static void update(double delta){
        try {
            lock.lock();
            for (DeferredTask task : tasks) {
                task.performTask();
            }
            tasks.clear();
        }finally{
            lock.unlock();
        }
    }

    private static DeferredTask performTask(OnMainThread callback){
        DeferredTask task = new DeferredTask(callback);
        try {
            lock.lock();
            tasks.add(task);
        }finally{
            lock.unlock();
        }
        return task;
    }

    private static class DeferredTask{

        private OnMainThread task;
        private Object results;

        private boolean done = false;

        protected DeferredTask(OnMainThread task){
            this.task = task;
        }

        public void performTask(){
            results = task.run();
            done = true;
        }

        public boolean isDone() {
            return done;
        }

        public Object getResults() {
            return results;
        }
    }
}