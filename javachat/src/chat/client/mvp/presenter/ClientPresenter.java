package chat.client.mvp.presenter;

import chat.base.ChatSession;
import chat.base.Command;
import chat.base.CommandName;
import chat.base.Constants;
import chat.client.mvp.view.View;

/**
 * The Class ClientPresenter.
 */
public class ClientPresenter implements Presenter {

  /** Logger for this class. */
  // protected static final Logger logger = LoggerFactory.getLogger(ClientPresenter.class);

  /** The Constant loggerDebugMDC. */
  // private static final Logger loggerDebug = LoggerFactory.getLogger("debug");
  // private static final Logger loggerDebugMDC = LoggerFactory.getLogger("debug.MDC");

  /** The view. */
  private View view;

  /** The chat session. */
  private ChatSession chatSession;

  /**
   * Instantiates a new client presenter.
   */
  public ClientPresenter() {
    chatSession = new ClientChatSession(this);
  }


  /**
   * Implementing {@link Presenter#openConnection(String)}.
   * 
   * @see chat.client.mvp.presenter.Presenter#openConnection(java.lang.String)
   */
  @Override
  public void openConnection(String username) {

    chatSession.open(username);

  }

  /**
   * Implementing {@link Presenter#closeConnection(boolean)}.
   * 
   * @see chat.client.mvp.presenter.Presenter#closeConnection(boolean)
   */
  @Override
  public void closeConnection(boolean sendEXTCMD) {

    chatSession.close(sendEXTCMD);

  }

  /**
   * Implementing {@link Presenter#sendCommand(String)}. Get the input string, parsing it to the
   * {@link Command} object, and send it.
   * 
   * @see chat.client.mvp.presenter.Presenter#sendCommand(java.lang.String)
   */
  @Override
  public void sendCommand(String commandString) {

    // Parsing sting to command object 
    Command command = new Command(commandString);
    
    switch (command.getCommandName()) {
      case CMDEXIT:
        // TODO duplicate send exit command
        boolean sendEXTCMD = true;
        closeConnection(sendEXTCMD);
        onViewStart();
        break;
      case CMDPRVMSG:
      case CMDENTER:
      case CMDMSG:
        // command.send(commandHandler.outputStream);
        chatSession.sendCommand(command);
        // loggerDebug.debug("sendCommand(String) - getView().onSendMessage(), getView:
        // " + getView().hashCode()); //$NON-NLS-1$
        getView().onSendMessage();
        break;
      case CMDERR:
        getView().showErrorWindow(
            "Wrong format or command \"" + command.getMessage() + "\" not supported.", "Error");
        getView().onSendMessage();
        break;
      default:
        // new Command(CommandName.CMDMSG, commandString).send(commandHandler.outputStream);
        chatSession.sendCommand(new Command(CommandName.CMDMSG, commandString));
        getView().onSendMessage();
        break;
    }

    // }
  }

  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.presenter.Presenter#setView(chat.client.mvp.view.View)
   */
  @Override
  public void setView(View view) {
    this.view = view;

  }

  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.presenter.Presenter#getView()
   */
  @Override
  public View getView() {
    if (view == null) {
      throw new IllegalStateException("The view is not set.");
    } else {
      return this.view;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.presenter.Presenter#onViewStart()
   */
  @Override
  public void onViewStart() {
    getView().onConnectionOpening(Constants.DEFAULT_WINDOW_NAME);
    String[] emptyUserList = new String[0];
    getView().onUpdateChatUserList(emptyUserList);
    getView().onReceiveMessage(Constants.MSG_ASK_FOR_USERNAME);
  }



  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.presenter.Presenter#closeConnection()
   */
  @Override
  public void closeConnection() {
    // TODO Auto-generated method stub
    closeConnection(true);
  }

}
