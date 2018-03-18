package chat.base;

/**
 * MVP passive View Interface.
 */
public interface View {

  /**
   * On connection opening.
   *
   * @param title the title
   */
  void onConnectionOpening(String title);

  /**
   * On connection opened.
   *
   * @param title the title
   */
  void onConnectionOpened(String title);

  /**
   * On connection closing.
   *
   * @param title the title
   */
  void onConnectionClosing(String title);

  /**
   * On connection closed.
   *
   * @param title the title
   */
  void onConnectionClosed(String title);

  /**
   * On update chat user list.
   *
   * @param userList the user list
   */
  void onUpdateChatUserList(String[] userList);

  /**
   * On send message.
   */
  public void onSendMessage();


  /**
   * On view close.
   */
 // public void onViewClose();

  /**
   * On receive message.
   *
   * @param message the message
   */
  public void onReceiveMessage(String message);

  /**
   * Sets the presenter.
   *
   * @param presenter the new presenter
   */
  void setPresenter(Presenter presenter);

  /**
   * Show message window.
   *
   * @param message the message
   * @param title the title
   */
  void showMessageWindow(Object message, String title);

  /**
   * Show information window.
   *
   * @param message the message
   * @param title the title
   */
  void showInformationWindow(Object message, String title);

  /**
   * Show warning window.
   *
   * @param message the message
   * @param title the title
   */
  void showWarningWindow(Object message, String title);

  /**
   * Show error window.
   *
   * @param message the message
   * @param title the title
   */
  void showErrorWindow(Object message, String title);

}
