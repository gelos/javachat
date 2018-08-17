package chat.base;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * The basic class for Session. Process all session activities.
 */
public abstract class Session {

  /** The logger. */
  protected final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

  /** The Constant loggerDebugMDC. */
  protected static final Logger loggerDebugMDC = LoggerFactory.getLogger("debug.MDC");

  /** The is session opened flag. */
  protected AtomicBoolean isSessionOpenedFlag;

  /** The chat user. */
  protected User user = null;

  /** The command handler. */
  protected CommandHandler commandHandler = null;

  /** The thrd name. */
  private String thrdName;

  /**
   * Instantiates a new chat session.
   */
  public Session() {
    this("");
  }


  /**
   * Instantiates a new chat session and set thread name to {@link CommandHandler} thread.
   *
   * @param thrdName the thrd name
   */
  public Session(String thrdName) {
    this.thrdName = thrdName;
    isSessionOpenedFlag = new AtomicBoolean(false);
  }

  /**
   * Run {@link CommandHandler} for processing commands in session. Open output and input streams.
   *
   * @param clientSocket the client socket
   */
  public void runCommandHandler(Socket clientSocket) {
    commandHandler = new CommandHandler(clientSocket, this);
    commandHandler.start(thrdName);
  }

  /**
   * Return true if session is open.
   *
   * @return the checks if is session opened flag
   */
  public final Boolean getIsSessionOpenedFlag() {
    return isSessionOpenedFlag.get();
  }

  /**
   * Open session. Initialize MDC with username to print it in logs.
   *
   * @param userName the name of user
   */
  public void open(String userName) {
    MDC.put("username", userName);
  }

  /**
   * Method with business logic for input {@link Command}. Called from {@link CommandHandler#run()}.
   *
   * @param command the command
   */
  public void receive(Command command) {
    loggerDebugMDC.debug(command.toString());
  };

  /**
   * Send command.
   *
   * @param command the command
   */
  public void send(Command command) {
    if ((commandHandler != null) && commandHandler.getIsOutputStreamOpened()) {
      commandHandler.send(command);
    }
  }

  /**
   * All the stuff that we need to do when closing the session. Stop {@link CommandHandler}, clear
   * MDC, dispose user.
   *
   * @param sendEXTCMD boolean flag, if true then sending EXTCMD
   */
  public void close(boolean sendEXTCMD) {

    if (sendEXTCMD) {

      send(new Command(CommandName.CMDEXIT, ""));
    }


    if ((commandHandler != null) && (commandHandler.isRunning())) {
      commandHandler.stop();
      try {
        commandHandler.getThread().join(Constants.THR_WAIT_TIMEOUT);
      } catch (InterruptedException e) {
        logger.error(this.getClass().getSimpleName() + "." + "closeSession(boolean)", e); //$NON-NLS-1$

      }

    }

    MDC.clear();
    user = null;

  };

}
