package chat.client.mvp.presenter;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import chat.base.ChatSession;
import chat.base.Command;
import chat.base.CommandName;
import chat.base.Constants;
import chat.base.User;

/**
 * The Class ClientChatSession. Chat session on client side. Extends {@link ChatSession}.
 */
public class ClientChatSession extends ChatSession {

  /** The MVC presenter. */
  private Presenter presenter = null;

  /**
   * Instantiates a new client chat session.
   *
   * @param presenter the presenter
   */
  public ClientChatSession(Presenter presenter) {

    // Create ChatSession with client tag in ComandHandler thread name
    super(Constants.THREAD_NAME_CLIENT);
    this.presenter = presenter;

  }

  /**
   * Client implementation of {@link ChatSession#open(String)} method.
   *
   * @see chat.base.ChatSession#open(String)
   */
  @Override
  public void open(String userName) {

    Socket clientSocket = null;

    super.open(userName);

    presenter.getView().onConnectionOpening(Constants.DEFAULT_WINDOW_NAME);

    try {
      clientSocket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
    } catch (IOException e) {
      logger.error(this.getClass().getSimpleName() + "." + "openSession(String)", e); //$NON-NLS-1$
    }

    runCommandHandler(clientSocket);

    this.user = new User(userName);

    // Waiting until the output stream opened
    int waitingForOutputStreamOpenningTimeoutMiliseconds = 0;
    while (!commandHandler.getIsOutputStreamOpened()
        && (waitingForOutputStreamOpenningTimeoutMiliseconds <= Constants.MAX_TIMEOUT_OUT_STREAM_OPEN_MS)) {
      try {

        TimeUnit.MILLISECONDS.sleep(1);

      } catch (InterruptedException e) {

        logger.error(this.getClass().getSimpleName() + "." + "openSession(String)", e); //$NON-NLS-1$

      }
      waitingForOutputStreamOpenningTimeoutMiliseconds++;
    }

    if (commandHandler.getIsOutputStreamOpened()) {

      // Initiate session by sending the ENTER command
      send(new Command(CommandName.CMDENTER, "", user.getUsername()));

    } else {

      boolean sendEXTCMD = false;
      presenter.closeConnection(sendEXTCMD);

      String msg = Constants.MSG_TIMEOUT_OUTPUT_STREAM + Constants.MAX_TIMEOUT_SESSION_OPEN_MS
          + Constants.MSG_CAN_T_CONNECT_TO_THE_SERVER_TIMEOUT_2;
      logger.error(this.getClass().getSimpleName() + "." + " openSession(String)", msg);
      presenter.getView().showErrorWindow(msg, "");

      return;

    }

    // Waiting for the OK command from server
    int waitingForOKEnterTimeoutMiliseconds = 0;
    while (!getIsSessionOpenedFlag()
        && (waitingForOKEnterTimeoutMiliseconds <= Constants.MAX_TIMEOUT_SESSION_OPEN_MS)) {
      try {

        TimeUnit.MILLISECONDS.sleep(1);

      } catch (InterruptedException e) {

        logger.error(this.getClass().getSimpleName() + "." + "openSession(String)", e); //$NON-NLS-1$

      }
      waitingForOKEnterTimeoutMiliseconds++;
    }

    if (getIsSessionOpenedFlag()) { // We receive OK command

      // Do that we must do in View on session open
      presenter.getView().onConnectionOpened(userName);

    } else {

      boolean sendEXTCMD = true;
      presenter.closeConnection(sendEXTCMD);

      String msg = Constants.MSG_TIMEOUT_OK_CMD + Constants.MAX_TIMEOUT_SESSION_OPEN_MS
          + Constants.MSG_CAN_T_CONNECT_TO_THE_SERVER_TIMEOUT_2;
      logger.error(this.getClass().getSimpleName() + "." + " openSession(String)", msg);
      presenter.getView().showErrorWindow(msg, "");

    }

  }

  /**
   * Client implementation of {@link ChatSession#receive(Command)} method.
   *
   * @see chat.base.ChatSession#receive(chat.base.Command)
   */
  @Override
  public void receive(Command command) {

    super.receive(command);

    // Ignoring all commands except OK with payload ENTER while session not open
    if (!getIsSessionOpenedFlag() && command.getCommandName() != CommandName.CMDOK
        && !command.getPayload().equals(CommandName.CMDENTER.toString())) {
      loggerDebugMDC.debug(this.getClass().getSimpleName() + "." + "processCommand(Command) {}", //$NON-NLS-1$
          Constants.MSG_SESSION_NOT_OPENED_BUT_COMMAND_RECEIVED + " " + command);
      return;
    }

    switch (command.getCommandName()) {

      case CMDERR:
        presenter.getView().showErrorWindow(command.getMessage(), "");
        logger.error(this.getClass().getSimpleName() + "." + "processCommand(Command)",
            command.getMessage());
        break;

      case CMDEXIT:
        presenter.closeConnection();
        break;

      case CMDHLP:
        // TODO complete
        presenter.getView().showInformationWindow(
            Constants.MSG_COMMAND_NOT_IMPLEMENTED_YET + " " + CommandName.CMDHLP, "");
        break;

      case CMDOK:

        // If received OK command with ENTER payload then set open session flag to true
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
        loggerDebugMDC.debug(this.getClass().getSimpleName() + "." + "processCommand() {}",
            Constants.WRN_UNKNOWN_COMMAND_MSG + " " + command);
    }

  }

  /**
   * Client implementation of {@link ChatSession#close(boolean)} method.
   * 
   * @see chat.base.ChatSession#close(boolean)
   */
  @Override
  public void close(boolean sendEXTCMD) {

    presenter.getView().onConnectionClosing(Constants.DEFAULT_WINDOW_NAME);

    loggerDebugMDC.debug(this.getClass().getSimpleName() + "." + "closeSession() {}",
        Constants.MSG_CLOSING_CLIENT);
    System.out.println(Constants.MSG_CLOSING_CLIENT);

    super.close(sendEXTCMD);

    logger.debug(this.getClass().getSimpleName() + "." + "closeSession() {}",
        Constants.MSG_CLIENT_STOPPED);
    System.out.println(Constants.MSG_CLIENT_STOPPED);

    presenter.getView().onConnectionClosed(Constants.DEFAULT_WINDOW_NAME);

  }

}
