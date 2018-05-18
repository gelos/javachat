package chat.base;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Class with implemented basic thread methods.
 */
public class WorkerThread implements Runnable {

  /** The thread. */
  private Thread thread;

  public final Thread getThread() {
    return thread;
  }

  /** The runningFlag flag. */
  private AtomicBoolean runningFlag;

  /**
   * Instantiates a new thread thread.
   */
  public WorkerThread() {
    runningFlag = new AtomicBoolean(false);
  }

  /**
   * Checks if is runningFlag.
   *
   * @return true, if is runningFlag
   */
  public boolean isRuning() {
    return runningFlag.get();
  }

  /**
   * Stop.
   */
  public void stop() {
    runningFlag.set(false);
  }

  /**
   * Start.
   */
  public void start() {
    this.start("");
  }

  /**
   * Start.
   */
  public void start(String threadPrefix) {
  
    runningFlag.set(true);
  
    //thread = (threadName.isEmpty()) ? new Thread(this) : new Thread(null, this, threadName);
    thread = new Thread(this);
    thread.setName(threadPrefix + thread.getName());
    thread.start();
    
  
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    // TODO Auto-generated method stub

  }

}
