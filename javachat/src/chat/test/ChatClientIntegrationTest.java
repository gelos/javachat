package chat.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import chat.base.ClientPresenter;
import chat.base.View;
import chat.client.mvp.swing.ClientViewSwing;
import chat.server.ClientHandler;
import chat.server.Server;
import mockit.Capturing;
import mockit.Verifications;

class ChatClientIntegrationTest {

	private static final int OPERATION_DELAY = 100;
	private static final String ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE = "The client did not receive a message ";
	private static final String CHAT_MSG_1 = "hello";
	private static final String CHAT_MSG_2 = "message2";
	private static final String CHAT_USERNAME1 = "user1";
	private static final String CHAT_USERNAME2 = "user2";
	private static final String ERR_USR_NAME_NOT_EQUAL_TO_EXPECTED = "User name not equal to expected.";
	private static final String ERR_USR_LST_DID_NOT_CONTAIN_USR = "User list did not contain expected username.";
	private static final String ERR_DID_NOT_RECEIVE_WLC_MSG = "Did not receive welcome message.";
	private static final String ERR_DID_NOT_RECEIVE_EXT_MSG = "Did not receive exit message.";;
	private Server server;

	@BeforeEach
	void setUp() throws Exception {

		server = new Server();

	}

	@AfterEach
	void tearDown() throws Exception {

		server.stop();

	}

	@DisplayName("Start the client, connect to the server, send a \"Hello\" message then disconnect.")
	@Test
	void startStopClientTest(@Capturing ClientViewSwing clientView) throws Throwable {

		// set exception handler to throw other thread exceptions in current thread
		final AtomicReference<Throwable> exception = new AtomicReference<>();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(final Thread t, final Throwable e) {
				exception.compareAndSet(null, e);
			}
		});

		final ClientPresenter clientPresenter = new ClientPresenter();

		clientPresenter.setView(clientView);
		clientView.setPresenter(clientPresenter);

		clientPresenter.openConnection(CHAT_USERNAME1);
		// TODO Remove the timeout, use awaitility instead
		// TODO Find out why if remove timeout client did not receive message?
		TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

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

		clientPresenter.sendCommand(CHAT_MSG_1);
		TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);
		
		new Verifications() {
			{
				clientPresenter.getView().onSendMessage();
				times = 1;

				String actualMessage;
				String expectedMessage = CHAT_MSG_1;
				clientPresenter.getView().onReceiveMessage(actualMessage = withCapture());
				assertTrue(actualMessage.contains(expectedMessage),
						ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + actualMessage);

			}
		};

		clientPresenter.closeConnection();
		TimeUnit.MILLISECONDS.sleep(OPERATION_DELAY);

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
						ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + actualMessage);
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
						ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + actualMessage);

				chatClientView2.onReceiveMessage(actualMessage = withCapture());
				assertTrue(actualMessage.contains(expectedMessage),
						ERR_THE_CLIENT_DID_NOT_RECEIVE_A_MESSAGE + actualMessage);
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
}
