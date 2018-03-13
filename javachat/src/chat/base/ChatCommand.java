package chat.base;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The Class Chat Command to communicate between clients and chat server. Used serialization for
 * transfer command object on network.
 */
public class ChatCommand implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The command name. {@see CommandName} */
  private CommandName commandName;

  /** The message. */
  private String message;

  /** The payload. */
  private String payload;

  // Constructors

  /**
   * Default constructor, instantiates a new ERR chat command with empty message.
   */
  public ChatCommand() {
    this(CommandName.CMDERR, "");
  }

  /**
   * Instantiates a new chat command, parsed from message. Default command type is MSG, also parse
   * ENTER and EXIT. If message empty or command not MSG, ENTER or EXIT when return ERR command.
   *
   * @param message the message
   */
  public ChatCommand(String message) {

    // left trim, lowercase string and split by first left space in two piece
    String[] strArrayLowerCase =
        message.replaceAll("^\\s+", "").toLowerCase().split(CommandName.CMDDLM.toString(), 2);

    // left trim string and split by first left space in two piece
    String[] strArray = message.replaceAll("^\\s+", "").split(CommandName.CMDDLM.toString(), 2);

    // try to resolve first piece of string as valid command tag
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

    this.commandName = command;
    this.message = message;
    this.payload = payload;
  }



  /**
   * Instantiates a new chat command.
   *
   * @param commandName the command name
   * @param message the message
   */
  public ChatCommand(CommandName commandName, String message) {
    this(commandName, message, "");
  }

  /**
   * Instantiates a new chat command.
   *
   * @param commandName the command name
   * @param message the message
   * @param payload the payload
   */
  public ChatCommand(CommandName commandName, String message, String payload) {
    this.commandName = commandName;
    this.message = message;
    this.payload = payload;
  }

  /**
   * Send.
   *
   * @param outputStream the output stream
   */
  public final void send(ObjectOutputStream outputStream) {
    try {
      outputStream.writeObject(this);
      outputStream.flush();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Gets the payload.
   *
   * @return the payload
   */
  public final String getPayload() {
    return payload;
  }

  /**
   * Gets the command name.
   *
   * @return the command name
   */
  public final CommandName getCommandName() {
    return this.commandName;
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
    result = prime * result + ((commandName == null) ? 0 : commandName.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ChatCommand [commandName=" + commandName + ", message=" + message + ", payload="
        + payload + "]";
  }

  /**
   * Compares only by command name.
   *
   * @param obj the obj
   * @return true, if successful
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
    if (commandName != other.commandName)
      return false;
    return true;
  }

}
