package chat.test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import chat.client.mvp.swing.ChatClientPresenter;
import chat.client.mvp.swing.ChatClientViewSwing;
import chat.client.mvp.swing.View;
import chat.server.ChatServer;
import mockit.Capturing;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

class ChatClientIntegrationTest {

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
    // View chatClientView = new View();

    // ChatClientViewSwing chatClientSwingView = new ChatClientViewSwing();

    chatClientPresenter.setView(chatClientView);

    new Expectations(ChatClientViewSwing.class) {
      {
        // chatClientView.getPresenter(); result = chatClientPresenter;
        // chatClientView.getEnterTextField(); result = "this is test";
        // chatClientView.showMsgOnChatPane(anyString); // result = null;
        // chatClientView.clearChatUserList();
        // result = null;
        chatClientView.onUpdateChatUserList((String[]) any);
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

    chatClientPresenter.parseMessage("hello");

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
  void sendMsgTest(@Capturing View chatClientView1, @Capturing View chatClientView2)
      throws Throwable {

    //TODO test names with spaces
    
    final String CLIENT_NAME1 = "client1";
    final String CLIENT_NAME2 = "client2";
    final String MSG1 = "hello 1";
    final String MSG2 = "hello 2";
    
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    final ChatClientPresenter chatClientPresenter1 = new ChatClientPresenter();
    final ChatClientPresenter chatClientPresenter2 = new ChatClientPresenter();

    /*
     * new Expectations(View.class) { { chatClientView1.onConnectionOpened(anyString);
     * chatClientView1.onUpdateChatUserList((String[]) any); chatClientView1.onSendMsg(anyString);
     * chatClientView1.onReceiveMsg(anyString); chatClientView2.onConnectionOpened(anyString);
     * chatClientView2.onUpdateChatUserList((String[]) any); chatClientView2.onSendMsg(anyString);
     * chatClientView2.onReceiveMsg(anyString); } };
     */

    chatClientPresenter1.setView(chatClientView1);
    chatClientPresenter2.setView(chatClientView2);

    chatClientPresenter1.openConnection(CLIENT_NAME1);

    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    new Verifications() {{
      assertTrue(chatClientPresenter1.getIsSessionOpened());
      
      String [] usrList;
      chatClientView1.onUpdateChatUserList(usrList = withCapture());
      assertTrue(Arrays.asList(usrList).contains(CLIENT_NAME1));
      
      String message;
      chatClientView1.onReceiveMsg(message = withCapture());
      assertTrue(message.contains(CLIENT_NAME1 + " login"));
    }};
    
    chatClientPresenter2.openConnection(CLIENT_NAME2);
    
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    new Verifications() {{
      assertTrue(chatClientPresenter2.getIsSessionOpened());
      
      String [] usrList;
      chatClientView1.onUpdateChatUserList(usrList = withCapture());
      assertTrue(Arrays.asList(usrList).contains(CLIENT_NAME1));
      assertTrue(Arrays.asList(usrList).contains(CLIENT_NAME2));
      chatClientView2.onUpdateChatUserList(usrList = withCapture());
      assertTrue(Arrays.asList(usrList).contains(CLIENT_NAME1));
      assertTrue(Arrays.asList(usrList).contains(CLIENT_NAME2));
      
      String message;
      chatClientView1.onReceiveMsg(message = withCapture());
      assertTrue(message.contains(CLIENT_NAME2 + " login"));
      chatClientView2.onReceiveMsg(message = withCapture());
      assertTrue(message.contains(CLIENT_NAME2 + " login"));
    }};
    
    chatClientPresenter1.parseMessage(MSG1);
    new Verifications() {
      {
        String message;
        chatClientView1.onReceiveMsg(message = withCapture());
        assertTrue(message.contains(CLIENT_NAME1 + ": " + MSG1));
        chatClientView2.onReceiveMsg(message = withCapture());
        assertTrue(message.contains(CLIENT_NAME1 + ": " + MSG1));
      }
    };

    chatClientPresenter2.parseMessage("hello2");
    new Verifications() {
      {
        String message;
        chatClientView1.onReceiveMsg(message = withCapture());
        assertTrue(message.contains("client2: hello2"));
        chatClientView2.onReceiveMsg(message = withCapture());
        assertTrue(message.contains("client2: hello2"));
      }
    };


    chatClientPresenter1.stop();
    chatClientPresenter2.stop();

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

    new Verifications() {
      {
        chatClientView2.onConnectionOpened(anyString);
        times = 2;
        // chatClientView.onUpdateChatUserList((String[]) any); times = 9;
        chatClientView1.onSendMsg(anyString);
        times = 2;
        chatClientView2.onReceiveMsg(anyString);
        times = 6;
      }
    };

  }

}
