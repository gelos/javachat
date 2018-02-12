package chat.base;

import java.io.OutputStream;
import java.io.PrintWriter;

public class ChatCommand {

  private CommandName command;
  private String payload;

  public final CommandName getCommand() {
    return this.command;
  }

  public final String getPayload() {
    return this.payload;
  }

  public ChatCommand(CommandName command, String payload) {
    this.command = command;
    this.payload = payload;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((command == null) ? 0 : command.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ChatCommand other = (ChatCommand) obj;
    if (command != other.command)
      return false;
    return true;
  }

}
