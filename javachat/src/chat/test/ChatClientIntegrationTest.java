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
import chat.base.ChatClientPresenter;
import chat.base.View;
import chat.client.mvp.swing.ChatClientViewSwing;
import chat.server.ChatServer;
import mockit.Capturing;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Mocked;
import mockit.Verifications;

class ChatClientIntegrationTest {

  private ChatServer chatServer;

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

  }

  @AfterEach
  void tearDown() throws Exception {

    // stop server
    chatServer.stop();

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

    chatClientPresenter.sendMessage("hello");

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

    chatClientPresenter.closeConnection();

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
  @DisplayName("Test for sending messages between two clients.")
  void sendMessageTwoClientsTest(@Mocked View chatClientView1, @Mocked View chatClientView2)
      throws Throwable {

    // TODO test names with spaces
    // TODO test opening session in already opened session

    // final int SRV_TIMEOUT = 1;
    final String CLIENT_NAME1 = "client1";
    final String CLIENT_NAME2 = "client2";
    final String MSG1 = "hello 1";
    final String MSG2 = "hello 2";
    final String LOGIN_MSG = "login";
    final String LOGOUT_MSG = "logout";

    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    final ChatClientPresenter chatClientPresenter1 = new ChatClientPresenter();
    final ChatClientPresenter chatClientPresenter2 = new ChatClientPresenter();

    chatClientPresenter1.setView(chatClientView1);
    chatClientPresenter2.setView(chatClientView2);

    // connect client 1
    chatClientPresenter1.openConnection(CLIENT_NAME1);

    // wait timeout for server processing
    /*
     * try { TimeUnit.SECONDS.sleep(SRV_TIMEOUT); } catch (InterruptedException e) {
     * e.printStackTrace(); }
     */

    new FullVerifications() {
      { // check for normal session opening command sequence for client 1

        chatClientView1.onConnectionOpening(anyString);

        String username; // check onConnectionOpened after ok command received
        chatClientView1.onConnectionOpened(username = withCapture());
        assertTrue(username.equalsIgnoreCase(CLIENT_NAME1));

        String[] usrList; // check for user list update
        chatClientView1.onUpdateChatUserList(usrList = withCapture());
        assertTrue(Arrays.asList(usrList).contains(CLIENT_NAME1));

        String message; // check for welcome user message
        chatClientView1.onReceiveMessage(message = withCapture());
        assertTrue(message.contains(CLIENT_NAME1 + " " + LOGIN_MSG));
      }
    };

    // connect client 2 and check normal command sequence for client1 and client2
    chatClientPresenter2.openConnection(CLIENT_NAME2);

    /*
     * try { TimeUnit.SECONDS.sleep(SRV_TIMEOUT); } catch (InterruptedException e) {
     * e.printStackTrace(); }
     */

    new FullVerifications() {
      {
        chatClientView2.onConnectionOpening(anyString);

        String username;
        chatClientView2.onConnectionOpened(username = withCapture());
        assertTrue(username.equalsIgnoreCase(CLIENT_NAME2));

        String[] usrList;
        chatClientView1.onUpdateChatUserList(usrList = withCapture());
        assertTrue(Arrays.asList(usrList).contains(CLIENT_NAME1));
        assertTrue(Arrays.asList(usrList).contains(CLIENT_NAME2));
        chatClientView2.onUpdateChatUserList(usrList = withCapture());
        assertTrue(Arrays.asList(usrList).contains(CLIENT_NAME1));
        assertTrue(Arrays.asList(usrList).contains(CLIENT_NAME2));

        String message;
        chatClientView1.onReceiveMessage(message = withCapture());
        assertTrue(message.contains(CLIENT_NAME2 + " " + LOGIN_MSG));
        chatClientView2.onReceiveMessage(message = withCapture());
        assertTrue(message.contains(CLIENT_NAME2 + " " + LOGIN_MSG));
      }
    };

    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


    // send message from client1
    chatClientPresenter1.sendMessage(MSG1);
    new FullVerifications() {
      {
        chatClientView1.onSendMessage();

        String message;
        chatClientView1.onReceiveMessage(message = withCapture());
        assertTrue(message.contains(CLIENT_NAME1 + ": " + MSG1));
        chatClientView2.onReceiveMessage(message = withCapture());
        assertTrue(message.contains(CLIENT_NAME1 + ": " + MSG1));
      }
    };

    // send message from client2
    chatClientPresenter2.sendMessage(MSG2);
    new FullVerifications() {
      {
        chatClientView2.onSendMessage();

        String message;
        chatClientView1.onReceiveMessage(message = withCapture());
        assertTrue(message.contains(CLIENT_NAME2 + ": " + MSG2));
        chatClientView2.onReceiveMessage(message = withCapture());
        assertTrue(message.contains(CLIENT_NAME2 + ": " + MSG2));
      }
    };

    chatClientPresenter1.closeConnection();

    new FullVerifications() {
      {
        chatClientView1.onConnectionClosing(anyString);
        chatClientView1.onConnectionClosed(anyString);

        String[] usrList; // check for user list update
        chatClientView2.onUpdateChatUserList(usrList = withCapture());
        assertTrue(Arrays.asList(usrList).contains(CLIENT_NAME2));

        String message; // check for welcome user message
        chatClientView2.onReceiveMessage(message = withCapture());
        assertTrue(message.contains(CLIENT_NAME1 + " " + LOGOUT_MSG));

      }
    };

    chatClientPresenter2.closeConnection();

    new FullVerifications() {
      {
        chatClientView2.onConnectionClosing(anyString);
        chatClientView2.onConnectionClosed(anyString);
      }
    };

    /*
     * try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { // TODO Auto-generated
     * catch block e.printStackTrace(); }
     */
    // if we get other thread exception throw it in current thread
    if (exception.get() != null) {
      throw exception.get();
    }

  }

}
