package chat.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import static chat.base.CommandName.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import chat.server.Server;

// TODO: Auto-generated Javadoc
/**
 * The Class ClientPresenter. Implementing chat client logic.
 */
public class ClientPresenter implements Presenter {
  /**
   * Logger for this class
   */
  private static final Logger logger = LoggerFactory.getLogger(ClientPresenter.class);
  //private static final Logger loggerDebug = LoggerFactory.getLogger("debug");
  private static final Logger loggerDebugMDC = LoggerFactory.getLogger("debug.MDC");

  private final static String MSG_ASK_FOR_USERNAME = "Enter username to start сhat: ";

  private final static String MSG_EMPTY_USRENAME = "Username cannot be empty.";

  private final static String MSG_CANT_CON_SRV = "Can't connect to server " + Server.SERVER_IP + ":"
      + Server.SERVER_PORT + ". Server not started.";

  private final static int MAX_TIMEOUT_SESSION_OPEN_MS = 100;

  private View view;

  private Socket serverSocket = null;

  private ProcessCommandThreadClass processCommandThread = null;

  private ObjectOutputStream outputStream = null;

  private ObjectInputStream inputStream = null;

  private AtomicBoolean isSessionOpened;

  /** The Constant DEFAULT_WINDOW_NAME. */
  public static final String DEFAULT_WINDOW_NAME = "Java Chat Client";
  private static final String THREAD_NAME_CLIENT = "client-";

  /**
   * Create an instance of a new chat client presenter.
   */
  public ClientPresenter() {
    isSessionOpened = new AtomicBoolean(false);
    /*
     * loggerDebug.debug(Thread.currentThread().getName()); if
     * (!Thread.currentThread().getName().contains(THREAD_NAME_CLIENT)) {
     * Thread.currentThread().setName(THREAD_NAME_CLIENT + Thread.currentThread().getName()); }
     */
  }

  /**
   * Updating view for a new session start.
   */
  public void onViewStart() {
    getView().onConnectionOpening(DEFAULT_WINDOW_NAME);
    String[] emptyUserList = new String[0];
    getView().onUpdateChatUserList(emptyUserList);
    getView().onReceiveMessage(MSG_ASK_FOR_USERNAME);
  }

  /**
   * Connect to chat server. Open socket, prepare input/output streams, create new thread to data
   * transfer.
   *
   * @param username the user name string
   */
  @Override
  public void openConnection(String username) {

    MDC.put("username", username);

    getView().onConnectionOpening(DEFAULT_WINDOW_NAME);
    isSessionOpened.set(false);

    try {

      serverSocket = new Socket(Server.SERVER_IP, Server.SERVER_PORT);
      outputStream =
          new ObjectOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));

    } catch (UnknownHostException uhe) {
      logger.error("openConnection(String)", uhe); //$NON-NLS-1$
      
    } catch (IOException ioe) {
      logger.error("openConnection(String)", ioe); //$NON-NLS-1$

    }

    try {

      // TODO create static create method, refactor command Command.create().send()
      //Command enterCommand = ;
      // loggerDebugMDC.debug(enterCommand.toString());
      new Command(CMDENTER, "", username).send(outputStream);

      inputStream = new ObjectInputStream(new BufferedInputStream(serverSocket.getInputStream()));

      processCommandThread = new ProcessCommandThreadClass();
      processCommandThread.start(THREAD_NAME_CLIENT);

    } catch (IOException e1) {
      logger.error("openConnection(String)", e1); //$NON-NLS-1$

      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    // Waiting for the '/ok enter' command to be received from the server
    int waitingForOKEnterTimeoutMiliseconds = 0;
    while (!isSessionOpened.get()
        && (waitingForOKEnterTimeoutMiliseconds <= MAX_TIMEOUT_SESSION_OPEN_MS)) {
      try {
        TimeUnit.MILLISECONDS.sleep(1);
      } catch (InterruptedException e) {
        logger.error("openConnection(String)", e); //$NON-NLS-1$

        e.printStackTrace();
      }
      waitingForOKEnterTimeoutMiliseconds++;
    }

    if (isSessionOpened.get()) { // we receive ok enter command

      // do that we must do in View on session open
      getView().onConnectionOpened(username);

      // res = true;

    } else {
      // TODO closeConnection here
      MDC.clear();
      getView().onConnectionClosed(ClientPresenter.DEFAULT_WINDOW_NAME);
      // stop message handler
      processCommandThread.stop();
      String msg = "Can't connect to the server, timeout " + MAX_TIMEOUT_SESSION_OPEN_MS
          + ". Check server, try again or increase open session timeout.";
      System.out.println(msg);
      getView().showErrorWindow(msg, "Open session timeout.");

    }

    // return res;

  }

  /**
   * Close connection.
   *
   * @see chat.client.mvp.swing.Presenter#closeConnection()
   */
  @Override
  public void closeConnection() {

    MDC.clear();

    getView().onConnectionClosing(ClientPresenter.DEFAULT_WINDOW_NAME);

    System.out.println("Closing client...");

    System.out.println("Send exit command and close connection");

    // stop message handler thread BEFORE closing server socket and associated
    // streams
    if ((processCommandThread != null) && (processCommandThread.isRunning())) {
      processCommandThread.stop();
    }

    if (serverSocket != null && serverSocket.isConnected()) {
      // send to server exit command
      new Command(CommandName.CMDEXIT, "").send(outputStream);

      try {
        serverSocket.close();
      } catch (IOException e) {
        logger.error("closeConnection()", e); //$NON-NLS-1$
      }
    }

    System.out.println("Client stopped.");
    getView().onConnectionClosed(ClientPresenter.DEFAULT_WINDOW_NAME);
  }

  /**
   * Processes client console input messages as chat command. Supported commands:
   * <li>{@link CommandName#CMDENTER}
   * <li>{@link CommandName#CMDEXIT}
   * <li>{@link CommandName#CMDMSG}
   * <li>{@link CommandName#CMDPRVMSG}
   *
   * @param commandString the command string
   * @see chat.client.mvp.swing.Presenter#sendCommand(java.lang.String)
   */
  @Override
  public void sendCommand(String commandString) {
    Command command = new Command(commandString);
    switch (command.getCommandName()) {
      case CMDEXIT:
        // TODO duplicate send exit command
        closeConnection();
        onViewStart();
        break;
      case CMDPRVMSG:
      case CMDENTER:
      case CMDMSG:
        command.send(outputStream);
        getView().onSendMessage();
        break;
      case CMDERR:
        getView().showErrorWindow(
            "Wrong format or command \"" + command.getMessage() + "\" not supported.", "Error");
        getView().onSendMessage();
        break;
      default:
        new Command(CommandName.CMDMSG, commandString).send(outputStream);
        getView().onSendMessage();
        break;
    }
  }

  /**
   * Send private message.
   *
   * @param message the message
   * @param userList the user list
   * @see chat.client.mvp.swing.Presenter#sendPrivateMessage(java.lang.String, java.lang.String)
   */
  @Override
  public void sendPrivateMessage(String message, String userList) {
    new Command(CommandName.CMDPRVMSG, message, userList).send(outputStream);
    getView().onSendMessage();
  }

  /**
   * The Class ClientHandler.
   */
  class ProcessCommandThreadClass extends WorkerThread {

    @Override
    public void run() {

      // Command command;
      try {

        // while ((command = (Command) inputStream.readObject()) != null && isRunning()) {
        while (isRunning()) {

          Command command = (Command) inputStream.readObject();
          loggerDebugMDC.debug(command.toString());
          // loggerRoot.debug("run() - {}", this.toString() + command); //$NON-NLS-1$

          // loggerRoot.debug("processCommand(Command) - user {}, command {}", ((user ==
          // null) ? "" : user.getUsername()), //$NON-NLS-1$
          // command);

          switch (command.getCommandName()) {

            case CMDERR:
              logger.debug("run() - {}", //$NON-NLS-1$
                  "ClientPresenter.ProcessCommandThreadClass.run()" + command.getMessage()); //$NON-NLS-1$
              getView().showErrorWindow(command.getMessage(), "Error");
              break;

            case CMDEXIT:
              closeConnection();
              break;

            case CMDHLP:
              // TODO complete
              break;

            case CMDOK:
              if (command.getPayload().equals(CommandName.CMDENTER.toString())) {
                isSessionOpened.set(true);
              }
              break;

            case CMDMSG:
            case CMDPRVMSG:
              getView().onReceiveMessage(command.getMessage());
              break;

            case CMDUSRLST:
              // Update userList
              getView().onUpdateChatUserList(command.getPayload().split(" "));
              break;

            default:
              // TODO save unknown commands to log file
              getView().showWarningWindow(command.toString(), "Unknown command");
          }

        }
      } catch (IOException ioe) {
        logger.debug("run() - {}", ioe.getMessage()); //$NON-NLS-1$
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        logger.error("run()", e); //$NON-NLS-1$
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.swing.Presenter#setView(chat.client.mvp.swing.View)
   */
  @Override
  public void setView(View view) {
    this.view = view;

  }

  /**
   * Gets the view.
   *
   * @return the view
   */
  public View getView() {
    if (view == null) {
      throw new IllegalStateException("The view is not set.");
    } else {
      return this.view;
    }
  }

}
