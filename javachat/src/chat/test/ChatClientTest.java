package chat.test;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import chat.client.mvp.swing.ChatClientSwingPresenter;
import chat.client.mvp.swing.ChatClientSwingView;
import chat.client.mvp.swing.PresenterSwing;
import chat.client.mvp.swing.ViewSwing;
import chat.client.swing.ChatClient;
import chat.server.ChatServer;
import mockit.*;

class ChatClientTest {

  private ChatServer chatServer;
  //private ChatClientSwingPresenter chatClientPresenter;
    
  @BeforeEach
  void setUp() throws Exception {
    
    // create server
    chatServer = new ChatServer();
    int timeout = 1;

    // wait while chatServer started
    while (!chatServer.isStarted() && (timeout <= 10)) {
      TimeUnit.SECONDS.sleep(1);
      timeout++;
    }   
    
    // create client
   //chatClientPresenter = new ChatClientSwingPresenter();
      
  }

  @AfterEach
  void tearDown() throws Exception {
   
    // stop client
    //chatClientPresenter.closeConnection();
   // chatClientPresenter = null;
    
    // stop server
    chatServer.stop();
    chatServer = null;  
   
  }

  
  @DisplayName("Start one client connect to server, send \"Hello\" message and then disconnect.")
  @Test
  //void startStopClientTest(final @Tested ChatClientSwingPresenter chatClientPresenter, @Capturing ChatClientSwingView chatClientView) {  
  void startStopClientTest(@Capturing ChatClientSwingView chatClientView) {
  //void startStopClientTest() {
    
    final ChatClientSwingPresenter chatClientPresenter = new ChatClientSwingPresenter();
    
    //ChatClientSwingView chatClientView = new ChatClientSwingView();
    //ViewSwing chatClientView = new ViewSwing();
            
    //ChatClientSwingView chatClientSwingView = new ChatClientSwingView();
    
    new Expectations() {{
      chatClientView.getPresenterSwing(); result = chatClientPresenter;
      chatClientView.getEnterTextField(); result = "this is test";
      chatClientView.showMsgChatPane(anyString);
    }};  
    
    chatClientPresenter.setView(chatClientView);
    
    //System.out.println(chatClientView.getEnterTextField());
    
    //System.out.println(((ChatClientSwingPresenter)chatClientView.getPresenterSwing()).getViewSwing().getEnterTextField());
    //chatClientPresenter.getViewSwing().showMsgChatPane("");
    
    assertTimeout(Duration.ofNanos(1), () -> {chatClientPresenter.openConnection("oleg");});
    //(chatClientPresenter.openConnection("oleg"), "Cant connect to chat server.");
    
    System.out.println("connection oppened");
    //chatClientPresenter.sendChatMsg();
    
    chatClientPresenter.closeConnection();
    
    //chatClientPresenter = null;
  }

}
