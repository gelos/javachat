package chat.base;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ChatSession {
  protected AtomicBoolean isSessionOpenedFlag;
  /** The chat user. */
  protected User user = null;
  
  public ChatSession() {
    isSessionOpenedFlag = new AtomicBoolean(false);
  }
  
  public final Boolean getIsSessionOpenedFlag() {
    return isSessionOpenedFlag.get();
  }

  public abstract void processCommand(Command Command);

}
