package chat.client.mvp.presenter;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import chat.base.ChatSession;
import chat.base.Command;
import chat.base.CommandName;
import chat.base.Constants;
import chat.base.User;

public class ClientChatSession extends ChatSession {

  private Presenter presenter = null;
  protected final static int MAX_TIMEOUT_SESSION_OPEN_MS = 100;
  private static final int MAX_TIMEOUT_OUT_STREAM_OPEN_MS = 100;

  public ClientChatSession(Presenter presenter) {
    super(Constants.THREAD_NAME_CLIENT);
    this.presenter = presenter;
  }

  @Override
  public void processCommand(Command command) {

    super.processCommand(command);

    switch (command.getCommandName()) {

      case CMDERR:
        // logger.debug("run() - {}", //$NON-NLS-1$
        // "ClientPresenter.ProcessCommandThread.run()" + Command.getMessage()); //$NON-NLS-1$
        presenter.getView().showErrorWindow(command.getMessage(), "Error");
        break;

      case CMDEXIT:
        presenter.closeConnection();
        break;

      case CMDHLP: // TODO complete break;

      case CMDOK:
        if (command.getPayload().equals(CommandName.CMDENTER.toString())) {
          isSessionOpenedFlag.set(true);
        }
        break;

      case CMDMSG:
      case CMDPRVMSG:
        presenter.getView().onReceiveMessage(command.getMessage());
        break;

      case CMDUSRLST: // Update userList
        presenter.getView().onUpdateChatUserList(command.getPayload().split(" "));
        break;

      default:
        presenter.getView().showWarningWindow(command.toString(),
            Constants.WRN_UNKNOWN_COMMAND_MSG);
        // logger.warn("ProcessCommandThread.run() {}",
        // Constants.WRN_UNKNOWN_COMMAND_MSG + " " + Command);
    }

  }

  @Override
  public void openSession(String userName) {
    // MDC.put("username", username);

    super.openSession(userName);


    Socket clientSocket = null;

    presenter.getView().onConnectionOpening(Constants.DEFAULT_WINDOW_NAME);

    try {
      clientSocket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // commandHandler = new ClientCommandHandler(clientSocket, username, this);
    // commandHandler = new CommandHandler_new(clientSocket, this);
    // commandHandler.start(THREAD_NAME_CLN);

    runCommandHandler(clientSocket);

    this.user = new User(userName);

    int waitingForOutputStreamOpenningTimeoutMiliseconds = 0;
    while (!commandHandler.getIsOutputStreamOpened()
        && (waitingForOutputStreamOpenningTimeoutMiliseconds <= MAX_TIMEOUT_OUT_STREAM_OPEN_MS)) {
      try {
        TimeUnit.MILLISECONDS.sleep(1);
      } catch (InterruptedException e) {
        // logger.error("openSession(String)", e); //$NON-NLS-1$

        e.printStackTrace();
      }
      waitingForOutputStreamOpenningTimeoutMiliseconds++;
    }

    System.out.println("waitingForOutputStreamOpenningTimeoutMiliseconds "
        + waitingForOutputStreamOpenningTimeoutMiliseconds);

    if (commandHandler.getIsOutputStreamOpened()) {
      // new Command(CommandName.CMDENTER, "", user.getUsername())
      // .send(commandHandler.outputStream);
      sendCommand(new Command(CommandName.CMDENTER, "", user.getUsername()));
    } else {

      // TODO refactor ti complain next else
      // MDC.clear();

      presenter.closeConnection(true);
      String msg = "Can't connect to the server, timeout " + MAX_TIMEOUT_SESSION_OPEN_MS
          + ". Check server, try again or increase open session timeout.";
      System.out.println(msg);
      presenter.getView().showErrorWindow(msg, "Open session timeout.");

      return;

    }

    int waitingForOKEnterTimeoutMiliseconds = 0;
    while (!getIsSessionOpenedFlag()
        && (waitingForOKEnterTimeoutMiliseconds <= MAX_TIMEOUT_SESSION_OPEN_MS)) {
      try {
        TimeUnit.MILLISECONDS.sleep(1);
      } catch (InterruptedException e) {
        // logger.error("openConnection(String)", e); //$NON-NLS-1$

        e.printStackTrace();
      }
      waitingForOKEnterTimeoutMiliseconds++;
    }

    System.out
        .println("waitingForOKEnterTimeoutMiliseconds " + waitingForOKEnterTimeoutMiliseconds);

    if (getIsSessionOpenedFlag()) { // we receive ok enter command

      // do that we must do in View on session open
      presenter.getView().onConnectionOpened(userName);

    } else {
      // TODO closeConnection here
      // MDC.clear();

      presenter.closeConnection(true);
      String msg = "Can't connect to the server, timeout " + MAX_TIMEOUT_SESSION_OPEN_MS
          + ". Check server, try again or increase open session timeout.";
      System.out.println(msg);
      presenter.getView().showErrorWindow(msg, "Open session timeout.");

    }

  }

  @Override
  public void closeSession(boolean sendEXTCMD) {
    // MDC.clear();

    presenter.getView().onConnectionClosing(Constants.DEFAULT_WINDOW_NAME);

    System.out.println("Closing client...");

    System.out.println("Send exit command and close connection");

    // stop message handler thread BEFORE closing server socket and associated
    // streams

    super.closeSession(sendEXTCMD);

    System.out.println("Client stopped.");
    presenter.getView().onConnectionClosed(Constants.DEFAULT_WINDOW_NAME);

  }

}
