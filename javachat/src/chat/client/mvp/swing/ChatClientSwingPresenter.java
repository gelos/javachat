package chat.client.mvp.swing;

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
 * The Class ChatClientSwingPresenter. Realize chat client logic.
 */
public class ChatClientSwingPresenter implements PresenterSwing {

  /** The Constant _GREETING_MESSAGE. */
  private final static String MSG_ASK_FOR_USERNAME = "Enter username to start сhat: ";

  private final static String MSG_EMPTY_USRENAME = "Username cannot be empty.";

  private final static String MSG_CANT_CON_SRV = "Can't connect to server " + ChatServer.SERVER_IP
      + ":" + ChatServer.SERVER_PORT + ". Server not started.";

  private ViewSwing viewSwing;

  /** The sever socket. */
  private Socket serverSocket = null;

  /** The client thread. */
  private ProcessServerMessages clientThread = null;

 // private MessageHandler messageHandler = null;
  private Thread messageHandler = null;
  
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
    
    //getViewSwing().showMsgChatPane("wegfw");
    
    messageHandler = new Thread (new MessageHandler(), "messageHandlerThread");
    messageHandler.start();
    
    //getViewSwing().showMsgChatPane("wegfw");
    
    //clientThread = new ProcessServerMessages();
    //clientThread.execute();

    // TODO generate /enter command
    
    // send to server /enter command
    sendEnterCMD(username, outStream);

    //System.out.println(getViewSwing().getEnterTextField() + " openConnection");
    
    return res;


  }


  private void sendEnterCMD(String username, PrintWriter outStream) {
    outStream.println(ChatHandler.CMD_ENTER + " " + username);
    // outStream.flush();
  }


  /**
   * Print greeting message to enter field.
   */
  @Override
  public void showGreetingMsg() {
    getViewSwing().clearChatPane();
    getViewSwing().showMsgChatPane(MSG_ASK_FOR_USERNAME);
  }

  
  class MessageHandler implements Runnable {

    @Override
    public void run() {
      // TODO Auto-generated method stub
      String message = "";
      //getViewSwing().showMsgChatPane(message);
      try {
        while ((message = inStream.readLine()) != null) {
          //getViewSwing().showMsgChatPane("");
          System.out.println(message);
          getViewSwing().showMsgChatPane(message);
        }
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
    }
    
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

      System.out.println(getViewSwing().getEnterTextField() + " doInBackground");
      
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

        System.out.println(message);
        System.out.println(getViewSwing().getEnterTextField() + " in process");
        
        // getView().showChatMessage(message);
        getViewSwing().showMsgChatPane(message);


      }
    }

  }



  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.swing.PresenterSwing#closeConnection()
   */
  @Override
  public void closeConnection() {
    // TODO Auto-generated method stub
    System.out.println("Closing client");
    System.out.println("Closing client");
  }

  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.swing.PresenterSwing#updateUserList()
   */
  @Override
  public void updateUserList() {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.swing.PresenterSwing#sendPrvMsg()
   */
  @Override
  public void sendPrvMsg() {
    // TODO Auto-generated method stub

  }



  /*
   * (non-Javadoc)
   * 
   * @see chat.client.mvp.swing.PresenterSwing#printMsg()
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

}
