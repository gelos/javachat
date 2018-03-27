package chat.base;

/**
 * The Interface Presenter.
 */
public interface Presenter {

  /**
   * Open connection.
   *
   * @param username the username
   */
  void openConnection(String username);

  /**
   * Close connection.
   */
  void closeConnection();

  /**
   * Send message.
   *
   * @param message the message
   */
  void sendMessage(String message);

  /**
   * Send private message.
   *
   * @param message the message
   * @param userList the user list
   */
  void sendPrivateMessage(String message, String userList);

  /**
   * Sets the view.
   *
   * @param view the new view
   */
  void setView(View view);

  /**
   * Gets the view.
   *
   * @return the view
   */
  View getView();
}
