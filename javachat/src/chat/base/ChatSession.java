package chat.base;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ChatSession {
  protected AtomicBoolean isSessionOpenedFlag;
  /** The chat user. */
  protected User user = null;
  protected CommandHandler_new clientCommandHandler = null;

  public ChatSession() {
    isSessionOpenedFlag = new AtomicBoolean(false);
  }

  public ChatSession(Socket clientSocket) {
    this();
    runCommandHandler(clientSocket);
  }

  public void runCommandHandler(Socket clientSocket) {
    clientCommandHandler = new CommandHandler_new(clientSocket, this);
    clientCommandHandler.start();
  }

  public final Boolean getIsSessionOpenedFlag() {
    return isSessionOpenedFlag.get();
  }

  public void sendCommand(Command command) {
    command.send(clientCommandHandler.outputStream);
  };

  public abstract void processCommand(Command command);

  public abstract void openSession(String username);

  public void closeSession(boolean sendEXTCMD) {
    
    if (sendEXTCMD) {

      sendCommand(new Command(CommandName.CMDEXIT, ""));
    }
        
    
    if ((clientCommandHandler != null) && (clientCommandHandler.isRunning())) {
      clientCommandHandler.stop();
      try {
        clientCommandHandler.getThread().join();
        // TODO check that we join all thread before stop!!!
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

  };

}
