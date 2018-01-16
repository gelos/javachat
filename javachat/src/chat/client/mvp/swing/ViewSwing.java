package chat.client.mvp.swing;

/**
 * The Interface ViewSwing.
 */
public interface ViewSwing {

  /**
   * Sets the presenter.
   *
   * @param presenterSwing the new presenter
   */
  void setPresenterSwing(PresenterSwing presenterSwing);

  /**
   * Show user list.
   */

  void showMessageWindow(Object message, String title, int messageType);

  void showMsgOnChatPane(String message);

  void clearChatPane();

  String getEnterTextField();
  
  void clearEnterTextField();

  void showUserList(String[] usrList);

  void clearChatUserList();
}
