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

/**
 * The Class ChatClientSwingView. Realize swing GUI view with chat client logic.
 */
public class ChatClientSwingView extends JFrame implements ViewSwing {

  // Constant

  private static final long serialVersionUID = -2989309737312155966L;

  // Class variables

  private Presenter presenter;

  private Action enterKeyListenerAction;

  private Action openConnectionListenerAction;

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
        getPresenter().sendMsg(chatTextField.getText());
      }
    };

    openConnectionListenerAction = new AbstractAction() {

      /**
       * 
       */
      private static final long serialVersionUID = -2510888198208290119L;

      @Override
      public void actionPerformed(ActionEvent e) {
        getPresenter().openConnection(chatTextField.getText());
      }
    };

  }

  private void initComponents() {

    setBounds(100, 100, 500, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout(0, 0));

    JSplitPane splitPane = new JSplitPane();
    getContentPane().add(splitPane);

    JPanel panel_left = new JPanel();
    splitPane.setLeftComponent(panel_left);
    panel_left.setLayout(new BorderLayout(0, 0));

    chatUserList = new JList<String>();
    chatUserList.setFocusable(false);

    JScrollPane scrollChatUserList = new JScrollPane(chatUserList);
    panel_left.add(scrollChatUserList);
    splitPane.getLeftComponent().setPreferredSize(new Dimension(100, 0));
    splitPane.getLeftComponent().setMinimumSize(new Dimension(50, 0));

    JPanel panel_right = new JPanel();
    splitPane.setRightComponent(panel_right);
    panel_right.setLayout(new BorderLayout(0, 0));

    chatTextField = new JTextField();
    chatTextField.setToolTipText("Type text and press Enter button");
    panel_right.add(chatTextField, BorderLayout.SOUTH);
    chatTextField.setColumns(10);

    JPanel chatPanel = new JPanel();
    JScrollPane scrollChatPane = new JScrollPane(chatPanel);
    panel_right.add(scrollChatPane, BorderLayout.CENTER);
    chatPanel.setLayout(new BorderLayout(0, 0));

    chatTextPane = new JTextPane();
    chatTextPane.setFocusable(false);
    chatTextPane.setEditable(false);
    chatPanel.add(chatTextPane, BorderLayout.SOUTH);

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

    // System.out.println("ChatClientSwingView.showMsgChatPane(" + message + ")");

    StyledDocument doc = chatTextPane.getStyledDocument();
    try {
      if (doc.getLength() == 0) {

        doc.insertString(doc.getLength(), message, null);
      } else {
        doc.insertString(doc.getLength(), "\n" + message, null);
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
    DefaultListModel model = new DefaultListModel();
    model.clear();
    chatUserList.setModel(model);
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
  public void onConnectionOpened(String title) {
    chatTextPane.setText("");
    chatTextField.setText("");
    chatTextField.removeActionListener(openConnectionListenerAction);
    chatTextField.removeActionListener(enterKeyListenerAction);
    chatTextField.addActionListener(enterKeyListenerAction);
    setTitle(title);
  }

  @Override
  public void onConnectionClosed(String title) {
    // TODO Auto-generated method stub
    chatTextField.removeActionListener(enterKeyListenerAction);
    chatTextField.removeActionListener(openConnectionListenerAction);
    chatTextField.addActionListener(openConnectionListenerAction);
    // chatTextField.setEditable(true);
    setTitle(title);
  }

  @Override
  public void onConnectionOpening(String title) {
    chatTextPane.setText("");
    chatTextField.setText("");
    // chatTextField.setEditable(true);

    chatTextField.removeActionListener(enterKeyListenerAction);
    chatTextField.removeActionListener(openConnectionListenerAction);
    chatTextField.addActionListener(openConnectionListenerAction);

    setTitle(title);

  }

  @Override
  public void onSessionClosing(String title) {
    // TODO Auto-generated method stub
  }



}
