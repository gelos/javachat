package chat.base;

/**
 * The User Name class for store chat user information. 
 */
public class ChatUser {

  /** The user name. */
  private String username;

  /**
   * Instantiates a new chat user.
   *
   * @param username the user name String
   */
  public ChatUser(String username) {
    this.username = username;
  }

  /**
   * Gets the username.
   *
   * @return the user name String
   */
  public String getUsername() {
    return username;
  }


}
