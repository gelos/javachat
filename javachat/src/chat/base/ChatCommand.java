package chat.base;

/**
 * The Class Chat Command. 
 */
public class ChatCommand {

  /** The command name. */
  private CommandName cmdName;
  
  /** The payload. */
  private String payload;

  /**
   * Gets the command.
   *
   * @return the command
   */
  public final CommandName getCommand() {
    return this.cmdName;
  }

  /**
   * Gets the payload.
   *
   * @return the payload
   */
  public final String getPayload() {
    return this.payload;
  }

  /**
   * Instantiates a new chat command.
   *
   * @param cmdName the CommandName
   * @param payload the payload string
   */
  public ChatCommand(CommandName cmdName, String payload) {
    this.cmdName = cmdName;
    this.payload = payload;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((cmdName == null) ? 0 : cmdName.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ChatCommand [cmdName=" + cmdName + ", payload=" + payload + "]";
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ChatCommand other = (ChatCommand) obj;
    if (cmdName != other.cmdName)
      return false;
    return true;
  }

}
