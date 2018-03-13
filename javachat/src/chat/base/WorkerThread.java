package chat.base;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Class with implemented basic thread methods.
 */
public class WorkerThread implements Runnable {

  /** The worker. */
  private Thread worker;

  /** The running flag. */
  private AtomicBoolean running;

  /**
   * Instantiates a new worker thread.
   */
  public WorkerThread() {
    running = new AtomicBoolean(false);
  }

  /**
   * Checks if is running.
   *
   * @return true, if is running
   */
  public boolean isRuning() {
    return running.get();
  }

  /**
   * Stop.
   */
  public void stop() {
    running.set(false);
  }

  /**
   * Start.
   */
  public void start() {

    // Setting running flag for while circle
    running.set(true);

    // Start new thread
    worker = new Thread(this);
    worker.start();

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
