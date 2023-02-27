package util;

public class Promise implements Runnable {

    @FunctionalInterface
    public interface PromiseBody{
        void callback();
    }

    private Thread thread;
    private PromiseBody callback;
    private PromiseBody then;

    private boolean isRunning = false;

    public Promise(PromiseBody callback){
        this.callback = callback;
        this.thread = new Thread(this);
        start();
    }

    public void start(){
        if(!this.isRunning) {
            this.isRunning = true;
            this.thread.start();
        }
    }

    public void stop(){
        if(isRunning) {
            this.isRunning = false;
            this.thread.stop();
            this.then.callback();
        }
    }

    @Override
    public void run() {
        this.callback.callback();
        if(then != null) {
            this.then.callback();
        }
    }

    public void then(PromiseBody callback) {
        this.then = callback;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
