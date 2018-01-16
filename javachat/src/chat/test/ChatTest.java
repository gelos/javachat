package chat.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import chat.client.mvp.swing.ChatClientSwingPresenter;
import chat.server.ChatServer;

class ChatTest {

  ChatServer server = null; 
  
  @BeforeEach
  void setUp() throws Exception {}

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void testServerStartStop() {
        
    server = null;
    
    Thread chatServerThread = new Thread() {
      @Override
      public void run() {
        server = new ChatServer();
      }
      
    };
    
    //ChatServer server = new ChatServer();
    
    chatServerThread.start();
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertNull(server);
    //assertTrue(server.close());
    //ChatClientSwingPresenter client = new ChatClientSwingPresenter();
    
    //fail("Not yet implemented");
  }

}
