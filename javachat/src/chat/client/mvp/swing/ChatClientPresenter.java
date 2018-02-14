package chat.client.mvp.swing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import chat.base.ChatCommand;
import chat.base.ChatUtils;
import chat.base.CommandName;
import chat.base.WorkerThread;
import chat.server.ChatServer;

/**
 * The Class ChatClientPresenter. Realize chat client logic.
 */
public class ChatClientPresenter implements Presenter {

  /** The Constant _GREETING_MESSAGE. */
  private final static String MSG_ASK_FOR_USERNAME = "Enter username to start сhat: ";

  private final static String MSG_EMPTY_USRENAME = "Username cannot be empty.";

  private final static String MSG_CANT_CON_SRV = "Can't connect to server " + ChatServer.SERVER_IP
      + ":" + ChatServer.SERVER_PORT + ". Server not started.";

  private final static int TIMEOUT_SESSION_OPEN = 3;

  private ViewSwing viewSwing;

  /** The sever socket. */
  private Socket serverSocket = null;

  // private MessageHandler messageHandler = null;
  private MessageHandler messageHandler = null;

  /** The out stream. */
  private PrintWriter outStream = null;

  /** The in stream. */
  private BufferedReader inStream = null;

  private AtomicBoolean isSessionOpen;

  public ChatClientPresenter() {

    isSessionOpen = new AtomicBoolean(false);
  }

  /**
   * Connect to chat server. Open socket, prepare input/output streams, create new thread to data
   * transfer.
   *
   * @param username the user name
   */
  @Override
  public boolean openConnection(String username) {

    isSessionOpen.set(false);

    boolean res = false;
    try {

      // try to open server connection
      serverSocket = new Socket(ChatServer.SERVER_IP, ChatServer.SERVER_PORT);

      // format streams
      outStream = new PrintWriter(serverSocket.getOutputStream(), true);
      inStream = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

    } catch (UnknownHostException uhe) {

      System.out.println(uhe.getMessage());
      return res;

    } catch (IOException ioe) {

      System.out.println(ioe.getMessage());
      return res;

    }

    // start message handler
    messageHandler = new MessageHandler();
    messageHandler.start();

    // send to server enter command
    ChatUtils.sendCommand(new ChatCommand(CommandName.CMDENTER, username), outStream);

    int timeout = 1;
    // wait for ok enter command from server
    while (!isSessionOpen.get() && (timeout <= TIMEOUT_SESSION_OPEN)) {
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      timeout++;
    }

    if (isSessionOpen.get()) { // we receive ok enter command

      // do that we must do in View on session open
      getViewSwing().onSessionOpen();

      res = true;

    } else {

      // stop message handler
      messageHandler.stop();
      String msg = "Can't connect to the server, timeout " + TIMEOUT_SESSION_OPEN
          + ". Check server, try again or increase open session timeout.";
      System.out.println(msg);
      getViewSwing().showErrorWindow(msg, "Open session timeout.");
      getViewSwing().onSessionClose();
    }

    return res;

  }


  @Override
  public void closeConnection() {

    // stop message handler thread
    if ((messageHandler != null) && (messageHandler.isRuning())) {
      messageHandler.stop();
    }

    // try to close serversocket

    if (serverSocket != null && serverSocket.isConnected()) {

      // send to server exit command
      ChatUtils.sendCommand(new ChatCommand(CommandName.CMDEXIT, ""), outStream);

      try {
        serverSocket.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  @Override
  public void sendMsg(String message) {
    ChatUtils.sendCommand(new ChatCommand(CommandName.CMDMSG, message), outStream);
  }

  @Override
  public void sendPrvMsg(String message, String userList) {
    ChatUtils.sendCommand(
        new ChatCommand(CommandName.CMDPRVMSG, message + CommandName.CMDDLM + userList), outStream);

  }

  class MessageHandler extends WorkerThread {

    @Override
    public void run() {

      // TODO Auto-generated method stub
      String message = "";
      // getViewSwing().showMsgChatPane(message);
      try {
        while (this.isRuning()) {

          message = inStream.readLine();
          if (message == null) {
            break;
          }
          System.out.println(message);
          ChatCommand cmd = ChatUtils.parseMessage(message);

          switch (cmd.getCommand()) {
            case CMDOK:
              if (cmd.getPayload().equals(CommandName.CMDENTER.toString())) {
                isSessionOpen.set(true);
              }
              break;

            case CMDUSRLST:
              // Update userList
              getViewSwing().clearChatUserList();
              getViewSwing().updateChatUserList(cmd.getPayload().split(" "));;
              break;

            case CMDEXIT:
              // TODO change it

              break;
            case CMDPRVMSG:
            case CMDMSG:
              System.out.println("ChatClientPresenter.MessageHandler.run()");
              getViewSwing().showMsgChatPane(cmd.getPayload());
              break;

            default:
              cmd = new ChatCommand(CommandName.CMDERR, "Unknow command " + message);
          }

        }
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
    }

  }

  /**
   * Print greeting message to enter field.
   */
  @Override
  public void showGreetingMsg() {
    getViewSwing().clearChatPane();
    getViewSwing().showMsgChatPane(MSG_ASK_FOR_USERNAME);
  }

  public void stop() {
    // TODO Auto-generated method stub
    System.out.println("Closing client...");

    System.out.println("Send exit command");
    ChatUtils.sendCommand(new ChatCommand(CommandName.CMDEXIT, ""), outStream);;

    System.out.println("Stopping message handler thread, closing ServerSocket");
    closeConnection();

    System.out.println("Client stopped.");
  }

  @Override
  public void setView(ChatClientSwingView view) {
    this.viewSwing = view;

  }

  public ViewSwing getViewSwing() {
    if (viewSwing == null) {
      throw new IllegalStateException("The viewSwing is not set.");
    } else {
      return this.viewSwing;
    }
  }


}
