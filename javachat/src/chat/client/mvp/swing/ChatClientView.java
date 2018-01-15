package chat.client.mvp.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

//
/**
 * The Class ChatClientView. Realize swing GUI view with chat client logic.
 */
public class ChatClientView extends JFrame implements View {

  // Constant

  private static final long serialVersionUID = -2989309737312155966L;

  private static final String DEFAULT_WINDOW_NAME = "Java Swing Chat Client";

  // Class variables

  private Presenter presenter;

  private Action enterKeyListenerAction;

  // GUI variables

  /** The enter text field. */
  private JTextField chatTextField;

  /** The text pane chat. */
  private JTextPane chatPanelChat;

  /** The user list. */
  private JList<?> chatUserList;

  // Constructor

  /**
   * Initialize GUI components.
   */
  public ChatClientView() {

    initActions();
    initComponents();

  }

  private void initActions() {

    enterKeyListenerAction = new AbstractAction() {

      /**
       * 
       */
      private static final long serialVersionUID = 2285084326177903354L;

      @Override
      public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        getPresenter().sendMsg(chatTextField.getText());
      }
    };

  }

  private void initComponents() {
    // TODO Auto-generated method stub

    // frame = new JFrame();
    setBounds(100, 100, 500, 400);
    setTitle(DEFAULT_WINDOW_NAME);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout(0, 0));

    JSplitPane splitPane = new JSplitPane();
    getContentPane().add(splitPane);

    chatUserList = new JList();
    splitPane.setLeftComponent(chatUserList);
    splitPane.getLeftComponent().setMinimumSize(new Dimension(125, 0));


    JPanel panel = new JPanel();
    splitPane.setRightComponent(panel);
    panel.setLayout(new BorderLayout(0, 0));

    chatTextField = new JTextField();
    // enterTextField.addKeyListener(new enterFieldKeyListener());
    chatTextField.addActionListener(enterKeyListenerAction);
    chatTextField.setToolTipText("Type text and press Enter button");
    panel.add(chatTextField, BorderLayout.SOUTH);
    chatTextField.setColumns(10);

    JPanel panel_1 = new JPanel();
    panel.add(panel_1, BorderLayout.CENTER);
    panel_1.setLayout(new BorderLayout(0, 0));

    chatPanelChat = new JTextPane();
    panel_1.add(chatPanelChat, BorderLayout.SOUTH);

  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;

  }

  private Presenter getPresenter() {
    if (presenter == null) {
      throw new IllegalStateException("The presenter is not set");
    } else {
      return presenter;
    }
  }

  @Override
  public void showMessageWindow(Object message, String title, int messageType) {
    // TODO Auto-generated method stub
    JOptionPane.showMessageDialog(this, message, title, messageType);
  }


  // TODO where we must catch exceptions, in view or in presenter?
  @Override
  public void showChatMessage(String message) {

    StyledDocument doc = chatPanelChat.getStyledDocument();
    try {
      doc.insertString(doc.getLength(), message + "\n", null);
    } catch (BadLocationException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

  }

  @Override
  public void showUserList() {
    // TODO Auto-generated method stub
    //chatUserList.setT
  }
  
  
  void clearChatUserList() {
    DefaultListModel listModel = (DefaultListModel) chatUserList.getModel();
    listModel.removeAllElements();
  }



  @Override
  public void clearChatPane() {
    // TODO Auto-generated method stub
    chatPanelChat.setText("");
  }



  @Override
  public void clearEnterTextField() {
    // TODO Auto-generated method stub
    chatTextField.setText("");
  }



  @Override
  public String getEnterTextField() {
    return chatTextField.getText();
  }



}
