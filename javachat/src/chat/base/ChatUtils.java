package chat.base;

import java.io.PrintWriter;

/**
 * The Class ChatUtils.
 */
public final class ChatUtils {

  /**
   * Send command.
   *
   * @param cmd the chat command
   * @param outStream the stream to send command
   */
  public static void sendCommand(ChatCommand cmd, PrintWriter outStream) {
    outStream
        .println(cmd.getCommand().toString() + CommandName.CMDDLM.toString() + cmd.getPayload());
  }

  /**
   * Parses the message. If can't parse message return ERR chat command.
   *
   * @param message the message string
   * @return the chat command
   */
  public static ChatCommand parseMessage(String message) {

    CommandName command = CommandName.CMDERR;
    String cmdString = "";
    String payload = "";

    // split string with CMDDLM on command and payload
    String[] str = message.split(CommandName.CMDDLM.toString(), 2);

    if (str.length == 2) {
      cmdString = str[0];

      command = CommandName.get(cmdString);
      if (command != CommandName.CMDERR) {
        payload = str[1];
      }
    }

    return new ChatCommand(command, payload);
  }

}
