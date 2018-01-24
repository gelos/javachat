package chat.server;

import java.io.IOException;
import java.net.ServerSocket;

public interface SocketFactory {
  
  public ServerSocket createSocketFor (int port) throws IOException;
  
}
