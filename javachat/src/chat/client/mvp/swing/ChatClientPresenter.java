package chat.client.mvp.swing;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import javax.swing.SwingWorker;
import chat.server.ChatHandler;
import chat.server.ChatServer;

/**
 * The Class ChatClientPresenter. Realize chat client logic.
 */
public class ChatClientPresenter implements Presenter {

  /** The Constant _GREETING_MESSAGE. */
  private final static String MSG_GREETING = "Enter username to start сhat: ";

  private final static String MSG_CANT_CON_SRV = "Can't connect to server " + ChatServer.SERVER_IP
      + ":" + ChatServer.SERVER_PORT + ". Server not started.";

  private View view;

  /** The sever socket. */
  private Socket serverSocket = null;

  /** The client thread. */
  private ProcessServerMessages clientThread = null;

  /** The out stream. */
  private PrintWriter outStream = null;

  /** The in stream. */
  private BufferedReader inStream = null;



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

      // System.out.println(ioe.getMessage());
      // emptyPanelLabel.setText(MSG_CANT_CON_SRV);
      return res;

    }

    // launch new thread with SwingWorker class to safe access swing GUI outside Event Dispatch
    // Thread

    clientThread = new ProcessServerMessages();
    clientThread.execute();

    // TODO generate /enter command

    // send to server /enter command
    sendEnterCMD(username, outStream);

    return res;


  }


  private void sendEnterCMD(String username, PrintWriter outStream) {
    outStream.println(ChatHandler.CMD_ENTER + " " + username);
    // outStream.flush();
  }


  /**
   * Print greeting message to enter field.
   */
  private void printGreetingMSG() {
    getView().clearChatPane();
    getView().showChatMessage(MSG_GREETING);
  }


  /**
   * The Class ProcessServerMessages.
   */
  class ProcessServerMessages extends SwingWorker<Void, String> {

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Void doInBackground() {

      String res = "";
      try {
        while ((res = inStream.readLine()) != null) {
          publish(res);
        }
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
      return null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process(List<String> chunks) {
      for (String message : chunks) {

        // getView().showChatMessage(message);
        getView().showChatMessage(message);


      }
    }

  }



  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.swing.Presenter#closeConnection()
   */
  @Override
  public void closeConnection() {
    // TODO Auto-generated method stub

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
   * @see chat.client.mvp.swing.Presenter#sendMsg()
   */
  @Override
  public void sendMsg(String message) {
    // TODO Auto-generated method stub
    if (serverSocket != null && serverSocket.isConnected()) {

      // send message to chat
      outStream.println(message);
      getView().clearEnterTextField();
      //enterTextField.setText("");

    } else { // start new connection

      // get user name
      String username = getView().getEnterTextField();
      username = username.substring(username.lastIndexOf(":") + 1);

      // try to connect
      // if (connectToChatServer(username)) {
      if (openConnection(username)) {

        getView().showChatMessage(username);
       } else {
        printGreetingMSG();
      }
    }
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
  public void setView(ChatClientView view) {
    this.view = view;

  }

  private View getView() {
    if (view == null) {
      throw new IllegalStateException("The view is not set.");
    } else {
      return this.view;
    }
  }

}
