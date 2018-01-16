package chat.client.swing;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JList;
import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import chat.server.ChatHandler;
import chat.server.ChatServer;
import javax.swing.JLabel;

// TODO test swing app with JUnit http://www.javacreed.com/testing-swing-application/
// TODO use MVC pattern http://www.javacreed.com/testing-swing-application/
/**
 * The Class ChatClientSwingView. Realize swing GUI with chat client logic.
 */
public class ChatClient {

  // Constant

  /** The Constant _GREETING_MESSAGE. */
  private final static String MSG_GREETING = "Enter username to start сhat: ";

  private final static String MSG_CANT_CON_SRV = "Can't connect to server " + ChatServer.SERVER_IP
      + ":" + ChatServer.SERVER_PORT + ". Server not started.";

  // Class variables

  /** The sever socket. */
  private Socket serverSocket = null;

  /** The client thread. */
  private ProcessServerMessages clientThread = null;

  /** The out stream. */
  private PrintWriter outStream = null;

  /** The in stream. */
  private BufferedReader inStream = null;

  // GUI variables

  /** The frame. */
  private JFrame frame;

  /** The cards. */
  private CardLayout cards;

  /** The card panel. */
  private JPanel cardPanel;

  /** The enter text field. */
  private JTextField enterTextField;

  /** The text pane chat. */
  private JTextPane textPanelChat;

  /** The user list. */
  private JList<?> userList;
  private JLabel emptyPanelLabel;


  /**
   * The main method. Run GUI in Event Dispatch Thread.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          ChatClient window = new ChatClient();
          window.frame.setVisible(true);
          window.printGreetingMSG();

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  // Constructor

  /**
   * Initialize GUI components.
   */
  public ChatClient() {

    frame = new JFrame();
    frame.setBounds(100, 100, 500, 400);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout(0, 0));

    JSplitPane splitPane = new JSplitPane();
    frame.getContentPane().add(splitPane);

    userList = new JList();
    splitPane.setLeftComponent(userList);
    splitPane.getLeftComponent().setMinimumSize(new Dimension(125, 0));


    JPanel panel = new JPanel();
    splitPane.setRightComponent(panel);
    panel.setLayout(new BorderLayout(0, 0));

    enterTextField = new JTextField();
    enterTextField.addKeyListener(new enterFieldKeyListener());
    enterTextField.setToolTipText("Type text and press Enter button");
    panel.add(enterTextField, BorderLayout.SOUTH);
    enterTextField.setColumns(10);

    cardPanel = new JPanel();
    panel.add(cardPanel, BorderLayout.CENTER);
    cards = new CardLayout(0, 0);
    cardPanel.setLayout(cards);

    JPanel cardPanelEmpty = new JPanel(new BorderLayout());
    cardPanel.add(cardPanelEmpty, "name_39379659049175");

    emptyPanelLabel = new JLabel("New label");
    cardPanelEmpty.add(emptyPanelLabel, BorderLayout.SOUTH);

    JPanel cardPanelChatText = new JPanel();
    cardPanelChatText.setLayout(new BorderLayout(0, 0));
    cardPanel.add(cardPanelChatText, "name_39697288842864");

    textPanelChat = new JTextPane();
    cardPanelChatText.add(textPanelChat, BorderLayout.SOUTH);

  }

  /**
   * The listener interface for receiving enterFieldKey events. The class that is interested in
   * processing a enterFieldKey event implements this interface, and the object created with that
   * class is registered with a component using the component's <code>addenterFieldKeyListener<code>
   * method. When the enterFieldKey event occurs, that object's appropriate method is invoked.
   *
   * @see enterFieldKeyEvent
   */
  private class enterFieldKeyListener extends KeyAdapter {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
      int key = e.getKeyCode();
      if (e.getSource() == enterTextField) {
        if (key == KeyEvent.VK_ENTER) {

          // clear error msg text
          emptyPanelLabel.setText("");
          
          // check if serverSocket already opened
          if (serverSocket != null && serverSocket.isConnected()) {

            // send message to chat
            outStream.println(enterTextField.getText());
            enterTextField.setText("");

          } else { // start new connection

            // get user name
            String username = enterTextField.getText();
            username = username.substring(username.lastIndexOf(":") + 1);

            // try to connect
            if (connectToChatServer(username)) {

              StyledDocument doc = textPanelChat.getStyledDocument();
              try {
                doc.insertString(doc.getLength(), username + "\n", null);
              } catch (BadLocationException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }
            } else {
              printGreetingMSG();
            }
          }
        }
      }
    }
  }


  /**
   * Connect to chat server. Open socket, prepare input/output streams, create new thread to data
   * transfer.
   *
   * @param username the user name
   */
  private boolean connectToChatServer(String username) {

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

      //System.out.println(ioe.getMessage());
      emptyPanelLabel.setText(MSG_CANT_CON_SRV);
      return res;

    }

    // launch new thread with SwingWorker class to safe access swing GUI outside Event Dispatch
    // Thread

    clientThread = new ProcessServerMessages();
    clientThread.execute();

    // TODO generate /enter command

    // send to server /enter command
    sendEnterCMD(username, outStream);
    cards.next(cardPanel);

    return res;

  }

  private void sendEnterCMD(String username, PrintWriter outStream) {
    outStream.println(ChatHandler.CMD_ENTER + " " + username);
    //outStream.flush();
  }

  /**
   * Print greeting message to enter field.
   */
  private void printGreetingMSG() {
    enterTextField.setText(MSG_GREETING);
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
      for (String msg : chunks) {

        StyledDocument doc = textPanelChat.getStyledDocument();
        try {
          doc.insertString(doc.getLength(), msg + "\n", null);
        } catch (BadLocationException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

      }
    }

  }



}
