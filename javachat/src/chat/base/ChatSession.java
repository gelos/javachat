package chat.base;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * The basic class for ChatSession. Process all session activities.
 */
public abstract class ChatSession {

  /** The logger. */
  protected final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
  protected static final Logger loggerDebugMDC = LoggerFactory.getLogger("debug.MDC");
    
  /** The is session opened flag. */
  protected AtomicBoolean isSessionOpenedFlag;

  /** The chat user. */
  protected User user = null;

  /** The command handler. */
  protected CommandHandler_new commandHandler = null;
  
  private String thrdName;

  
  public ChatSession(String thrdName) {
    this.thrdName = thrdName;
    isSessionOpenedFlag = new AtomicBoolean(false);
  }
  
  /**
   * Instantiates a new chat session.
   */
  public ChatSession() {
    this("");
  }

  /**
   * Instantiates a new chat session.
   *
   * @param clientSocket the client socket
   */
//  public ChatSession(Socket clientSocket) {
//    this();
//    runCommandHandler(clientSocket);
//  }

  /**
   * Run command handler.
   *
   * @param clientSocket the client socket
   */
  public void runCommandHandler(Socket clientSocket) {
    commandHandler = new CommandHandler_new(clientSocket, this);
    commandHandler.start(thrdName);
  }

  /**
   * Gets the checks if is session opened flag.
   *
   * @return the checks if is session opened flag
   */
  public final Boolean getIsSessionOpenedFlag() {
    return isSessionOpenedFlag.get();
  }

  /**
   * Send command.
   *
   * @param command the command
   */
  public void sendCommand(Command command) {
    commandHandler.send(command);
  };

  /**
   * Process command.
   *
   * @param command the command
   */
  public void processCommand(Command command) {
    loggerDebugMDC.debug(command.toString());
  };

  /**
   * Open session.
   *
   * @param username the username
   */
  public void openSession(String userName) {
    MDC.put("username", userName);
  };

  /**
   * Close session.
   *
   * @param sendEXTCMD the send EXTCMD
   */
  public void closeSession(boolean sendEXTCMD) {

    if (sendEXTCMD) {

      sendCommand(new Command(CommandName.CMDEXIT, ""));
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
