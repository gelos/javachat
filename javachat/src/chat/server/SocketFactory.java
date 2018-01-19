package chat.server;

import java.net.ServerSocket;

public interface SocketFactory {
  
  public ServerSocket createSocketFor(int port);

}
