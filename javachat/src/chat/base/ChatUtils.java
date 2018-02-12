package chat.base;

import java.io.PrintWriter;

public final class ChatUtils {

  public static void sendCommand(ChatCommand cmd, PrintWriter outStream) {
    outStream.println(cmd.getCommand().toString() + CommandName.CMDDLM.toString() + cmd.getPayload());
  }

  public static ChatCommand parseMessage(String message) {
    
    CommandName command = CommandName.CMDERR;
    String cmdString = "";
    String payload = "";
    
    String[] str = message.split(CommandName.CMDDLM.toString(), 2);    
    
    if (str.length == 2) {
      cmdString = str[0];
      
      try {
        command = CommandName.valueOf(cmdString);
        payload = str[1];
      } catch (IllegalArgumentException | NullPointerException   e) {
        command = CommandName.CMDERR;
      }
  
    }
        
    return new ChatCommand(command, payload);
  }
  
}
