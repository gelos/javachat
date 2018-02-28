package chat.base;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The Class Chat Command. 
 */
public class ChatCommand implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** The command name. */
  private CommandName cmdName;
  
  /** The message. */
  private String message;

  private String payload;

  public ChatCommand() {
    this(CommandName.CMDERR,"");
  }
  
  public ChatCommand(CommandName cmdName, String message) {
    this(cmdName, message, "");
  }
  
  /**
   * Instantiates a new chat command.
   *
   * @param cmdName the CommandName
   * @param message the message string
   */
  public ChatCommand(CommandName cmdName, String message, String payload) {
    this.cmdName = cmdName;
    this.message = message;
    this.payload = payload;
  }

  public final void send(ObjectOutputStream outputStream) {
    try {
      outputStream.writeObject(this);
      outputStream.flush();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public final String getPayload() {
    return payload;
  }

  /**
   * Gets the command.
   *
   * @return the command
   */
  public final CommandName getCommand() {
    return this.cmdName;
  }

  /**
   * Gets the message.
   *
   * @return the message
   */
  public final String getMessage() {
    return this.message;
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

  @Override
  public String toString() {
    return "ChatCommand [cmdName=" + cmdName + ", message=" + message + ", payload=" + payload
        + "]";
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
