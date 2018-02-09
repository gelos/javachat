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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

//
/**
 * The Class ChatClientSwingView. Realize swing GUI view with chat client logic.
 */
public class ChatClientSwingView extends JFrame implements ViewSwing {

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
  private JTextPane chatTextPane;

  /** The user list. */
  private JList<?> chatUserList;

  // Constructor

  /**
   * Initialize GUI components.
   */
  public ChatClientSwingView() {

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
        //getPresenter().sendChatMsgToServer(chatTextField.getText());
        getPresenter().sendChatMsg();
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
    chatUserList.setFocusable(false);
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
     
    JPanel chatPanel = new JPanel();
    JScrollPane scrollChatPane = new JScrollPane(chatPanel);
    //panel.add(panel_1, BorderLayout.CENTER);
    panel.add(scrollChatPane, BorderLayout.CENTER);
    chatPanel.setLayout(new BorderLayout(0, 0));

    chatTextPane = new JTextPane();
    chatTextPane.setFocusable(false);
    chatTextPane.setEditable(false);
    //JScrollPane jsp = new JScrollPane(chatPanelChat);
    chatPanel.add(chatTextPane, BorderLayout.SOUTH);
    //panel_1.add(jsp, BorderLayout.SOUTH);

    chatTextField.requestFocusInWindow();
    
  }

  public Presenter getPresenter() {
    if (presenter == null) {
      throw new IllegalStateException("The presenter is not set");
    } else {
      return presenter;
    }
  }


  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;

  }

  // TODO where we must catch exceptions, in view or in presenter?
  @Override
  public void showMsgChatPane(String message) {

    StyledDocument doc = chatTextPane.getStyledDocument();
    try {
      if (doc.getLength() == 0) {
        
        doc.insertString(doc.getLength(), message, null);
      } else {
        doc.insertString(doc.getLength(), "\n" + message , null);
     }
    } catch (BadLocationException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

  }


  @Override
  public void clearChatPane() {
    // TODO Auto-generated method stub
    chatTextPane.setText("");
  }

  @Override
  public void updateChatUserList(String[] usrList) {
    // TODO Auto-generated method stub
    // chatUserList.setT
    DefaultListModel<String> listModel = (DefaultListModel<String>) chatUserList.getModel();
    for (String username : usrList) {
      listModel.addElement(username);
    }
    // chatUserList.setModel(listModel);
  }

  @Override
  public void clearChatUserList() {
    DefaultListModel listModel = (DefaultListModel) chatUserList.getModel();
    listModel.removeAllElements();
  }


  @Override
  public String getEnterTextField() {
    return chatTextField.getText();
  }


  @Override
  public void clearEnterTextField() {
    // TODO Auto-generated method stub
    chatTextField.setText("");
  }

  @Override
  public void showMessageWindow(Object message, String title) {
    // TODO Auto-generated method stub
    JOptionPane.showMessageDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
  }

  @Override
  public void showInformationWindow(Object message, String title) {
    // TODO Auto-generated method stub
    JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public void showWarningWindow(Object message, String title) {
    // TODO Auto-generated method stub
    JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
  }

  @Override
  public void showErrorWindow(Object message, String title) {
    // TODO Auto-generated method stub
    JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void onSessionOpen() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onSessionClose() {
    // TODO Auto-generated method stub
    
  }



}
