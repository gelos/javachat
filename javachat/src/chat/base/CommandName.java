package chat.base;

public enum CommandName {

  /**
   * The Constant _EXIT_CMD. Command to close chat session, pattern /exit. Initiated by client,
   * processed by server.
   */

  CMDDLM(" "), CMDEXIT("exit"), CMDENTER("enter"), CMDUSRLST("usrlst"), CMDPRVMSG("prvmsg"), CMDMSG(
      "msg"), CMDERR("err"), CMDHLP("help"), CMDOK("ok");

  /**
   * The Constant _ENTER_CMD. Command to start chat session, pattern /enter username. Initiated by
   * client, processed by server.
   */
  // public static final String CMD_ENTER = "/enter";
  /**
   * The Constant _USRLST_CMD. Command to update user list in client GUI, pattern /usrlst ulst where
   * ulst string of usernames with space character delimeter. Initiated by server, processed by
   * client.
   */
  // public static final String CMD_USRLST = "/usrlst";

  /** The Constant _PRVMSG_CMD. */
  // public static final String CMD_PRVMSG = "/prvmsg";

  /** The Constant _MSG_CMD. */
  // public static final String CMD_MSG = "/msg";

  /** The Constant _HELP_CMD. */
  // public static final String CMD_HELP = "/help";

  // public static final String CMD_ERR = "/err";

  // private static final String CMD_DELIMETER = " ";



  private String commandName;

  private CommandName(String commandName) {
    this.commandName = commandName;
  }

  @Override
  public String toString() {
    return this.commandName;
  }
}
