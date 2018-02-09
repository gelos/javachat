package chat.base;

import java.util.concurrent.atomic.AtomicBoolean;

public class WorkerThread implements Runnable{

  private Thread worker;

  private AtomicBoolean running;

  public WorkerThread() {
    running = new AtomicBoolean(false);
  }

  public boolean isRuning() {
    return running.get();
  }
  
  public void stop() {
    running.set(false);
  }

  public void start() {

    // Setting running flag for while circle
    running.set(true);

    // Start new thread
    worker = new Thread(this);
    worker.start();

  }

  @Override
  public void run() {
    // TODO Auto-generated method stub
    
  }

}