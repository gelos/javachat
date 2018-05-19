package chat.base;

/**
 * MVP passive View Interface.
 */
public interface View {

  /**
   * Implements all stuff what we need on the connection opening.
   *
   * @param windowTitle the window title
   */
  void onConnectionOpening(String windowTitle);

  /**
   * Implements all stuff what we need on the connection opened.
   *
   * @param windowTitle the window title
   */
  void onConnectionOpened(String windowTitle);

  /**
   * Implements all stuff what we need on the connection closing.
   *
   * @param windowTitle the window title
   */
  void onConnectionClosing(String windowTitle);

  /**
   * Implements all stuff what we need on the connection closed.
   *
   * @param windowTitle the window title
   */
  void onConnectionClosed(String windowTitle);

  /**
   * Implements all stuff what we need on chat user list update.
   *
   * @param userList the user list
   */
  void onUpdateChatUserList(String[] userList);

  /**
   * Implements all stuff what we need on the message send.
   */
  public void onSendMessage();

  /**
  * Implements all stuff what we need on the message received.
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
   * @param windowTitle the windowTitle
   */
  void showMessageWindow(Object message, String windowTitle);

  /**
   * Show information window.
   *
   * @param message the message
   * @param windowTitle the windowTitle
   */
  void showInformationWindow(Object message, String windowTitle);

  /**
   * Show warning window.
   *
   * @param message the message
   * @param windowTitle the windowTitle
   */
  void showWarningWindow(Object message, String windowTitle);

  /**
   * Show error window.
   *
   * @param message the message
   * @param windowTitle the windowTitle
   */
  void showErrorWindow(Object message, String windowTitle);

}
