package chat.test;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import chat.client.mvp.swing.ChatClientPresenter;
import chat.client.mvp.swing.ChatClientSwingView;
import chat.client.mvp.swing.Presenter;
import chat.client.mvp.swing.ViewSwing;
import chat.client.swing.ChatClient;
import chat.server.ChatServer;
import mockit.*;

class ChatClientTest {

  private ChatServer chatServer;
  //private ChatClientPresenter chatClientPresenter;
    
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
   //chatClientPresenter = new ChatClientPresenter();
      
  }

  @AfterEach
  void tearDown() throws Exception {
   
    // stop client
    //chatClientPresenter.closeConnection();
   // chatClientPresenter = null;
    
    // stop server
    chatServer.stop();
    //chatServer = null;  
   
  }

  
  @DisplayName("Start one client connect to server, send \"Hello\" message and then disconnect.")
  @Test
  //void startStopClientTest(final @Tested ChatClientPresenter chatClientPresenter, @Capturing ChatClientSwingView chatClientView) {  
  //void startStopClientTest(@Capturing ChatClientSwingView chatClientView) {
  void startStopClientTest(@Capturing ChatClientSwingView chatClientView) {
  //void startStopClientTest() {
    
    final ChatClientPresenter chatClientPresenter = new ChatClientPresenter();
    
    //ChatClientSwingView chatClientView = new ChatClientSwingView();
    //ViewSwing chatClientView = new ViewSwing();
            
    //ChatClientSwingView chatClientSwingView = new ChatClientSwingView();
    
    chatClientPresenter.setView(chatClientView);
    
    new Expectations(ChatClientSwingView.class) {{
      //chatClientView.getPresenter(); result = chatClientPresenter;
      //chatClientView.getEnterTextField(); result = "this is test";
      chatClientView.showMsgChatPane(anyString); result = null;
    }};  
    
    chatClientPresenter.setView(chatClientView);
    
    //System.out.println(chatClientView.getEnterTextField());
    
    //System.out.println(((ChatClientPresenter)chatClientView.getPresenterSwing()).getViewSwing().getEnterTextField());
    //chatClientPresenter.getViewSwing().showMsgChatPane("");
    
    chatClientPresenter.openConnection("oleg");
    
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    //assertTimeout(Duration.ofNanos(1), () -> {chatClientPresenter.openConnection("oleg");});
    //(chatClientPresenter.openConnection("oleg"), "Cant connect to chat server.");
    
    System.out.println("connection oppened");
    //chatClientPresenter.sendChatMsg();
    
    chatClientPresenter.stop();
    
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    new Verifications() {{
      chatClientView.showMsgChatPane(anyString); times =1;
    }};
    
    //chatClientPresenter = null;
  }

}
