package chat.test;

import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import chat.client.mvp.swing.ChatClientSwingPresenter;
import chat.server.ChatServer;

class ChatTest {

  @BeforeEach
  void setUp() throws Exception {}

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void test() {
    
    ChatServer server = new ChatServer();
    
    ChatClientSwingPresenter client = new ChatClientSwingPresenter();
    
    fail("Not yet implemented");
  }

}
