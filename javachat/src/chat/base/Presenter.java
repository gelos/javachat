package chat.base;

// TODO: Auto-generated Javadoc
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
   * Send command.
   *
   * @param commandString the command string
   */
  void sendCommand(String commandString);

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
