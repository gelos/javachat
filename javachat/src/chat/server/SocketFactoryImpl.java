package chat.server;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketFactoryImpl implements SocketFactory {

  @Override
  public ServerSocket createSocketFor(int port) throws IOException {
    
    ServerSocket res = null;
    
    //try {
      res = new ServerSocket(port);
    /*} catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } */
    
    return res;
  }

}
