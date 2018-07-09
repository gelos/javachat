package chat.client.mvp.presenter;

import static chat.base.CommandName.CMDENTER;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import chat.base.ChatSession;
import chat.base.Command;
import chat.base.CommandHandler_new;
import chat.base.CommandName;
import chat.base.Constants;
import chat.base.User;
import chat.client.mvp.view.View;
import chat.server.Server;

public class ClientPresenter extends ChatSession implements Presenter {

  /**
   * Logger for this class
   */
  protected static final Logger logger = LoggerFactory.getLogger(ClientPresenter.class);
  // private static final Logger loggerDebug = LoggerFactory.getLogger("debug");
  private static final Logger loggerDebugMDC = LoggerFactory.getLogger("debug.MDC");

  protected final static int MAX_TIMEOUT_SESSION_OPEN_MS = 100;
  private static final int MAX_TIMEOUT_OUT_STREAM_OPEN_MS = 100;

  private static final String THREAD_NAME_CLN = "client-";
  
  private CommandHandler_new clientCommandHandler = null;
  private View view;
  public Socket clientSocket = null;


  /*
   * public ClientPresenter() { super(); }
   */
  @Override
  public void openConnection(String username) {
    MDC.put("username", username);

    getView().onConnectionOpening(Constants.DEFAULT_WINDOW_NAME);

    try {
      clientSocket = new Socket(Server.SERVER_IP, Server.SERVER_PORT);
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // clientCommandHandler = new ClientCommandHandler(clientSocket, username, this);
    clientCommandHandler = new CommandHandler_new(clientSocket, this);
    clientCommandHandler.start(THREAD_NAME_CLN);

    
    this.user = new User(username);

    System.out.println("pre client socket " + clientSocket.isConnected() + clientCommandHandler.outputStream);
    
/*    try {
      Thread.sleep(1000);
    } catch (InterruptedException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }*/
    
    int waitingForOutputStreamOpenningTimeoutMiliseconds = 0;
    while (!clientCommandHandler.getIsOutputStreamOpened() 
        && (waitingForOutputStreamOpenningTimeoutMiliseconds <= MAX_TIMEOUT_OUT_STREAM_OPEN_MS)) {
      try {
        TimeUnit.MILLISECONDS.sleep(1);
      } catch (InterruptedException e) {
        logger.error("openConnection(String)", e); //$NON-NLS-1$

        e.printStackTrace();
      }
      waitingForOutputStreamOpenningTimeoutMiliseconds++;
    }
    
    System.out.println("waitingForOutputStreamOpenningTimeoutMiliseconds " + waitingForOutputStreamOpenningTimeoutMiliseconds);
    
    if (clientCommandHandler.getIsOutputStreamOpened()) {
      new Command(CMDENTER, "", user.getUsername()).send(clientCommandHandler.outputStream);  
    } else {
      
      // TODO refactor ti complain next else
      MDC.clear();

      closeConnection();
      String msg = "Can't connect to the server, timeout " + MAX_TIMEOUT_SESSION_OPEN_MS
          + ". Check server, try again or increase open session timeout.";
      System.out.println(msg);
      getView().showErrorWindow(msg, "Open session timeout.");
      
      return;
      
    }
    
    

    System.out.println("client socket " + clientSocket.isConnected() + clientCommandHandler.outputStream);
    
    int waitingForOKEnterTimeoutMiliseconds = 0;
    while (!getIsSessionOpenedFlag()
        && (waitingForOKEnterTimeoutMiliseconds <= MAX_TIMEOUT_SESSION_OPEN_MS)) {
      try {
        TimeUnit.MILLISECONDS.sleep(1);
      } catch (InterruptedException e) {
        logger.error("openConnection(String)", e); //$NON-NLS-1$

        e.printStackTrace();
      }
      waitingForOKEnterTimeoutMiliseconds++;
    }

    System.out.println("waitingForOKEnterTimeoutMiliseconds " + waitingForOKEnterTimeoutMiliseconds);
    
    if (getIsSessionOpenedFlag()) { // we receive ok enter command

      // do that we must do in View on session open
      getView().onConnectionOpened(username);

    } else {
      // TODO closeConnection here
      MDC.clear();

      closeConnection();
      String msg = "Can't connect to the server, timeout " + MAX_TIMEOUT_SESSION_OPEN_MS
          + ". Check server, try again or increase open session timeout.";
      System.out.println(msg);
      getView().showErrorWindow(msg, "Open session timeout.");

    }

  }

  @Override
  public void closeConnection() {

    MDC.clear();

    getView().onConnectionClosing(Constants.DEFAULT_WINDOW_NAME);

    System.out.println("Closing client...");

    System.out.println("Send exit command and close connection");

    // stop message handler thread BEFORE closing server socket and associated
    // streams
    if ((clientCommandHandler != null) && (clientCommandHandler.isRunning())) {
      clientCommandHandler.stop();
      try {
        clientCommandHandler.getThread().join();
        // TODO check that we join all thread before stop!!!
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    System.out.println("Client stopped.");
    getView().onConnectionClosed(Constants.DEFAULT_WINDOW_NAME);


  }

  @Override
  public void sendCommand(String commandString) {
    if ((clientCommandHandler != null) && (clientCommandHandler.isRunning())) {
      Command Command = new Command(commandString);
      switch (Command.getCommandName()) {
        case CMDEXIT:
          // TODO duplicate send exit command
          closeConnection();
          onViewStart();
          break;
        case CMDPRVMSG:
        case CMDENTER:
        case CMDMSG:
          Command.send(clientCommandHandler.outputStream);
          // loggerDebug.debug("sendCommand(String) - getView().onSendMessage(), getView:
          // " + getView().hashCode()); //$NON-NLS-1$
          getView().onSendMessage();
          break;
        case CMDERR:
          getView().showErrorWindow(
              "Wrong format or command \"" + Command.getMessage() + "\" not supported.", "Error");
          getView().onSendMessage();
          break;
        default:
          new Command(CommandName.CMDMSG, commandString).send(clientCommandHandler.outputStream);
          getView().onSendMessage();
          break;
      }

    }
  }

  /*
   * @Override public void sendPrivateMessage(String message, String userList) { // TODO
   * Auto-generated method stub
   * 
   * }
   */

  @Override
  public void setView(View view) {
    this.view = view;

  }

  @Override
  public View getView() {
    if (view == null) {
      throw new IllegalStateException("The view is not set.");
    } else {
      return this.view;
    }
  }

  @Override
  public void onViewStart() {
    getView().onConnectionOpening(Constants.DEFAULT_WINDOW_NAME);
    String[] emptyUserList = new String[0];
    getView().onUpdateChatUserList(emptyUserList);
    getView().onReceiveMessage(Constants.MSG_ASK_FOR_USERNAME);
  }

  @Override
  public void processCommand(Command Command) {

    switch (Command.getCommandName()) {

      case CMDERR:
        logger.debug("run() - {}", //$NON-NLS-1$
            "ClientPresenter.ProcessCommandThread.run()" + Command.getMessage()); //$NON-NLS-1$
        getView().showErrorWindow(Command.getMessage(), "Error");
        break;

      case CMDEXIT:
        closeConnection();
        break;

      case CMDHLP: // TODO complete break;

      case CMDOK:
        if (Command.getPayload().equals(CommandName.CMDENTER.toString())) {
          isSessionOpenedFlag.set(true);
        }
        break;

      case CMDMSG:
      case CMDPRVMSG:
        getView().onReceiveMessage(Command.getMessage());
        break;

      case CMDUSRLST: // Update userList
        getView().onUpdateChatUserList(Command.getPayload().split(" "));
        break;

      default:
        getView().showWarningWindow(Command.toString(), Constants.WRN_UNKNOWN_COMMAND_MSG);
        logger.warn("ProcessCommandThread.run() {}",
            Constants.WRN_UNKNOWN_COMMAND_MSG + " " + Command);
    }



  }

}
