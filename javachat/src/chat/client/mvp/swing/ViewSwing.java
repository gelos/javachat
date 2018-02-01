package chat.client.mvp.swing;

/**
 * The Interface ViewSwing.
 */
public interface ViewSwing {

  void setPresenterSwing(PresenterSwing presenterSwing);
  void showMessageWindow(Object message, String title, int messageType);

  // Chat text pane
  void showMsgChatPane(String message);
  void clearChatPane();
  
  // Message text field
  String getEnterTextField(); 
  void clearEnterTextField();

  // Chat user list 
  void updateChatUserList(String[] usrList);
  void clearChatUserList();
}
