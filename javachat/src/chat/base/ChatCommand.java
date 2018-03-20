package chat.base;

import static chat.base.CommandName.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The Class Chat Command. Commands to communicate between clients and chat server. Used
 * serialization for transfer command object on network.
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
   * Instantiates a new chat command, parsed from command string. Command parsed:
   * <li>{@link CommandName#CMDMSG}
   * <li>{@link CommandName#CMDENTER}
   * <li>{@link CommandName#CMDEXIT}
   * <li>{@link CommandName#CMDPRVMSG}
   * <p>
   * If message empty or command not in list return {@link CommandName#CMDERR} command.
   *
   * @param commandString the command string
   */
  public ChatCommand(String commandString) {

    // left trim, lowercase string and split by first left space in two piece
    String[] strArrayLowerCase =
        commandString.replaceAll("^\\s+", "").toLowerCase().split(CMDDLM.toString(), 2);

    // left trim string and split by first left space in two piece
    String[] strArray = commandString.replaceAll("^\\s+", "").split(CMDDLM.toString(), 2);

    // try to resolve first piece of string as valid command tag, return null if can't
    CommandName command = CommandName.get(strArrayLowerCase[0]);
    String payload = "";

    if (command == null) { // string can't resolved to command

      if (commandString.length() != 0) {
        command = CommandName.CMDMSG;
      } else {
        command = CommandName.CMDERR;
      }

    } else {
      switch (command) {

        // save payload for ENTER and EXIT commands
        case CMDENTER:
        case CMDEXIT:
          commandString = "";
          if (strArray.length > 1) {
            payload = strArray[1];
          }
          break;

        // save message for MSG command
        case CMDMSG:
          if (strArrayLowerCase.length > 1) {
            commandString = strArray[1];
          }
          break;

        case CMDPRVMSG: // process /PRVMSG DLM UDLM username list UDLM DLM message
                        // where username list used DLM to separate usernames
                        // else return error command
          strArray = commandString.split(CMDUDLM.toString(), 3);
          if (strArray.length > 2 && (strArray[2].length() > CMDDLM.toString().length())) {

            // remove if necessary first CMDDLM in command string
            if (strArray[2].substring(0, CMDDLM.toString().length())
                .equalsIgnoreCase(CMDDLM.toString())) {
              commandString = strArray[2].substring(CMDDLM.toString().length()); // get private
                                                                                 // message
            } else {
              commandString = strArray[2]; // get private message
            }

            payload = strArray[1].trim(); // get recipient username list

            // remove duplicated continuous CMDDLM
            strArray = payload.split(CMDDLM.toString());
            payload = "";
            for (String string : strArray) {
              if (!string.isEmpty()) {
                payload += (payload.length() == 0) ? string : CMDDLM.toString() + string;
              }
            }


          } else {
            command = CommandName.CMDERR;
          }
          break;

        default:
          command = CommandName.CMDERR;
          break;
      }
    }

    this.commandName = command;
    this.message = commandString;
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
