package chat.client.mvp.presenter;

import static chat.base.CommandName.CMDENTER;
import static chat.base.CommandName.CMDMSG;
import static chat.base.CommandName.CMDUSRLST;
import static chat.base.Constants.MSG_ACCPT_CLIENT;
import static chat.base.Constants.MSG_CLOSE_CONNECTION;
import static chat.base.Constants.MSG_EXIT_USR;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import org.slf4j.MDC;
import chat.base.ChatSession;
import chat.base.Command;
import chat.base.CommandHandler;
import chat.base.CommandName;
import chat.base.Constants;
import chat.base.User;

public class ClientCommandHandler extends CommandHandler {

  private Presenter presenter;

  public ClientCommandHandler(Socket clientSocket, String username, Presenter presenter) {
    super(clientSocket);
    this.user = new User(username);
    this.presenter = presenter;
  }

  @Override
  public void run() {

    try {
      openOutputStream();

      new Command(CMDENTER, "", user.getUsername()).send(outputStream);
      openInputStream();

      String ip = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress())
          .toString().replace("/", "");
      CommandHandler.logger.info("run() - {}", MSG_ACCPT_CLIENT + ip); //$NON-NLS-1$
      System.out.println(MSG_ACCPT_CLIENT + ip);

      // Reading commands from the current client input socket while the handler is
      // running
      while (isRunning()) {

        Command command = (Command) inputStream.readObject();
        if (isRunning()) {
          loggerDebugMDC.debug(command.toString());
          processCommand(command);
        }

      }

      /*
       * while (isRunning()) {
       * 
       * Command Command = (Command) inputStream.readObject(); processCommand(Command);
       * 
       * }
       */

      // } catch (SocketException | EOFException e) {
    } catch (SocketException e) {
      // TODO: handle exception
      System.out.println("normal close");
    }
    catch (EOFException e) {
      // TODO: handle exception
      System.out.println("server close connection ???");
    }
    catch (IOException | ClassNotFoundException e) {
      // TODO Auto-generated catch block
      System.out.println("ClientCommandHandler.run() catch " + e.getMessage());
      // System.out
      // .println("ClientCommandHandler.run() clientSocket.isClosed() " + clientSocket.isClosed());
      e.printStackTrace();
    } finally {
      MDC.clear();
      user = null;

    }

  }

  /* (non-Javadoc)
   * @see chat.client.mvp.presenter.BusinessLogic#processCommand(chat.base.Command)
   */
  @Override
  public void processCommand(Command Command) {
    switch (Command.getCommandName()) {

      case CMDERR:
        logger.debug("run() - {}", //$NON-NLS-1$
            "ClientPresenter.ProcessCommandThread.run()" + Command.getMessage()); //$NON-NLS-1$
        presenter.getView().showErrorWindow(Command.getMessage(), "Error");
        break;

      case CMDEXIT:
        presenter.closeConnection();
        break;

      case CMDHLP:
        // TODO complete
        break;

      case CMDOK:
        if (Command.getPayload().equals(CommandName.CMDENTER.toString())) {
          isChatSessionOpenedFlag.set(true);
        }
        break;

      case CMDMSG:
      case CMDPRVMSG:
        presenter.getView().onReceiveMessage(Command.getMessage());
        break;

      case CMDUSRLST:
        // Update userList
        presenter.getView().onUpdateChatUserList(Command.getPayload().split(" "));
        break;

      default:
        presenter.getView().showWarningWindow(Command.toString(),
            Constants.WRN_UNKNOWN_COMMAND_MSG);
        logger.warn("ProcessCommandThread.run() {}",
            Constants.WRN_UNKNOWN_COMMAND_MSG + " " + Command);
    }

  }


  @Override
  public void stop() {

    if (clientSocket != null && clientSocket.isConnected()) {
      // send to server exit command
      System.out.println("ClientCommandHandler.run() send Exit cmd");
      new Command(CommandName.CMDEXIT, "").send(outputStream);
    }

    // Set the flag isRunning to false and close client socket, this will allow us to exit the while
    // loop in the run method.
    super.stop();

    // System.out
    // .println("ClientCommandHandler.stop() clientSocket.isClosed() " + clientSocket.isClosed());
   // System.out.println("ClientCommandHandler.stop() - stop thread");

  }

  public void sendCommand(String commandString) {

    Command Command = new Command(commandString);
    switch (Command.getCommandName()) {
      case CMDEXIT:
        // TODO duplicate send exit command
        presenter.closeConnection();
        presenter.onViewStart();
        break;
      case CMDPRVMSG:
      case CMDENTER:
      case CMDMSG:
        Command.send(outputStream);
        // loggerDebug.debug("sendCommand(String) - getView().onSendMessage(), getView:
        // " + getView().hashCode()); //$NON-NLS-1$
        presenter.getView().onSendMessage();
        break;
      case CMDERR:
        presenter.getView().showErrorWindow(
            "Wrong format or command \"" + Command.getMessage() + "\" not supported.", "Error");
        presenter.getView().onSendMessage();
        break;
      default:
        new Command(CommandName.CMDMSG, commandString).send(outputStream);
        presenter.getView().onSendMessage();
        break;
    }

  }



}
