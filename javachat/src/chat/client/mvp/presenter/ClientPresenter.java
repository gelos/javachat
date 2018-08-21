package chat.client.mvp.presenter;

import chat.base.Session;
import chat.base.Command;
import chat.base.CommandName;
import chat.base.Constants;
import chat.client.mvp.view.View;

/**
 * The Class ClientPresenter. Implements {@link Presenter}. Used in MVP model.
 */
public class ClientPresenter implements Presenter {

  /** The view. */
  private View view;

  /** The chat session. */
  private Session session;

  /**
   * Instantiates a new client presenter.
   */
  public ClientPresenter() {
    session = new ClientSession(this);
  }

  /**
   * Implementing {@link Presenter#openConnection(String)}.
   * 
   * @see chat.client.mvp.presenter.Presenter#openConnection(java.lang.String)
   */
  @Override
  public void openConnection(String username) {

    session.open(username);

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
        boolean sendEXTCMD = true;
        closeConnection(sendEXTCMD);
        onViewStart();
        break;

      case CMDPRVMSG:
      case CMDENTER:
      case CMDMSG:
        session.send(command);
        getView().onSendMessage();
        break;

      case CMDERR:
        getView().showErrorWindow(Constants.MSG_WRONG_FORMAT_OR_COMMAND_1 + command.getMessage()
            + Constants.MSG_WRONG_FORMAT_OR_COMMAND_2, "");
        getView().onSendMessage();
        break;

      default:
        session.send(new Command(CommandName.CMDMSG, commandString));
        getView().onSendMessage();
        break;
    }
  }


  /**
   * Implementing {@link Presenter#closeConnection()}.
   * 
   * @see chat.client.mvp.presenter.Presenter#closeConnection()
   */
  @Override
  public void closeConnection() {
    closeConnection(true);
  }


  /**
   * Implementing {@link Presenter#closeConnection(boolean)}.
   * 
   * @see chat.client.mvp.presenter.Presenter#closeConnection(boolean)
   */
  @Override
  public void closeConnection(boolean sendEXTCMD) {

    session.close(sendEXTCMD);

  }

  /**
   * Implementing {@link Presenter#setView(View)}.
   * 
   * @see chat.client.mvp.presenter.Presenter#setView(chat.client.mvp.view.View)
   */
  @Override
  public void setView(View view) {
    this.view = view;

  }

  /**
   * Implementing {@link Presenter#getView()}.
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

  /**
   * Implementing {@link Presenter#onViewStart()}.
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

}
