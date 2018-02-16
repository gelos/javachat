package chat.test;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import chat.client.mvp.swing.ChatClientPresenter;
import chat.client.mvp.swing.ChatClientSwingView;
import chat.server.ChatServer;
import mockit.Capturing;
import mockit.Expectations;

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
  void startStopClientTest(@Capturing ChatClientSwingView chatClientView) throws Throwable {
  //void startStopClientTest() {   
    
    // set exception handler to throw other thread exceptions in current thread
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });    
    
    final ChatClientPresenter chatClientPresenter = new ChatClientPresenter();
    
    //ChatClientSwingView chatClientView = new ChatClientSwingView();
    //ViewSwing chatClientView = new ViewSwing();
            
    //ChatClientSwingView chatClientSwingView = new ChatClientSwingView();
    
    chatClientPresenter.setView(chatClientView);
    
    new Expectations(ChatClientSwingView.class) {{
      //chatClientView.getPresenter(); result = chatClientPresenter;
      //chatClientView.getEnterTextField(); result = "this is test";
      chatClientView.showMsgChatPane(anyString); //result = null; 
      chatClientView.clearChatUserList(); result = null;
      chatClientView.updateChatUserList((String[])any); result = null;
      chatClientView.onConnectionOpened(anyString);
    }};  
    
    chatClientPresenter.setView(chatClientView);
    
    //System.out.println(chatClientView.getEnterTextField());
    
    //System.out.println(((ChatClientPresenter)chatClientView.getPresenterSwing()).getViewSwing().getEnterTextField());
    //chatClientPresenter.getViewSwing().showMsgChatPane("");
    
    chatClientPresenter.openConnection("oleg");
    
    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    chatClientPresenter.sendMsg("hello");
    
    //assertTimeout(Duration.ofNanos(1), () -> {chatClientPresenter.openConnection("oleg");});
    //(chatClientPresenter.openConnection("oleg"), "Cant connect to chat server.");
    
    System.out.println("connection oppened");
    //chatClientPresenter.sendChatMsg();
    
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
        
    chatClientPresenter.stop();
    
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // if we get other thread exception throw it in current thread
    if (exception.get() != null) {
      throw exception.get();
    }

    
    /*new Verifications() {{
      chatClientView.showMsgChatPane(anyString); times =1;
    }};
*/    
    //chatClientPresenter = null;
  }

}
