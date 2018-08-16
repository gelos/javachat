package chat.base;

import static chat.base.CommandName.CMDDLM;
import static chat.base.CommandName.CMDERR;
import static chat.base.CommandName.CMDMSG;
import static chat.base.CommandName.CMDUDLM;
import static chat.base.CommandName.CMDULDLM;
import java.io.Serializable;

/**
 * The Class Chat Command. Commands to communicate between clients and chat server. Used
 * serialization for transfer command object on network.
 */
public class Command implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The command name. {@link CommandName} */
  private CommandName commandName;

  /** The message. */
  private String message;

  /** The payload. */
  private String payload;

  /**
   * Default constructor, instantiates a new ERR chat command with empty message.
   */
  public Command() {
    this(CMDERR, "");
  }

  /**
   * Instantiating a new command, parsed from the input commandString. Command parsed:
   * 
   * If the message is empty or the command is not found the list, return {@link CommandName#CMDERR}
   * command.
   *
   * @param commandString the command string
   */
  
  // TODO Fix it
  
//  <li>{@link CommandName#CMDMSG}
//  * <li>{@link CommandName#CMDENTER}
//  * <li>{@link CommandName#CMDEXIT}
//  * <li>{@link CommandName#CMDPRVMSG}
//  * <p>
  
  
  public Command(String commandString) {

    CommandName defaultCommandName = (commandString.length() == 0) ? CMDERR : CMDMSG;
    CommandName commandName = defaultCommandName;

    String payloadSubString = "";

    // Left trim string and split by first left space in two piece by CMDDLM
    String leftTrimCommandString = commandString.replaceAll("^\\s+", "");

    String[] commandStringSplitedByCMDDLM = leftTrimCommandString.split(CMDDLM.toString(), 2);

    // Trying to resolve first piece of string as the valid command tag. Ignoring letter case,
    // return null if failed
    String commandNameStringLowerCase = commandStringSplitedByCMDDLM[0].toLowerCase();
    commandName = CommandName.get(commandNameStringLowerCase);

    String messageSubString =
        (commandStringSplitedByCMDDLM.length > 1) ? commandStringSplitedByCMDDLM[1] : "";

    // If first sub string can't resolved to command
    if (commandName == null) {

      messageSubString = commandString;
      // And command string not empty, try it as CMDMSG
      if (commandString.length() != 0) {
        commandName = CommandName.CMDMSG;
      } else {
        commandName = CommandName.CMDERR;
      }

    } else {
      switch (commandName) {

        case CMDENTER:
        case CMDEXIT:
          payloadSubString = messageSubString;
          messageSubString = "";
          break;

        case CMDMSG:
          break;

        case CMDPRVMSG: // process /PRVMSG DLM UDLM username list UDLM DLM message
                        // where username list used ULDLM to separate usernames
                        // else return error command

          // Left trim string and split message to three parts: empty, username list and
          // message by CMDUDLM
          commandStringSplitedByCMDDLM =
              messageSubString.replaceAll("^\\s+", "").split(CMDUDLM.toString(), 3);

          // Checking that we splitted minimum by three parts. First part empty (see format) user
          // name list not empty
          if (commandStringSplitedByCMDDLM.length > 2
              && (commandStringSplitedByCMDDLM[2].length() > CMDDLM.toString().length())
              && commandStringSplitedByCMDDLM[0].isEmpty()) {

            // Removing if necessary first CMDDLM in command string
            if (commandStringSplitedByCMDDLM[2].substring(0, CMDDLM.toString().length())
                .equalsIgnoreCase(CMDDLM.toString())) {

              // Getting the private message
              messageSubString =
                  commandStringSplitedByCMDDLM[2].substring(CMDDLM.toString().length());
            } else {
              messageSubString = commandStringSplitedByCMDDLM[2];
            }

            // Geting the recipient username list
            payloadSubString = commandStringSplitedByCMDDLM[1].trim();

            // remove duplicated continuous CMDDLM
            commandStringSplitedByCMDDLM = payloadSubString.split(CMDULDLM.toString());
            payloadSubString = "";
            for (String string : commandStringSplitedByCMDDLM) {
              if (!string.isEmpty()) {
                payloadSubString +=
                    ((payloadSubString.length() != 0) ? CMDULDLM.toString() : "") + string.trim();
              }
            }

          } else {
            // Generate error command and with original command string in messages field
            commandName = CommandName.CMDERR;
            messageSubString = commandString;
          }
          break;

        default:
          commandName = CMDERR;
          messageSubString = commandString;
          break;
      }
    }

    this.commandName = commandName;
    this.message = messageSubString;
    this.payload = payloadSubString;
  }

  /**
   * Instantiates a new chat command.
   *
   * @param commandName the command name
   * @param message the message
   */
  public Command(CommandName commandName, String message) {
    this(commandName, message, "");
  }

  /**
   * Instantiates a new chat command.
   *
   * @param commandName the command name
   * @param message the message
   * @param payload the payload
   */
  public Command(CommandName commandName, String message, String payload) {
    this.commandName = commandName;
    this.message = message;
    this.payload = payload;
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
    return "Command [commandName=" + commandName + ", message=" + message + ", payload=" + payload
        + "]";
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
    Command other = (Command) obj;
    if (commandName != other.commandName)
      return false;
    return true;
  }

}
