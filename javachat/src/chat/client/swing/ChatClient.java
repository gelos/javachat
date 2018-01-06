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
import javax.swing.JButton;
import javax.swing.AbstractListModel;
import java.awt.GridBagLayout;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import chat.server.ChatServer;

// TODO: Auto-generated Javadoc

// TODO test swing app with JUnit http://www.javacreed.com/testing-swing-application/
// TODO use MVC pattern http://www.javacreed.com/testing-swing-application/
/**
 * The Class ChatClient.
 */
public class ChatClient {

  /** The Constant _GREETING_MESSAGE. */
  private final static String _GREETING_MESSAGE = "Enter username to start сhat: ";

  /** The chat socket. */
  Socket serverSocket = null;

  ProcessServerMessages clientThread;

  /** The send. */
  PrintWriter send;

  /** The rec. */
  BufferedReader rec;

  /** The frame. */
  private JFrame frame;

  /** The cards. */
  private CardLayout cards;

  /** The card panel. */
  private JPanel cardPanel;

  /** The enter text field. */
  private JTextField enterTextField;

  /** The user. */
  private ChatUser user;

  /** The text pane chat. */
  private JTextPane textPaneChat;

  /** The user list. */
  private JList userList;

  /**
   * The user list model.
   *
   * @param args the arguments
   */
  // private ListModel userListModel;

  /**
   * Launch the application.
   *
   * @param args the arguments
   * @wbp.parser.entryPoint
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          ChatClient window = new ChatClient();
          window.frame.setVisible(true);
          window.askForUserName();

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the application.
   */
  
    public ChatClient() { initialize(); }
   

  /**
   * Connect to chat server.
   *
   * @param username the username
   */
  private void connectToChatServer(String username) {

    // try to open server connection
    try {
      serverSocket = new Socket(ChatServer._SERVER_HOST, ChatServer._SERVER_PORT);
      send = new PrintWriter(serverSocket.getOutputStream(), true);
      rec = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

    } catch (UnknownHostException uhe) {
      System.out.println(uhe.getMessage());
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    }

    clientThread = new ProcessServerMessages();
    clientThread.execute();

    // clientThread = new Thread(this, "whatever");
    // clientThread.start();

    // do {
    // user = new ChatUser(username);



    // TODO generate /enter command


    cards.next(cardPanel);
    // } while ((user.getUsername() == "") || !checkUserNameDuplicate(user.getUsername()))
  }

  /**
   * Initialize the contents of the frame.
   * @wbp.parser.entryPoint
   */
  private void initialize() {
    frame = new JFrame();
    frame.setBounds(100, 100, 500, 400);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout(0, 0));

    JSplitPane splitPane = new JSplitPane();
    frame.getContentPane().add(splitPane);

    userList = new JList();
    splitPane.setLeftComponent(userList);
    splitPane.getLeftComponent().setMinimumSize(new Dimension(125, 0));

    // userList.setModel(new ListModel<String>(new String [] {"one","two"}));

    // userListModel = userList.getModel();

    JPanel panel = new JPanel();
    splitPane.setRightComponent(panel);
    panel.setLayout(new BorderLayout(0, 0));

    enterTextField = new JTextField();
    enterTextField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (e.getSource() == enterTextField) {
          if (key == KeyEvent.VK_ENTER) {

            // send message to chat
            if (alreadyConnected()) {

            } else { // start new connection
              String username = enterTextField.getText();
              username = username.substring(username.lastIndexOf(":") + 1);
              connectToChatServer(username);
              StyledDocument doc = textPaneChat.getStyledDocument();
              try {
                doc.insertString(doc.getLength(), username + "\n", null);
              } catch (BadLocationException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }
            }
          }
        }
      }
    });


    enterTextField.setToolTipText("Type text and press Enter button");
    panel.add(enterTextField, BorderLayout.SOUTH);
    enterTextField.setColumns(10);

    cardPanel = new JPanel();
    panel.add(cardPanel, BorderLayout.CENTER);
    cards = new CardLayout(0, 0);
    cardPanel.setLayout(cards);

    JPanel cardPanelEmpty = new JPanel(new BorderLayout());
    cardPanel.add(cardPanelEmpty, "name_39379659049175");

    JPanel cardPanelChatText = new JPanel();
    cardPanelChatText.setLayout(new BorderLayout(0, 0));
    // cardPanel.add(chatTextArea, "name_39697288842864");
    cardPanel.add(cardPanelChatText, "name_39697288842864");

    textPaneChat = new JTextPane();
    cardPanelChatText.add(textPaneChat, BorderLayout.SOUTH);

  }

  /**
   * Ask for user name.
   */
  private void askForUserName() {
    enterTextField.setText(_GREETING_MESSAGE);
  }

  /**
   * Already connected.
   *
   * @return true, if successful
   */
  private boolean alreadyConnected() {
    return (serverSocket != null);
  }


  class ProcessServerMessages extends SwingWorker<Void, String> {

    @Override
    public Void doInBackground() {

      String res = "";
      try {
        while ((res = rec.readLine()) != null) {
          publish(res);
        }
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
      return null;

    }

    protected void process(List<String> chunks) {
      for (String msg : chunks) {

        StyledDocument doc = textPaneChat.getStyledDocument();
        try {
          doc.insertString(doc.getLength(), msg + "\n", null);
        } catch (BadLocationException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

      }
    }

  }

  /*
   * @Override public void run() { String temp = ""; try { while (((temp = rec.readLine()) != null)
   * && (clientThread != null)) { //ta.append(temp + "\n");
   * 
   * StyledDocument doc = textPaneChat.getStyledDocument(); try { doc.insertString(doc.getLength(),
   * "username" + "\n", null); } catch (BadLocationException e1) { // TODO Auto-generated catch
   * block e1.printStackTrace(); }
   * 
   * } } catch (IOException ioe) { System.out.println(ioe.getMessage()); }
   * 
   * }
   */

}
