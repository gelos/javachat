package chat.base;

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
import chat.server.ChatServer;

// TODO: Auto-generated Javadoc
/**
 * The Class ChatClientPresenter. Implementing chat client logic.
 */
public class ChatClientPresenter implements Presenter {

  private final static String MSG_ASK_FOR_USERNAME = "Enter username to start сhat: ";

  private final static String MSG_EMPTY_USRENAME = "Username cannot be empty.";

  private final static String MSG_CANT_CON_SRV = "Can't connect to server " + ChatServer.SERVER_IP
      + ":" + ChatServer.SERVER_PORT + ". Server not started.";

  private final static int MAX_TIMEOUT_SESSION_OPEN = 3;

  private View view;

  private Socket serverSocket = null;

  private CommandHandler commandHandler = null;

  private ObjectOutputStream outputStream = null;

  private ObjectInputStream inputStream = null;

  private AtomicBoolean isSessionOpened;

  /** The Constant DEFAULT_WINDOW_NAME. */
  public static final String DEFAULT_WINDOW_NAME = "Java Chat Client";

  /**
   * Create an instance of a new chat client presenter.
   */
  public ChatClientPresenter() {
    isSessionOpened = new AtomicBoolean(false);
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

    getView().onConnectionOpening(DEFAULT_WINDOW_NAME);
    isSessionOpened.set(false);

    try {

      serverSocket = new Socket(ChatServer.SERVER_IP, ChatServer.SERVER_PORT);
      outputStream =
          new ObjectOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));

    } catch (UnknownHostException uhe) {
      // TODO save to debug
      System.out.println(uhe.getMessage());
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    }

    try {


      // TODO create static create method, refactor command ChatCommand.create().send()
      new ChatCommand(CMDENTER, "", username).send(outputStream);

      inputStream = new ObjectInputStream(new BufferedInputStream(serverSocket.getInputStream()));

      commandHandler = new CommandHandler();
      commandHandler.start();

    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    // Waiting for the '/ok enter' command to be received from the server
    int waitingForOKEnterTimeoutMiliseconds = 0;
    while (!isSessionOpened.get() && (waitingForOKEnterTimeoutMiliseconds <= MAX_TIMEOUT_SESSION_OPEN)) {
      try {
        TimeUnit.MILLISECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      waitingForOKEnterTimeoutMiliseconds++;
    }

    if (isSessionOpened.get()) { // we receive ok enter command

      // do that we must do in View on session open
      getView().onConnectionOpened(username);

      // res = true;

    } else {
      getView().onConnectionClosed(ChatClientPresenter.DEFAULT_WINDOW_NAME);
      // stop message handler
      commandHandler.stop();
      String msg = "Can't connect to the server, timeout " + MAX_TIMEOUT_SESSION_OPEN
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

    getView().onConnectionClosing(ChatClientPresenter.DEFAULT_WINDOW_NAME);

    System.out.println("Closing client...");

    System.out.println("Send exit command and close connection");

    // stop message handler thread BEFORE closing server socket and associated streams
    if ((commandHandler != null) && (commandHandler.isRuning())) {
      commandHandler.stop();
    }

    if (serverSocket != null && serverSocket.isConnected()) {
      // send to server exit command
      new ChatCommand(CommandName.CMDEXIT, "").send(outputStream);

      try {
        serverSocket.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    System.out.println("Client stopped.");
    getView().onConnectionClosed(ChatClientPresenter.DEFAULT_WINDOW_NAME);
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
    ChatCommand command = new ChatCommand(commandString);
    switch (command.getCommandName()) {
      case CMDEXIT:
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
        new ChatCommand(CommandName.CMDMSG, commandString).send(outputStream);
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
    new ChatCommand(CommandName.CMDPRVMSG, message, userList).send(outputStream);
    getView().onSendMessage();
  }


  /**
   * The Class CommandHandler.
   */
  class CommandHandler extends WorkerThread {

    /*
     * (non-Javadoc)
     * 
     * @see chat.base.WorkerThread#run()
     */
    @Override
    public void run() {

      // TODO Auto-generated method stub
      ChatCommand chatCommand;
      try {

        while ((chatCommand = (ChatCommand) inputStream.readObject()) != null && isRuning()) {

          System.out.println(this.toString() + chatCommand);

          switch (chatCommand.getCommandName()) {

            case CMDERR:
              System.out
                  .println("ChatClientPresenter.CommandHandler.run()" + chatCommand.getMessage());
              getView().showErrorWindow(chatCommand.getMessage(), "Error");
              break;

            case CMDEXIT:
              closeConnection();
              break;

            case CMDHLP:
              // TODO complete
              break;

            case CMDOK:
              if (chatCommand.getPayload().equals(CommandName.CMDENTER.toString())) {
                isSessionOpened.set(true);
              }
              break;

            case CMDMSG:
            case CMDPRVMSG:
              getView().onReceiveMessage(chatCommand.getMessage());
              break;

            case CMDUSRLST:
              // Update userList
              getView().onUpdateChatUserList(chatCommand.getPayload().split(" "));
              break;

            default:
              // TODO save unknown commands to log file
              getView().showWarningWindow(chatCommand.toString(), "Unknown command");
          }

        }
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
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
