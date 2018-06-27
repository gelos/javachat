package chat.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import chat.base.Constants;
import chat.client.mvp.presenter.Presenter;
import chat.client.mvp.presenter.PresenterFabric;
import chat.server.Server;
import mockit.Expectations;
import mockit.Verifications;

class ChatClientIntegrationTest {

  public static final int MAX_TEST_REPEAT = 1;
  private static final long LATCH_OPERATION_TIMEOUT_SEC = 3L;
  private static final String ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE =
      "The client did not receive a message ";
  private static final String CHAT_MSG_1 = "hello";
  private static final String CHAT_MSG_2 = "message2";
  private static final String CHAT_USERNAME1 = "user1";
  private static final String CHAT_USERNAME2 = "user2";
  private static final String ERR_USR_NAME_NOT_EQUAL_TO_EXPECTED =
      "User name not equal to expected.";
  private static final String ERR_USR_LST_DID_NOT_CONTAIN_USR =
      "User list did not contain expected username.";
  private static final String ERR_DID_NOT_RECEIVE_WLC_MSG = "Did not receive welcome message.";
  private static final String ERR_DID_NOT_RECEIVE_EXT_MSG = "Did not receive exit message.";
  private static final String ERR_TIMEOUT_COUNTDOWN_LATCH =
      "Timeout greater than " + LATCH_OPERATION_TIMEOUT_SEC + "sec., waiting for CountDownLatch.";
  private Server server;

  @BeforeEach
  void setUp() throws Exception {

    server = new Server();

  }

  @AfterEach
  void tearDown() throws Exception {

    server.stop();

  }

  @RepeatedTest(value = MAX_TEST_REPEAT,
      name = "{displayName} {currentRepetition}/{totalRepetitions}")
  @DisplayName("Start the client, connect to the server, send a \"Hello\" message then disconnect.")
  void startStopClientTest() throws Throwable {

    // set exception handler to throw other thread exceptions in current thread
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    boolean isCountDownLatchZero = false;

    // Partial mocking ClientViewSwingWithLatch instance
    ClientViewSwingWithLatch clientView = new ClientViewSwingWithLatch();
    CountDownLatch latch = new CountDownLatch(1);
    clientView.setLatch(latch);

    // Indicate which methods are being mocked.
    new Expectations(clientView) {
      {
        clientView.onConnectionOpening(anyString);
        clientView.onConnectionOpened(anyString);
        clientView.onUpdateChatUserList((String[]) any);
        clientView.onSendMessage();
        clientView.onConnectionClosing(anyString);
      }
    };

    final Presenter clientPresenter = PresenterFabric.createPresenter();

    clientPresenter.setView(clientView);
    clientView.setPresenter(clientPresenter);

    clientPresenter.openConnection(CHAT_USERNAME1);

    isCountDownLatchZero = latch.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

    new Verifications() {
      {
        clientPresenter.getView().onConnectionOpening(anyString);
        times = 1;
        clientPresenter.getView().onConnectionOpened(anyString);
        times = 1;

        // Check for update user list command
        clientPresenter.getView().onUpdateChatUserList((String[]) any);
        times = 1;

        // Check for user login message
        clientPresenter.getView().onReceiveMessage(anyString);
        times = 1;

      }
    };

    latch = new CountDownLatch(1);
    clientView.setLatch(latch);
    clientPresenter.sendCommand(CHAT_MSG_1);
    isCountDownLatchZero = latch.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

    new Verifications() {
      {
        clientPresenter.getView().onSendMessage();
        times = 1;

        String actualMessage;
        String expectedMessage = CHAT_MSG_1;
        clientPresenter.getView().onReceiveMessage(actualMessage = withCapture());

        assertTrue(actualMessage.contains(expectedMessage),
            ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + " Instead got " + actualMessage);

      }
    };

    latch = new CountDownLatch(1);
    clientView.setLatch(latch);
    clientPresenter.closeConnection();
    isCountDownLatchZero = latch.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

    new Verifications() {
      {
        clientPresenter.getView().onConnectionClosing(anyString);
        times = 1;
        clientPresenter.getView().onConnectionClosed(anyString);
        times = 1;

      }
    };

    // fail();

    // if we get other thread exception throw it in current thread
    if (exception.get() != null) {
      throw exception.get();
    }

  }

  @RepeatedTest(value = MAX_TEST_REPEAT,
      name = "{displayName} {currentRepetition}/{totalRepetitions}")
  @DisplayName("Sending messages between two clients.")
  void sendMessagesBetweenTwoClientsTest() throws Throwable {

    // TODO test opening session in already opened session

    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    final Presenter clientPresenter1 = PresenterFabric.createPresenter();
    final Presenter clientPresenter2 = PresenterFabric.createPresenter();

    boolean isCountDownLatchZero = false;

    // Partial mocking ClientViewSwingWithLatch class and instance
    ClientViewSwingWithLatch clientView1 = new ClientViewSwingWithLatch();
    ClientViewSwingWithLatch clientView2 = new ClientViewSwingWithLatch();

    // Indicate which methods are being mocked on concrete instance.
    new Expectations(clientView1) {
      {
        clientView1.onConnectionOpening(anyString);
        clientView1.onConnectionOpened(anyString);
        clientView1.onUpdateChatUserList((String[]) any);
        clientView1.onSendMessage();
        clientView1.onConnectionClosing(anyString);
      }
    };

    // Indicate which methods are being mocked on concrete instance.
    new Expectations(clientView2) {
      {
        clientView2.onConnectionOpening(anyString);
        clientView2.onConnectionOpened(anyString);
        clientView2.onUpdateChatUserList((String[]) any);
        clientView1.onSendMessage();
        clientView2.onConnectionClosing(anyString);
      }
    };

    CountDownLatch latch1 = new CountDownLatch(1);
    clientView1.setLatch(latch1);
    CountDownLatch latch2 = new CountDownLatch(1);

    clientPresenter1.setView(clientView1);
    clientPresenter2.setView(clientView2);

    clientPresenter1.openConnection(CHAT_USERNAME1);
    // Waiting for clientView1 to complete onReceiveMessage
    isCountDownLatchZero = latch1.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

    new Verifications() {
      { // check for normal session opening command sequence for client 1

        clientView1.onConnectionOpening(anyString);

        String actualUsername;
        String expectedUsername = CHAT_USERNAME1;
        clientView1.onConnectionOpened(actualUsername = withCapture());
        assertTrue(actualUsername.equals(expectedUsername), ERR_USR_NAME_NOT_EQUAL_TO_EXPECTED);

        String[] actualUserList;
        clientView1.onUpdateChatUserList(actualUserList = withCapture());
        assertTrue(Arrays.asList(actualUserList).contains(expectedUsername),
            ERR_USR_LST_DID_NOT_CONTAIN_USR);

        String actualMessage;
        String expectedMessage = CHAT_USERNAME1 + " " + Constants.MSG_WLC_USR;
        clientView1.onReceiveMessage(actualMessage = withCapture());
        assertTrue(actualMessage.contains(expectedMessage), ERR_DID_NOT_RECEIVE_WLC_MSG);
      }
    };

    latch1 = new CountDownLatch(1);
    clientView1.setLatch(latch1);
    latch2 = new CountDownLatch(1);
    clientView2.setLatch(latch2);
    // connect client 2 and check normal command sequence for client1 and client2
    clientPresenter2.openConnection(CHAT_USERNAME2);

    // Waiting for clientView1 and ClientView2 to complete onReceiveMessage
    isCountDownLatchZero = latch1.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);
    isCountDownLatchZero = latch2.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

    new Verifications() {
      {
        clientView2.onConnectionOpening(anyString);

        String actualUsername;
        String expectedUsername = CHAT_USERNAME2;
        clientView2.onConnectionOpened(actualUsername = withCapture());
        assertTrue(actualUsername.equals(expectedUsername), ERR_USR_NAME_NOT_EQUAL_TO_EXPECTED);

        String[] actualUserList;
        clientView1.onUpdateChatUserList(actualUserList = withCapture());
        expectedUsername = CHAT_USERNAME1;
        assertTrue(Arrays.asList(actualUserList).contains(expectedUsername),
            ERR_USR_LST_DID_NOT_CONTAIN_USR);
        expectedUsername = CHAT_USERNAME2;
        assertTrue(Arrays.asList(actualUserList).contains(expectedUsername),
            ERR_USR_LST_DID_NOT_CONTAIN_USR);

        clientView2.onUpdateChatUserList(actualUserList = withCapture());
        expectedUsername = CHAT_USERNAME1;
        assertTrue(Arrays.asList(actualUserList).contains(expectedUsername),
            ERR_USR_LST_DID_NOT_CONTAIN_USR);
        expectedUsername = CHAT_USERNAME2;
        assertTrue(Arrays.asList(actualUserList).contains(expectedUsername),
            ERR_USR_LST_DID_NOT_CONTAIN_USR);

        String actualMessage;
        String expectedMessage = CHAT_USERNAME2 + " " + Constants.MSG_WLC_USR;
        clientView1.onReceiveMessage(actualMessage = withCapture());
        assertTrue(actualMessage.contains(expectedMessage), ERR_DID_NOT_RECEIVE_WLC_MSG);
        clientView2.onReceiveMessage(actualMessage = withCapture());
        assertTrue(actualMessage.contains(expectedMessage), ERR_DID_NOT_RECEIVE_WLC_MSG);

      }
    };

    latch1 = new CountDownLatch(1);
    clientView1.setLatch(latch1);
    latch2 = new CountDownLatch(1);
    clientView2.setLatch(latch2);

    clientPresenter1.sendCommand(CHAT_MSG_1);

    // Waiting for clientView1 and ClientView2 to complete onReceiveMessage
    isCountDownLatchZero = latch1.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);
    isCountDownLatchZero = latch2.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

    new Verifications() {
      {
        clientView1.onSendMessage();
        times = 1;

        String actualMessage;
        String expectedMessage = CHAT_MSG_1;
        clientView1.onReceiveMessage(actualMessage = withCapture());
        assertTrue(actualMessage.contains(expectedMessage),
            ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + " Instead got " + actualMessage);

        clientView2.onReceiveMessage(actualMessage = withCapture());
        assertTrue(actualMessage.contains(expectedMessage),
            ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + " Instead got " + actualMessage);
      }
    };

    latch1 = new CountDownLatch(1);
    clientView1.setLatch(latch1);
    latch2 = new CountDownLatch(1);
    clientView2.setLatch(latch2);

    clientPresenter2.sendCommand(CHAT_MSG_2);

    // Waiting for clientView1 and ClientView2 to complete onReceiveMessage
    isCountDownLatchZero = latch1.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);
    isCountDownLatchZero = latch2.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

    new Verifications() {
      {
        clientView2.onSendMessage();
        times = 1;

        String actualMessage;
        String expectedMessage = CHAT_MSG_2;
        clientView1.onReceiveMessage(actualMessage = withCapture());
        assertTrue(actualMessage.contains(expectedMessage),
            ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + " Instead got " + actualMessage);

        clientView2.onReceiveMessage(actualMessage = withCapture());
        assertTrue(actualMessage.contains(expectedMessage),
            ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + " Instead got " + actualMessage);
      }
    };

    latch1 = new CountDownLatch(1);
    clientView1.setLatch(latch1);
    latch2 = new CountDownLatch(1);
    clientView2.setLatch(latch2);

    clientPresenter1.closeConnection();

    // Waiting for clientView1 and ClientView2 to complete onReceiveMessage
    isCountDownLatchZero = latch1.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);
    isCountDownLatchZero = latch2.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

    new Verifications() {
      {
        clientView1.onConnectionClosing(anyString);
        times = 1;
        clientView1.onConnectionClosed(anyString);
        times = 1;

        String[] actualUserList;
        String expectedUsername = CHAT_USERNAME2;
        clientView2.onUpdateChatUserList(actualUserList = withCapture());
        assertTrue(Arrays.asList(actualUserList).contains(expectedUsername),
            ERR_USR_LST_DID_NOT_CONTAIN_USR);

        String actualMessage;
        String expectedMessage = CHAT_USERNAME1 + " " + Constants.MSG_EXIT_USR;

        clientView2.onReceiveMessage(actualMessage = withCapture());
        assertTrue(actualMessage.contains(expectedMessage),
            ERR_DID_NOT_RECEIVE_EXT_MSG + " Instead got " + actualMessage);

      }
    };

    latch2 = new CountDownLatch(1);
    clientView2.setLatch(latch2);

    clientPresenter2.closeConnection();
    // Waiting for ClientView2 to complete onConnectionClosed
    isCountDownLatchZero = latch2.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

    new Verifications() {
      {
        clientView2.onConnectionClosing(anyString);
        times = 1;
        clientView2.onConnectionClosed(anyString);
        times = 1;
      }
    };

    // if we get other thread exception throw it in current thread
    if (exception.get() != null) {
      throw exception.get();
    }
  }

  // @RepeatedTest(value = MAX_TEST_REPEAT, name = "{displayName}
  // {currentRepetition}/{totalRepetitions}")
  @Test
  @DisplayName("Connecting to a server with an existing username.")
  void connectWithExistingUserNameTest() throws Throwable {

    // set exception handler to throw other thread exceptions in current thread
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    final Presenter clientPresenter1 = PresenterFabric.createPresenter();
    final Presenter clientPresenter2 = PresenterFabric.createPresenter();

    boolean isCountDownLatchZero = false;

    // Partial mocking ClientViewSwingWithLatch class and instance
    ClientViewSwingWithLatch clientView1 = new ClientViewSwingWithLatch();
    ClientViewSwingWithLatch clientView2 = new ClientViewSwingWithLatch();

    // Indicate which methods are being mocked on concrete instance.
    new Expectations(clientView1) {
      {
        clientView1.onConnectionOpening(anyString);
        clientView1.onConnectionOpened(anyString);
        clientView1.onUpdateChatUserList((String[]) any);
        clientView1.onSendMessage();
        clientView1.onConnectionClosing(anyString);
      }
    };

    // Indicate which methods are being mocked on concrete instance.
    new Expectations(clientView2) {
      {
        clientView2.onConnectionOpening(anyString);
        clientView2.onConnectionOpened(anyString);
        clientView2.onUpdateChatUserList((String[]) any);
        clientView1.onSendMessage();
        clientView2.onConnectionClosing(anyString);
        clientView2.showErrorWindow(any, anyString);
      }
    };

    CountDownLatch latch1 = new CountDownLatch(1);
    clientView1.setLatch(latch1);

    clientPresenter1.setView(clientView1);
    clientPresenter2.setView(clientView2);

    clientPresenter1.openConnection(CHAT_USERNAME1);
    // Waiting for clientView1 to complete onReceiveMessage
    isCountDownLatchZero = latch1.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

    CountDownLatch latch2 = new CountDownLatch(1);
    clientView2.setLatch(latch2);
    // Waiting for clientView2 to complete onConnectionclosed()
    clientPresenter2.openConnection(CHAT_USERNAME1);
    isCountDownLatchZero = latch2.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
    assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

    fail("The test is not completed.");

    new Verifications() {
      {
        // clientView2.onConnectionClosing(anyString); times = 2;
        String actualMessage;
        String expectedMessage = Constants.ERR_NAME_EXISTS_MSG;
        clientView2.showErrorWindow(actualMessage = withCapture(), anyString);
        assertTrue(actualMessage.contains(expectedMessage),
            ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + " Instead got " + actualMessage);
        times = 1;
        // clientPresenter2.getView().onConnectionClosing(anyString); times = 3;
        // clientView1.onConnectionClosing(anyString);
        // clientView2.onConnectionClosing(anyString);
        clientPresenter2.getView().onConnectionClosed(anyString);
        times = 1;
      }
    };

    // if we get other thread exception throw it in current thread
    if (exception.get() != null) {
      throw exception.get();
    }

  }
}
