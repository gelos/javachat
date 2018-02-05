package chat.client.mvp.swing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
import javax.swing.SwingWorker;
import chat.base.WorkerThreadClass;
import chat.server.ChatHandler;
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

  private ViewSwing viewSwing;

  /** The sever socket. */
  private Socket serverSocket = null;

  // private MessageHandler messageHandler = null;
  private MessageHandler messageHandler = null;

  /** The out stream. */
  private PrintWriter outStream = null;

  /** The in stream. */
  private BufferedReader inStream = null;


  @Override
  public void sendChatMsg() {
    // TODO Auto-generated method stub

    String message = getViewSwing().getEnterTextField();
    getViewSwing().clearEnterTextField();

    if (serverSocket != null && serverSocket.isConnected()) {

      // send message to chat
      outStream.println(message);

      // enterTextField.setText("");

    } else { // start new connection

      // get user name
      String username = message;
      if (!username.equals("")) {

        if (openConnection(username)) {

          getViewSwing().showMsgChatPane(username);
        } else {
          getViewSwing().showMsgChatPane(MSG_CANT_CON_SRV);
        }
      } else {
        getViewSwing().showMsgChatPane(MSG_EMPTY_USRENAME);
        getViewSwing().showMsgChatPane(MSG_ASK_FOR_USERNAME);
      }
    }
  }


  /**
   * Connect to chat server. Open socket, prepare input/output streams, create new thread to data
   * transfer.
   *
   * @param username the user name
   */
  @Override
  public boolean openConnection(String username) {
    // TODO Auto-generated method stub

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
      // emptyPanelLabel.setText(MSG_CANT_CON_SRV);
      return res;

    }

    // launch new thread with SwingWorker class to safe access swing GUI outside Event Dispatch
    // Thread

    // getViewSwing().showMsgChatPane("wegfw");

    messageHandler = new MessageHandler();
    messageHandler.start();

    // getViewSwing().showMsgChatPane("wegfw");

    // clientThread = new ProcessServerMessages();
    // clientThread.execute();

    // TODO generate /enter command

    // send to server /enter command
    sendEnterCMD(username, outStream);

    // System.out.println(getViewSwing().getEnterTextField() + " openConnection");

    return res;


  }


  /**
   * Print greeting message to enter field.
   */
  @Override
  public void showGreetingMsg() {
    getViewSwing().clearChatPane();
    getViewSwing().showMsgChatPane(MSG_ASK_FOR_USERNAME);
  }


  class MessageHandler extends WorkerThreadClass {

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

          getViewSwing().showMsgChatPane(message);
        }
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
    }

  }

  
  // TODO complete that
  private void handleUsrLstCMD() {
    
  }

  public void stop() {
    // TODO Auto-generated method stub
    System.out.println("Closing client...");
    
    System.out.println("Send exit command");
    sendExitCMD(outStream);
    
    System.out.println("Stopping message handler thread, closing ServerSocket");
    closeConnection();
    
    System.out.println("Client stopped.");
  }

  private void sendExitCMD(PrintWriter outStream) {
    // TODO Auto-generated method stub
    outStream.println(ChatHandler.CMD_EXIT);
  }


  private void sendEnterCMD(String username, PrintWriter outStream) {
    outStream.println(ChatHandler.CMD_ENTER + " " + username);
    // outStream.flush();
  }


  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.swing.Presenter#updateUserList()
   */
  @Override
  public void updateUserList() {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.swing.Presenter#sendPrvMsg()
   */
  @Override
  public void sendPrvMsg() {
    // TODO Auto-generated method stub

  }



  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.swing.Presenter#printMsg()
   */
  @Override
  public void printMsg() {
    // TODO Auto-generated method stub

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


  @Override
  public void closeConnection() {

    // stop message handler thread
    if ((messageHandler != null) && (messageHandler.isRuning())) {
      messageHandler.stop();
    }

    // try to close serversocket

    if (serverSocket != null && serverSocket.isConnected()) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

}
