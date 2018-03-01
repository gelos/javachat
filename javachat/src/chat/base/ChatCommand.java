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


  // Constructors

  public ChatCommand() {
    this(CommandName.CMDERR, "");
  }

  public ChatCommand(String message) {

    // left trim, lowercase string and split by space in two piece
    String[] strArrayLowerCase = message.replaceAll("^\\s+","").toLowerCase().split(CommandName.CMDDLM.toString(), 2);
    // left trim string and split by space in two piece
    String[] strArray = message.replaceAll("^\\s+","").split(CommandName.CMDDLM.toString(), 2);

    CommandName command = CommandName.get(strArrayLowerCase[0]);
    String payload = "";
    
    if (command == null) {

      if (message.length() != 0) {
        command = CommandName.CMDMSG;
      } else {
        command = CommandName.CMDERR;
      }

    } else {
      switch (command) {

        case CMDENTER:
        case CMDEXIT:
          message = "";
          if (strArray.length > 1) {
            payload = strArray[1];
          }
          break;

        case CMDMSG:
          if (strArrayLowerCase.length > 1) {
            message = strArray[1];
          } 
          break;
          
        default:
          command = CommandName.CMDERR;
          break;
      }
    }

    this.cmdName = command;
    this.message = message;
    this.payload = payload;
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

  /*
   * (non-Javadoc)
   * 
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

  /*
   * (non-Javadoc)
   * 
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
