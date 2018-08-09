package chat.client.mvp.presenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import chat.base.ChatSession;
import chat.base.Command;
import chat.base.CommandName;
import chat.base.Constants;
import chat.client.mvp.view.View;

public class ClientPresenter implements Presenter {

  /**
   * Logger for this class
   */
  protected static final Logger logger = LoggerFactory.getLogger(ClientPresenter.class);
  // private static final Logger loggerDebug = LoggerFactory.getLogger("debug");
  private static final Logger loggerDebugMDC = LoggerFactory.getLogger("debug.MDC");

  private static final String THREAD_NAME_CLN = "client-";

  // private CommandHandler_new commandHandler = null;
  private View view;
  // TODO refactor as local var
  // public Socket clientSocket = null;

  private ChatSession chatSession;

  public ClientPresenter() {
    chatSession = new ClientChatSession(this);
  }



  @Override
  public void openConnection(String username) {

    chatSession.openSession(username);
  }

  @Override
  public void closeConnection(boolean sendEXTCMD) {

       
    chatSession.closeSession(sendEXTCMD);


  }

  @Override
  public void sendCommand(String commandString) {
    //if ((commandHandler != null) && (commandHandler.isRunning())) {
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
          //command.send(commandHandler.outputStream);
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
          //new Command(CommandName.CMDMSG, commandString).send(commandHandler.outputStream);
          chatSession.sendCommand(new Command(CommandName.CMDMSG, commandString));
          getView().onSendMessage();
          break;
      }

    //}
  }

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
  public void closeConnection() {
    // TODO Auto-generated method stub
    closeConnection(true);
  }

}
