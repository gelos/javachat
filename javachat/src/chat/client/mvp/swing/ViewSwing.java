package chat.client.mvp.swing;

/**
 * The Interface ViewSwing.
 */
public interface ViewSwing {

  void setPresenter(Presenter presenter);
  void showMessageWindow(Object message, String title);
  void showInformationWindow(Object message, String title);
  void showWarningWindow(Object message, String title);
  void showErrorWindow(Object message, String title);

  public void onSendMsg();
  public void onReceiveMsg(String message);
    
  // Chat text pane
  void showMsgOnChatPane(String message);
  void clearChatPane();
  
  // Message text field
  String getEnterTextField(); 
  void clearEnterTextField();

  // Chat user list 
  void updateChatUserList(String[] usrList);
  void clearChatUserList();
  
  void onConnectionOpening(String title);
  void onSessionClosing(String title);
  
  void onConnectionOpened(String title);
  void onConnectionClosed(String title); 
  
}
