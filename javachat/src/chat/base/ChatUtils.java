package chat.base;

import java.io.PrintWriter;

/**
 * The Class ChatUtils.
 */
public final class ChatUtils {

  /**
   * Send command.
   *
   * @param cmd the Chat Command
   * @param outStream the stream to send command
   */
  public static void sendCommand(ChatCommand cmd, PrintWriter outStream) {
    outStream
        .println(cmd.getCommand().toString() + CommandName.CMDDLM.toString() + cmd.getPayload());
  }

  /**
   * Parse the message. If can't parse message return ERR Chat Command with original message in
   * payload.
   *
   * @param message the String message to parse
   * @return the Chat Command
   */
  public static ChatCommand parseMessage(String message) {

    CommandName command = CommandName.CMDERR;
    
    //trim spaces
    message = message.trim();
    
    String cmdString = "";
    String payload = message;    
    
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
