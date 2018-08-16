package chat.base;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The Enum CommandName used in {@link Command}. Command names that can be used:
 * <li>{@link #CMDOK}</li>
 * <li>{@link #CMDDLM}</li>
 * <li>{@link #CMDEXIT}</li>
 * <li>{@link #CMDENTER}</li>
 * <li>{@link #CMDUSRLST}</li>
 * <li>{@link #CMDPRVMSG}</li>
 * <li>{@link #CMDMSG}</li>
 * <li>{@link #CMDERR}</li>
 * <li>{@link #CMDHLP}</li>
 * <li>{@link #CMDOK}</li>
 */

public enum CommandName implements Serializable {

  /** The space character as delimiter of command parts in string command representation. */
  CMDDLM(" "),

  /**
   * The single quote character (') as delimiter of recipient user list in {@link CommandName#CMDPRVMSG} from
   * over command parts in string command representation.
   */
  CMDUDLM("\'"),

  /**
   * The delimiter inside recipient user list in {@link CommandName#CMDPRVMSG} in string command representation.
   */
  CMDULDLM(","),

  /** The command to close chat session. Initiated by client or server. */
  CMDEXIT("/exit"),

  /**
   * The command to start chat session. Initiated by client, processed by server. Payload must
   * contain user name.
   */
  CMDENTER("/enter"),

  /**
   * The command to update user list on client View. Payload is a string of the user names with
   * {@link CommandName#CMDDLM} as delimiter. Initiated by server, processed by client.
   */
  CMDUSRLST("/usrlst"),

  /**
   * The command to send private chat messages. Initiated by client, processed by server. Payload
   * list of message recipients.
   */
  CMDPRVMSG("/prvmsg"),

  /**
   * The command to send chat messages. Initiated by client, processed by server. Message contain
   * chat message, payload is empty.
   */
  CMDMSG("/msg"),

  /**
   * The error command, using if something going wrong. Example client or server receive unknown
   * command. Initiated by client or server, message contains error message.
   */
  CMDERR("/err"),

  /** The command to display help for supported commands. Initiated by client or server. */
  CMDHLP("/help"),

  /**
   * The command to approve client chat session establishing. Initiated by server, processed by
   * client. Payload is a approved command name.
   */
  CMDOK("/ok");

  /** The command name. */
  private String commandName;

  /** The map. */
  private final static Map<String, CommandName> lookup = new HashMap<>();

  /** Initialize map to reverse lookup */
  static {
    for (CommandName cn : EnumSet.allOf(CommandName.class)) {
      lookup.put(cn.toString(), cn);
    }
  }

  /**
   * Reverse lookup. Return {@link CommandName} by string name.
   *
   * @param key the command name string
   * @return the command name or null if command not found
   */
  public static CommandName get(String key) {
    return lookup.get(key);
  }

  /**
   * 
   *
   * @return the string
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return this.commandName;
  }

  /**
   * Instantiates a new command name.
   *
   * @param commandName the command name
   */
  private CommandName(String commandName) {
    this.commandName = commandName;
  }
}
