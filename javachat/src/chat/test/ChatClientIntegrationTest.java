package chat.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import chat.base.ClientPresenter;
import chat.base.Presenter;
import chat.base.View;
import chat.client.mvp.swing.ClientViewSwing;
import chat.server.ClientHandler;
import chat.server.Server;
import mockit.Capturing;
import mockit.Expectations;
import mockit.Verifications;

class ChatClientIntegrationTest {

	private static final long LATCH_OPERATION_TIMEOUT_SEC = 3L;
	private static final int OPERATION_DELAY = 100;
	private static final String ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE = "The client did not receive a message ";
	private static final String CHAT_MSG_1 = "hello";
	private static final String CHAT_MSG_2 = "message2";
	private static final String CHAT_USERNAME1 = "user1";
	private static final String CHAT_USERNAME2 = "user2";
	private static final String ERR_USR_NAME_NOT_EQUAL_TO_EXPECTED = "User name not equal to expected.";
	private static final String ERR_USR_LST_DID_NOT_CONTAIN_USR = "User list did not contain expected username.";
	private static final String ERR_DID_NOT_RECEIVE_WLC_MSG = "Did not receive welcome message.";
	private static final String ERR_DID_NOT_RECEIVE_EXT_MSG = "Did not receive exit message.";
	private static final String ERR_TIMEOUT_COUNTDOWN_LATCH = "Timeout greater than " + LATCH_OPERATION_TIMEOUT_SEC
			+ "sec., waiting for CountDownLatch.";
	private Server server;

	@BeforeEach
	void setUp() throws Exception {

		server = new Server();

	}

	@AfterEach
	void tearDown() throws Exception {

		server.stop();

	}

	@Test
	@DisplayName("Sending messages between two clients.")
	void sendMessagesBetweenTwoClientsTest(@Capturing View chatClientView1, @Capturing View chatClientView2)
			throws Throwable {

		// TODO test opening session in already opened session

		final AtomicReference<Throwable> exception = new AtomicReference<>();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(final Thread t, final Throwable e) {
				exception.compareAndSet(null, e);
			}
		});

		final ClientPresenter clientPresenter1 = new ClientPresenter();
		final ClientPresenter clientPresenter2 = new ClientPresenter();

		clientPresenter1.setView(chatClientView1);
		chatClientView1.setPresenter(clientPresenter1);

		clientPresenter2.setView(chatClientView2);
		chatClientView2.setPresenter(clientPresenter2);

		clientPresenter1.openConnection(CHAT_USERNAME1);
		// TODO Remove the timeout, use awaitility instead
		// TODO Find out why if remove timeout client did not receive message?
		TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

		new Verifications() {
			{ // check for normal session opening command sequence for client 1

				chatClientView1.onConnectionOpening(anyString);

				String actualUsername;
				String expectedUsername = CHAT_USERNAME1;
				chatClientView1.onConnectionOpened(actualUsername = withCapture());
				assertTrue(actualUsername.equals(expectedUsername), ERR_USR_NAME_NOT_EQUAL_TO_EXPECTED);

				String[] actualUserList;
				chatClientView1.onUpdateChatUserList(actualUserList = withCapture());
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);

				String actualMessage;
				String expectedMessage = CHAT_USERNAME1 + " " + ClientHandler.MSG_WLC_USR;
				chatClientView1.onReceiveMessage(actualMessage = withCapture());
				assertTrue(actualMessage.contains(expectedMessage), ERR_DID_NOT_RECEIVE_WLC_MSG);
			}
		};

		// connect client 2 and check normal command sequence for client1 and client2
		clientPresenter2.openConnection(CHAT_USERNAME2);
		TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

		new Verifications() {
			{
				chatClientView2.onConnectionOpening(anyString);

				String actualUsername;
				String expectedUsername = CHAT_USERNAME2;
				chatClientView2.onConnectionOpened(actualUsername = withCapture());
				assertTrue(actualUsername.equals(expectedUsername), ERR_USR_NAME_NOT_EQUAL_TO_EXPECTED);

				String[] actualUserList;
				chatClientView1.onUpdateChatUserList(actualUserList = withCapture());
				expectedUsername = CHAT_USERNAME1;
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);
				expectedUsername = CHAT_USERNAME2;
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);

				chatClientView2.onUpdateChatUserList(actualUserList = withCapture());
				expectedUsername = CHAT_USERNAME1;
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);
				expectedUsername = CHAT_USERNAME2;
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);

				String actualMessage;
				String expectedMessage = CHAT_USERNAME2 + " " + ClientHandler.MSG_WLC_USR;
				chatClientView1.onReceiveMessage(actualMessage = withCapture());
				assertTrue(actualMessage.contains(expectedMessage), ERR_DID_NOT_RECEIVE_WLC_MSG);
				chatClientView2.onReceiveMessage(actualMessage = withCapture());
				assertTrue(actualMessage.contains(expectedMessage), ERR_DID_NOT_RECEIVE_WLC_MSG);

			}
		};

		clientPresenter1.sendCommand(CHAT_MSG_1);
		TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

		new Verifications() {
			{
				chatClientView1.onSendMessage();
				times = 1;

				String actualMessage;
				String expectedMessage = CHAT_MSG_1;
				chatClientView1.onReceiveMessage(actualMessage = withCapture());
				assertTrue(actualMessage.contains(expectedMessage),
						ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + " Instead got " + actualMessage);

				chatClientView2.onReceiveMessage(actualMessage = withCapture());
				assertTrue(actualMessage.contains(expectedMessage),
						ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + " Instead got " + actualMessage);
			}
		};

		clientPresenter2.sendCommand(CHAT_MSG_2);
		TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

		new Verifications() {
			{
				chatClientView2.onSendMessage();
				times = 1;

				String actualMessage;
				String expectedMessage = CHAT_MSG_2;
				chatClientView1.onReceiveMessage(actualMessage = withCapture());
				assertTrue(actualMessage.contains(expectedMessage),
						ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + " Instead got " + actualMessage);

				chatClientView2.onReceiveMessage(actualMessage = withCapture());
				assertTrue(actualMessage.contains(expectedMessage),
						ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + " Instead got " + actualMessage);
			}
		};

		clientPresenter1.closeConnection();
		TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

		new Verifications() {
			{
				chatClientView1.onConnectionClosing(anyString);
				times = 1;
				chatClientView1.onConnectionClosed(anyString);
				times = 1;

				String[] actualUserList;
				String expectedUsername = CHAT_USERNAME2;
				chatClientView2.onUpdateChatUserList(actualUserList = withCapture());
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);

				String actualMessage;
				String expectedMessage = CHAT_USERNAME1 + " " + ClientHandler.MSG_EXIT_USR;

				chatClientView2.onReceiveMessage(actualMessage = withCapture());
				assertTrue(actualMessage.contains(expectedMessage),
						ERR_DID_NOT_RECEIVE_EXT_MSG + " Instead got " + actualMessage);

			}
		};

		clientPresenter2.closeConnection();
		TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

		new Verifications() {
			{
				chatClientView2.onConnectionClosing(anyString);
				times = 1;
				chatClientView2.onConnectionClosed(anyString);
				times = 1;
			}
		};

		// if we get other thread exception throw it in current thread
		if (exception.get() != null) {
			throw exception.get();
		}
	}

	@DisplayName("Start the client, connect to the server, send a \"Hello\" message then disconnect.")
	@Test
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

		// Partial mocking ClientViewSwingTest instance
		ClientViewSwingTest clientView = new ClientViewSwingTest();
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

		final ClientPresenter clientPresenter = new ClientPresenter();

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

		// if we get other thread exception throw it in current thread
		if (exception.get() != null) {
			throw exception.get();
		}

	}

	@Test
	@DisplayName("Sending messages between two clients. With countDownLatch.")
	void sendMessagesBetweenTwoClientsWithCountDownTest() throws Throwable {

		// TODO test opening session in already opened session

		final AtomicReference<Throwable> exception = new AtomicReference<>();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(final Thread t, final Throwable e) {
				exception.compareAndSet(null, e);
			}
		});

		final ClientPresenter clientPresenter1 = new ClientPresenter();
		final ClientPresenter clientPresenter2 = new ClientPresenter();

		boolean isCountDownLatchZero = false;

		ClientViewSwing anyClientView = new ClientViewSwingTest();

		// Indicate which methods are being mocked.
		new Expectations(ClientViewSwingTest.class) {
			{
				anyClientView.onConnectionOpening(anyString);
				anyClientView.onConnectionOpened(anyString);
				anyClientView.onUpdateChatUserList((String[]) any);
				anyClientView.onSendMessage();
				anyClientView.onConnectionClosing(anyString);
				//anyClientView.setPresenter((Presenter) any);
			}
		};

		// Partial mocking ClientViewSwingTest class and instance
		ClientViewSwingTest clientView1 = new ClientViewSwingTest();
		ClientViewSwingTest clientView2 = new ClientViewSwingTest();

		CountDownLatch latch1 = new CountDownLatch(1);
		clientView1.setLatch(latch1);
		CountDownLatch latch2 = new CountDownLatch(1);

		clientPresenter1.setView(clientView1);
		// clientView1.setPresenter(clientPresenter1);
		clientPresenter2.setView(clientView2);
		// clientView2.setPresenter(clientPresenter2);

		clientPresenter1.openConnection(CHAT_USERNAME1);
		isCountDownLatchZero = latch1.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
		assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

		// TODO Remove the timeout, use awaitility instead
		// TODO Find out why if remove timeout client did not receive message?
		// TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

		new Verifications() {
			{ // check for normal session opening command sequence for client 1

				clientView1.onConnectionOpening(anyString);

				String actualUsername;
				String expectedUsername = CHAT_USERNAME1;
				clientView1.onConnectionOpened(actualUsername = withCapture());
				assertTrue(actualUsername.equals(expectedUsername), ERR_USR_NAME_NOT_EQUAL_TO_EXPECTED);

				String[] actualUserList;
				clientView1.onUpdateChatUserList(actualUserList = withCapture());
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);

				String actualMessage;
				String expectedMessage = CHAT_USERNAME1 + " " + ClientHandler.MSG_WLC_USR;
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

		// TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

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
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);
				expectedUsername = CHAT_USERNAME2;
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);

				clientView2.onUpdateChatUserList(actualUserList = withCapture());
				expectedUsername = CHAT_USERNAME1;
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);
				expectedUsername = CHAT_USERNAME2;
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);

				String actualMessage;
				String expectedMessage = CHAT_USERNAME2 + " " + ClientHandler.MSG_WLC_USR;
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
		// TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

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
		// TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

		/*System.out.println(
				"ChatClientIntegrationTest.sendMessagesBetweenTwoClientsWithCountDownTest() clientPresenter2.getView():"
						+ clientPresenter2.getView().hashCode() + ", clientView2:" + clientView2.hashCode()
						+ ", clientView1:" + clientView1.hashCode());
*/
		new Verifications() {
			{
				// TODO check why there are two invocation
				clientView2.onSendMessage(); times = 2;

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
//		TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

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
				assertTrue(Arrays.asList(actualUserList).contains(expectedUsername), ERR_USR_LST_DID_NOT_CONTAIN_USR);

				String actualMessage;
				String expectedMessage = CHAT_USERNAME1 + " " + ClientHandler.MSG_EXIT_USR;

				clientView2.onReceiveMessage(actualMessage = withCapture());
				assertTrue(actualMessage.contains(expectedMessage),
						ERR_DID_NOT_RECEIVE_EXT_MSG + " Instead got " + actualMessage);

			}
		};

		latch2 = new CountDownLatch(1);
		clientView2.setLatch(latch2);
		
		clientPresenter2.closeConnection();
//		TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);
		isCountDownLatchZero = latch2.await(LATCH_OPERATION_TIMEOUT_SEC, TimeUnit.SECONDS);
		assertTrue(ERR_TIMEOUT_COUNTDOWN_LATCH, isCountDownLatchZero);

		new Verifications() {
			{
				// TODO check why there are two invocation
				clientView2.onConnectionClosing(anyString);
				times = 2;
				clientView2.onConnectionClosed(anyString);
				times = 2;
			}
		};

		// if we get other thread exception throw it in current thread
		if (exception.get() != null) {
			throw exception.get();
		}
	}
}
