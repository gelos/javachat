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
    this(CMDERR, "");
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
   * @param messageSubString the command string
   */
  public ChatCommand(String commandString) {

    CommandName defaultCommandName = (commandString.length() == 0) ? CMDERR : CMDMSG;
    CommandName commandName = defaultCommandName;

    String payloadSubString = "";

    // left trim, lower case string and split by first left space in two piece
    // String[] strArrayLowerCase =
    // commandString.replaceAll("^\\s+", "").toLowerCase().split(CMDDLM.toString(), 2);

    // Left trim string and split by first left space in two piece by CMDDLM
    String leftTrimCommandString = commandString.replaceAll("^\\s+", "");
    
    String[] commandStringSplitedByCMDDLM = leftTrimCommandString.split(CMDDLM.toString(), 2);

    // Try to resolve first piece of string as valid command tag ignoring letter case, return null
    // if can't
    String commandNameStringLowerCase = commandStringSplitedByCMDDLM[0].toLowerCase();
    commandName = CommandName.get(commandNameStringLowerCase);

    String messageSubString = (commandStringSplitedByCMDDLM.length > 1) ? commandStringSplitedByCMDDLM[1] : "";

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

          // Split message to three parts: empty, username list and message by CMDUDLM
          // strArray = commandString.trim().split(CMDUDLM.toString(), 3);

          // strArray = messageSubString.trim().split(CMDUDLM.toString(), 3);

          // Left trim string and split message to three parts: empty, username list and message by
          // CMDUDLM
          commandStringSplitedByCMDDLM = messageSubString.replaceAll("^\\s+", "").split(CMDUDLM.toString(), 3);



          // Check that we split minimum on three parts, first part empty (see format) and user
          // name list not empty
          if (commandStringSplitedByCMDDLM.length > 2 && (commandStringSplitedByCMDDLM[2].length() > CMDDLM.toString().length())
              && commandStringSplitedByCMDDLM[0].isEmpty()) {

            // Remove if necessary first CMDDLM in command string
            if (commandStringSplitedByCMDDLM[2].substring(0, CMDDLM.toString().length())
                .equalsIgnoreCase(CMDDLM.toString())) {
              messageSubString = commandStringSplitedByCMDDLM[2].substring(CMDDLM.toString().length()); // get private
              // message
            } else {
              messageSubString = commandStringSplitedByCMDDLM[2]; // get private message
            }

            payloadSubString = commandStringSplitedByCMDDLM[1].trim(); // get recipient username list

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
