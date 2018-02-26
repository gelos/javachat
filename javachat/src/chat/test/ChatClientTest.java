package chat.test;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import chat.client.mvp.swing.ChatClientPresenter;
import chat.client.mvp.swing.ChatClientViewSwing;
import chat.server.ChatServer;
import mockit.Capturing;
import mockit.Expectations;
import mockit.Verifications;

class ChatClientTest {

  private ChatServer chatServer;
  // private ChatClientPresenter chatClientPresenter;

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
    // chatClientPresenter = new ChatClientPresenter();

  }

  @AfterEach
  void tearDown() throws Exception {

    // stop client
    // chatClientPresenter.closeConnection();
    // chatClientPresenter = null;

    // stop server
    chatServer.stop();
    // chatServer = null;

  }

  @Disabled
  @DisplayName("Start one client connect to server, send \"Hello\" message and then disconnect.")
  @Test
  // void startStopClientTest(final @Tested ChatClientPresenter chatClientPresenter, @Capturing
  // ChatClientViewSwing chatClientView) {
  // void startStopClientTest(@Capturing ChatClientViewSwing chatClientView) {
  void startStopClientTest(@Capturing ChatClientViewSwing chatClientView) throws Throwable {
    // void startStopClientTest() {

    // set exception handler to throw other thread exceptions in current thread
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    final ChatClientPresenter chatClientPresenter = new ChatClientPresenter();

    // ChatClientViewSwing chatClientView = new ChatClientViewSwing();
    // ViewSwing chatClientView = new ViewSwing();

    // ChatClientViewSwing chatClientSwingView = new ChatClientViewSwing();

    chatClientPresenter.setView(chatClientView);

    new Expectations(ChatClientViewSwing.class) {
      {
        // chatClientView.getPresenter(); result = chatClientPresenter;
        // chatClientView.getEnterTextField(); result = "this is test";
        chatClientView.showMsgOnChatPane(anyString); // result = null;
        chatClientView.clearChatUserList();
        result = null;
        chatClientView.updateChatUserList((String[]) any);
        result = null;
        chatClientView.onConnectionOpened(anyString);
      }
    };

    chatClientPresenter.setView(chatClientView);

    // System.out.println(chatClientView.getEnterTextField());

    // System.out.println(((ChatClientPresenter)chatClientView.getPresenterSwing()).getViewSwing().getEnterTextField());
    // chatClientPresenter.getViewSwing().showMsgChatPane("");

    chatClientPresenter.openConnection("oleg");

    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    chatClientPresenter.sendMsg("hello");

    // assertTimeout(Duration.ofNanos(1), () -> {chatClientPresenter.openConnection("oleg");});
    // (chatClientPresenter.openConnection("oleg"), "Cant connect to chat server.");

    System.out.println("connection oppened");
    // chatClientPresenter.sendChatMsg();

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


    /*
     * new Verifications() {{ chatClientView.showMsgChatPane(anyString); times =1; }};
     */
    // chatClientPresenter = null;
  }

  @Test
  @DisplayName("Test for sending message for three clients.")
  void sendMsgTest(@Capturing ChatClientViewSwing chatClientView) throws Throwable {

    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    final ChatClientPresenter chatClientPresenter1 = new ChatClientPresenter();
    chatClientPresenter1.setView(chatClientView);
    final ChatClientPresenter chatClientPresenter2 = new ChatClientPresenter();
    chatClientPresenter2.setView(chatClientView);
    final ChatClientPresenter chatClientPresenter3 = new ChatClientPresenter();
    chatClientPresenter3.setView(chatClientView);

    new Expectations(ChatClientViewSwing.class) {
      {
        // chatClientView.getPresenter(); result = chatClientPresenter;
        // chatClientView.getEnterTextField(); result = "this is test";
        chatClientView.onSendMsg(); result = null; times = 3;
        chatClientView.showMsgOnChatPane(anyString); // result = null;
        chatClientView.clearChatUserList();
        result = null;
        chatClientView.updateChatUserList((String[]) any);
        result = null;
        chatClientView.onConnectionOpened(anyString);
      }
    };

    chatClientPresenter1.setView(chatClientView);
    chatClientPresenter2.setView(chatClientView);
    chatClientPresenter3.setView(chatClientView);

    chatClientPresenter1.openConnection("client1");
    chatClientPresenter2.openConnection("client2");
    chatClientPresenter3.openConnection("client3");

    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    chatClientPresenter1.sendMsg("hello1");
    chatClientPresenter2.sendMsg("hello2");
    chatClientPresenter3.sendMsg("hello3");  
    
    chatClientPresenter1.stop();
    chatClientPresenter2.stop();
    chatClientPresenter3.stop();

    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    new Verifications() {};
    
    // if we get other thread exception throw it in current thread
    if (exception.get() != null) {
      throw exception.get();
    } 

  }

}
