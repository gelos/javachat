package chat.base;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The Enum CommandName. Command format <command name> <command delimiter> <command payload>
 */
public enum CommandName {

  /**  The command delimiter. */
  CMDDLM(" "),

  /** The command to close chat session. Initiated by client or server. */
  CMDEXIT("/exit"),

  /** The command to start chat session. Initiated by client, processed by server. */
  CMDENTER("/enter"),

  /**
   * The command to update user list on client View, payload is a string of the user names with
   * space character delimiter. Initiated by server, processed by client.
   */
  CMDUSRLST("/usrlst"),

  /** The command to send private chat messages. Initiated by client, processed by server. */
  CMDPRVMSG("/prvmsg"),

  /** The command to send chat messages. Initiated by client, processed by server. */
  CMDMSG("/msg"),

  /**
   * The error command, using if something going wrong. Example client or server receive unknown
   * command. Initiated by client or server.
   */
  CMDERR("/err"),

  /** The command to display help for supported commands. Initiated by client or server. */
  CMDHLP("/help"),

  /**
   * The command to approve client chat session establishing. Initiated by server, processed by
   * client.
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
   * Reverse lookup. String value of command name to command name.
   *
   * @param key the command name string
   * @return the command name or err command if command not found
   */
  public static CommandName get(String key) {
    CommandName res = CommandName.CMDERR;
    if (lookup.get(key) != null) {
      res = lookup.get(key);
    };
    return res;
  }
  
  /**
   * Instantiates a new command name.
   *
   * @param commandName the command name
   */
  private CommandName(String commandName) {
    this.commandName = commandName;
  }

  /* (non-Javadoc)
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return this.commandName;
  }
}
