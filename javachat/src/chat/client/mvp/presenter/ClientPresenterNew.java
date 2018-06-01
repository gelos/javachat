package chat.client.mvp.presenter;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import chat.base.Constants;
import chat.client.mvp.view.View;
import chat.server.Server;

public class ClientPresenterNew implements Presenter {

  /**
   * Logger for this class
   */
  protected static final Logger logger = LoggerFactory.getLogger(ClientPresenter.class);
  // private static final Logger loggerDebug = LoggerFactory.getLogger("debug");
  private static final Logger loggerDebugMDC = LoggerFactory.getLogger("debug.MDC");

  protected final static int MAX_TIMEOUT_SESSION_OPEN_MS = 100;

  
  private static final String THREAD_NAME_CLN = "client-";
  private ClientCommandHandler clientCommandHandler = null;
  private View view;
  public Socket clientSocket = null;


  public ClientPresenterNew() {
    super();
  }

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

    clientCommandHandler = new ClientCommandHandler(clientSocket, username, this);
    clientCommandHandler.start(THREAD_NAME_CLN);

    int waitingForOKEnterTimeoutMiliseconds = 0;
    while (!clientCommandHandler.getIsChatSessionOpened()
        && (waitingForOKEnterTimeoutMiliseconds <= MAX_TIMEOUT_SESSION_OPEN_MS)) {
      try {
        TimeUnit.MILLISECONDS.sleep(1);
      } catch (InterruptedException e) {
        logger.error("openConnection(String)", e); //$NON-NLS-1$

        e.printStackTrace();
      }
      waitingForOKEnterTimeoutMiliseconds++;
    }

    if (clientCommandHandler.getIsChatSessionOpened()) { // we receive ok enter command

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
      clientCommandHandler.sendCommand(commandString);

    }
  }

 /* @Override
  public void sendPrivateMessage(String message, String userList) {
    // TODO Auto-generated method stub

  }*/

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

}
