package chat.base;

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

/**
 * The Class ChatClientPresenter. Realize chat client logic.
 */
public class ChatClientPresenter implements Presenter {

  /** The Constant _GREETING_MESSAGE. */
  private final static String MSG_ASK_FOR_USERNAME = "Enter username to start сhat: ";

  /** The Constant MSG_EMPTY_USRENAME. */
  private final static String MSG_EMPTY_USRENAME = "Username cannot be empty.";

  /** The Constant MSG_CANT_CON_SRV. */
  private final static String MSG_CANT_CON_SRV = "Can't connect to server " + ChatServer.SERVER_IP
      + ":" + ChatServer.SERVER_PORT + ". Server not started.";

  /** The Constant DEFAULT_WINDOW_NAME. */
  private static final String DEFAULT_WINDOW_NAME = "Java Swing Chat Client";

  /** The Constant TIMEOUT_SESSION_OPEN. */
  private final static int TIMEOUT_SESSION_OPEN = 3;

  /** The view. */
  private View view;

  /** The sever socket. */
  private Socket serverSocket = null;

  /** The message handler. */
  // private CommandHandler commandHandler = null;
  private CommandHandler commandHandler = null;

  /** The out stream. */
  private ObjectOutputStream outputStream = null;

  /** The in stream. */
  private ObjectInputStream inputStream = null;

  /** The is session opened flag. */
  private AtomicBoolean isSessionOpened;

  /**
   * Instantiates a new chat client presenter.
   */
  public ChatClientPresenter() {
    isSessionOpened = new AtomicBoolean(false);
  }

  /**
   * Refresh View to start new session.
   */
  public void onViewStart() {
    getView().onConnectionOpening(DEFAULT_WINDOW_NAME); // prepare new connection
    getView().onUpdateChatUserList(new String[0]); // clear user list
    getView().onReceiveMessage(MSG_ASK_FOR_USERNAME); // ask for username
  }
  

  /**
   * Connect to chat server. Open socket, prepare input/output streams, create new thread to data
   * transfer.
   *
   * @param username the user name
   */
  @Override
  public void openConnection(String username) {

    getView().onConnectionOpening(DEFAULT_WINDOW_NAME);
    isSessionOpened.set(false);

    // boolean res = false;
    try {

      // try to open server connection
      serverSocket = new Socket(ChatServer.SERVER_IP, ChatServer.SERVER_PORT);

      outputStream =
          new ObjectOutputStream(new BufferedOutputStream(serverSocket.getOutputStream()));

    } catch (UnknownHostException uhe) {

      System.out.println(uhe.getMessage());
      // return res;

    } catch (IOException ioe) {

      System.out.println(ioe.getMessage());
      // return res;

    }

    // send to server enter command
    try {

      new ChatCommand(CommandName.CMDENTER, "", username).send(outputStream);

      inputStream = new ObjectInputStream(new BufferedInputStream(serverSocket.getInputStream()));

      // start message handler
      commandHandler = new CommandHandler();
      commandHandler.start();

    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    int timeout = 1;
    // wait for ok enter command from server
    while (!isSessionOpened.get() && (timeout <= TIMEOUT_SESSION_OPEN)) {
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      timeout++;
    }

    if (isSessionOpened.get()) { // we receive ok enter command

      // do that we must do in View on session open
      getView().onConnectionOpened(username);

      // res = true;

    } else {
      getView().onConnectionClosed(DEFAULT_WINDOW_NAME);
      // stop message handler
      commandHandler.stop();
      String msg = "Can't connect to the server, timeout " + TIMEOUT_SESSION_OPEN
          + ". Check server, try again or increase open session timeout.";
      System.out.println(msg);
      getView().showErrorWindow(msg, "Open session timeout.");
     
    }

    // return res;

  }


  /**
   * @see chat.client.mvp.swing.Presenter#closeConnection()
   */
  @Override
  public void closeConnection() {

    getView().onConnectionClosing(DEFAULT_WINDOW_NAME);
    
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
    getView().onConnectionClosed(DEFAULT_WINDOW_NAME);
  }

  /**
   * @see chat.client.mvp.swing.Presenter#sendMessage(java.lang.String)
   */
  @Override
  public void sendMessage(String message) {
    ChatCommand command = new ChatCommand(message);
    switch (command.getCommandName()) {
      case CMDEXIT:
        closeConnection();
        onViewStart();
        break;
      case CMDENTER:
      case CMDMSG:
        command.send(outputStream);
        getView().onSendMessage();
        break;
      default:
        getView().showWarningWindow(command.getMessage(), "Command not supported");
        break;
    }
  }

  /**
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
  
    /* (non-Javadoc)
     * @see chat.base.WorkerThread#run()
     */
    @Override
    public void run() {
  
      // TODO Auto-generated method stub
      ChatCommand chatCommand;
      try {
  
        while ((chatCommand = (ChatCommand) inputStream.readObject()) != null && isRuning()) {
  
          System.out.println("ChatClientPresenter.CommandHandler.run()" + chatCommand);
  
          switch (chatCommand.getCommandName()) {
  
            case CMDERR:
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

  /* (non-Javadoc)
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
  private View getView() {
    if (view == null) {
      throw new IllegalStateException("The view is not set.");
    } else {
      return this.view;
    }
  }

}
