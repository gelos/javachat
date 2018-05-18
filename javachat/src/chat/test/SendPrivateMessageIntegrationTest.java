package chat.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static chat.base.CommandName.CMDDLM;
import static chat.base.CommandName.CMDPRVMSG;
import static chat.base.CommandName.CMDUDLM;
import static chat.base.CommandName.CMDULDLM;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import chat.base.ClientPresenter;
import chat.base.Presenter;
import chat.base.View;
import chat.server.Server;
import chat.server.ClientHandler;
import mockit.Capturing;
import mockit.Verifications;

@DisplayName("Send private message ")
class SendPrivateMessageIntegrationTest {
	/**
	 * Logger for this class
	 */
	//private static final Logger loggerDebug = LoggerFactory.getLogger("debug");

	// MAX_NUMBER_OF_CLIENTS must be equal sum number of @Capturing View variables
	public static final int MAX_NUMBERS_OF_USERS = 4;
	public static final String USER_NAME_PREFIX = "client";
	public static final String MESSAGE_PREFIX = "message";

	// To differentiate the sender, recipients, and recipient client, you must
	// declare each variable
	// separately.
	@Capturing
	View senderView;
	@Capturing
	View recipientView1;
	@Capturing
	View recipientView2;
	@Capturing
	View noRecipientView;

	private Server server;

	private Presenter[] chatClientPresenterStorage = new Presenter[MAX_NUMBERS_OF_USERS];

	// The factory method to create presenter and view Swing
	private ClientPresenter createChatClientFactory(String username, View view) {
		ClientPresenter presenter = new ClientPresenter();
		view.setPresenter(presenter);
		presenter.setView(view);
		return presenter;
	}

	@BeforeEach
	void BeforeAll(TestInfo testInfo) throws Exception {

		System.out.println();
		System.out.println("----------------------------------------------------------------------");
		System.out.println(testInfo.getTestClass().get().getSimpleName() + "." + testInfo.getDisplayName());
		System.out.println("----------------------------------------------------------------------");

		server = new Server();

		View view;
		for (int i = 0; i < chatClientPresenterStorage.length; i++) {
			switch (i) {
			case 0:
				view = senderView;
				break;
			case 1:
				view = recipientView1;
				break;
			case 2:
				view = recipientView2;
				break;
			default:
				view = noRecipientView;
				break;
			}
			chatClientPresenterStorage[i] = createChatClientFactory(USER_NAME_PREFIX + i, view);
			//loggerDebug.debug(chatClientPresenterStorage[i].toString());
		}

		// Connect client to server
		int i = 0;
		for (Presenter chatClient : chatClientPresenterStorage) {
			chatClient.openConnection(USER_NAME_PREFIX + i++);
		}

		// Waiting until all chat clients log on to the server. Should be used for
		// stability of the test.
		TimeUnit.MILLISECONDS.sleep(1500);

	}

	@AfterEach
	void AfterAll() throws Exception {

		server.stop();

	}

	private String getFullPrivateMessageRecepientList() {
		String privateMessageRecepientList = "";
		for (int i = 1; i < MAX_NUMBERS_OF_USERS; i++) {
			String userName = USER_NAME_PREFIX + i;
			privateMessageRecepientList += ((i != 1) ? CMDULDLM : "") + userName;
		}
		return privateMessageRecepientList;
	}

	@Nested
	@DisplayName("with normal message")
	class Normal {

		static final int NUMBER_OF_PRIVATE_MSG_RECEPIENTS = MAX_NUMBERS_OF_USERS - 2;

		@Test
		void testNormalMessage() throws InterruptedException {

			// client 0 send message, clients 1-(n-2) must receive it, client n-1 must not
			// receive
			assumeTrue(MAX_NUMBERS_OF_USERS >= 4, "Client number must be >= 4.");

			String privateMessageRecepientList = "";
			for (int i = 1; i <= NUMBER_OF_PRIVATE_MSG_RECEPIENTS; i++) {
				String userName = USER_NAME_PREFIX + i;
				privateMessageRecepientList += ((i != 1) ? CMDULDLM : "") + userName;
			}

			String chatMessage = MESSAGE_PREFIX + 0;
			String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList + CMDUDLM + CMDDLM
					+ chatMessage;
			chatClientPresenterStorage[0].sendCommand(privateCommand);

			new Verifications() {
				{
					chatClientPresenterStorage[0].getView().onSendMessage();
					times = 1;

					String expectedMessage = USER_NAME_PREFIX + 0 + ": " + chatMessage;
					String actualMessage;

					for (int i = 0; i <= NUMBER_OF_PRIVATE_MSG_RECEPIENTS; i++) {
						chatClientPresenterStorage[i].getView().onReceiveMessage(actualMessage = withCapture());
						assertTrue(actualMessage.contains(expectedMessage),
								"Clients in recepient list must receive private message. But received "
										+ actualMessage);
					}

					// Checking that last user not received the private message. The statement
					// 'times = 1'
					// means getting only one welcome login message initiated by sequential clients
					// startup.
					chatClientPresenterStorage[MAX_NUMBERS_OF_USERS - 1].getView()
							.onReceiveMessage(actualMessage = withCapture());
					times = 1;
					assertFalse(actualMessage.contains(expectedMessage),
							"Last client must not receive private message. But received " + actualMessage);

				}
			};
		}
	}

	@Nested
	@DisplayName("with empty user list")
	class EmptyUserList {
		@Test
		void testEmptyUserList() {

			String privateMessageRecepientList = "";
			String chatMessage = MESSAGE_PREFIX + 0;
			String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList + CMDUDLM + CMDDLM
					+ chatMessage;
			chatClientPresenterStorage[0].sendCommand(privateCommand);

			new Verifications() {
				{
					senderView.onSendMessage();
					times = 1;

					String expectedMessage = USER_NAME_PREFIX + 0 + ": " + chatMessage;
					String actualMessage;

					for (int i = 0; i < MAX_NUMBERS_OF_USERS; i++) {
						chatClientPresenterStorage[i].getView().onReceiveMessage(actualMessage = withCapture());
						assertTrue(actualMessage.contains(expectedMessage),
								"All client must receive private message. But received " + actualMessage);
					}
				}
			};
		}
	}

	@Nested
	@DisplayName("with duplicate user names in list")
	class DuplicateUserNames {
		@Test
		void testDuplicateUserNames() {

			String privateMessageRecepientList = getFullPrivateMessageRecepientList();

			String duplicateUser1 = USER_NAME_PREFIX + 1;
			String duplicateUser3 = USER_NAME_PREFIX + 3;
			privateMessageRecepientList += CMDULDLM + duplicateUser1 + CMDULDLM + duplicateUser3;

			String chatMessage = MESSAGE_PREFIX + 0;
			String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList + CMDUDLM + CMDDLM
					+ chatMessage;
			chatClientPresenterStorage[0].sendCommand(privateCommand);

			new Verifications() {
				{

					String expectedMessage = USER_NAME_PREFIX + 0 + ": " + chatMessage;
					String actualMessage;

					for (int i = 0; i < chatClientPresenterStorage.length; i++) {
						chatClientPresenterStorage[i].getView().onReceiveMessage(actualMessage = withCapture());
						assertTrue(actualMessage.contains(expectedMessage),
								"All client must receive private message only once. But received " + actualMessage);
						if (i == 1) {

							// Checking that user1 received the private message only once. The statement
							// 'times =
							// 4' means getting three welcome login messages initiated by sequential clients
							// startup plus one private message.

							times = 4;
						} else if (i == 3) {

							// Checking that user3 received the private message only once. The statement
							// 'times =
							// 2' means getting one welcome login messages initiated by sequential clients
							// startup plus one private message.

							times = 2;
						}
					}
				}
			};
		}
	}

	@Nested
	@DisplayName("with unknown users")
	class UnknownUsers {
		@Test
		void testUnknownUsers() {

			String privateMessageRecepientList = getFullPrivateMessageRecepientList();

			String unknowUser1 = USER_NAME_PREFIX + (MAX_NUMBERS_OF_USERS + 1);
			String unknowUser2 = USER_NAME_PREFIX + (MAX_NUMBERS_OF_USERS + 100);
			privateMessageRecepientList = unknowUser1 + CMDULDLM + privateMessageRecepientList + CMDULDLM + unknowUser2;

			String chatMessage = MESSAGE_PREFIX + 0;
			String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList + CMDUDLM + CMDDLM
					+ chatMessage;
			chatClientPresenterStorage[0].sendCommand(privateCommand);

			new Verifications() {
				{
					String expectedMessage = ClientHandler.ERR_USRS_NOT_FOUND + unknowUser1 + CMDULDLM + unknowUser2;
					Object actualObject = null;

					chatClientPresenterStorage[0].getView().showErrorWindow(actualObject = withCapture(), anyString);
					assertTrue(actualObject.toString().contains(expectedMessage),
							"Message \"" + ClientHandler.ERR_USRS_NOT_FOUND + "\" not received.");
				}
			};
		}
	}

	@Nested
	@DisplayName("with different case sensitivity in all usernames")
	class CaseSensitivity {
		@Test
		void testCaseSensitivity() {

			String privateMessageRecepientList = "";
			String privateMessageRecepientListCaseSensitivity = "";
			for (int i = 1; i < MAX_NUMBERS_OF_USERS; i++) {
				String userName = USER_NAME_PREFIX + i;
				String userNameCaseSensitivity = Character.toUpperCase(userName.charAt(0)) + userName.substring(1);
				privateMessageRecepientList += ((i != 1) ? CMDULDLM : "") + userNameCaseSensitivity + CMDULDLM
						+ userName;
				privateMessageRecepientListCaseSensitivity += ((i != 1) ? CMDULDLM : "") + userNameCaseSensitivity;
			}

			String chatMessage = MESSAGE_PREFIX + 0;
			String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList + CMDUDLM + CMDDLM
					+ chatMessage;

			chatClientPresenterStorage[0].sendCommand(privateCommand);

			final String innerPrivateMessageRecepientList = privateMessageRecepientListCaseSensitivity;
			new Verifications() {
				{
					String expectedErrorMessage = ClientHandler.ERR_USRS_NOT_FOUND + innerPrivateMessageRecepientList;
					Object actualObject = null;

					chatClientPresenterStorage[0].getView().showErrorWindow(actualObject = withCapture(), anyString);

					assertTrue(actualObject.toString().contains(expectedErrorMessage),
							"Message \"" + ClientHandler.ERR_USRS_NOT_FOUND + "\" not received.");

					String expectedMessage = USER_NAME_PREFIX + 0 + ": " + chatMessage;
					String actualMessage;

					for (int i = 0; i < MAX_NUMBERS_OF_USERS; i++) {
						chatClientPresenterStorage[i].getView().onReceiveMessage(actualMessage = withCapture());
						assertTrue(actualMessage.contains(expectedMessage),
								"All client must receive private message. But received " + actualMessage);
					}
				}
			};
		}
	}

	@Nested
	@DisplayName("with additional spaces in usernames")
	class AdditionalSpaces {
		@Test
		void testAdditionalSpaces() {

			String privateMessageRecepientList = USER_NAME_PREFIX + 1 + CMDULDLM + CMDDLM + CMDDLM + USER_NAME_PREFIX
					+ 3;

			String chatMessage = MESSAGE_PREFIX + 0;
			String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList + CMDUDLM + CMDDLM
					+ chatMessage;
			// System.out
			// .println("SendPrivateMessageIntegrationTest.AdditionalSpaces.testwithCaseSensitivity()"
			// + privateCommand);
			chatClientPresenterStorage[0].sendCommand(privateCommand);

			new Verifications() {
				{
					String expectedMessage = USER_NAME_PREFIX + 0 + ": " + chatMessage;
					String actualMessage;

					chatClientPresenterStorage[1].getView().onReceiveMessage(actualMessage = withCapture());
					assertTrue(actualMessage.contains(expectedMessage),
							"All client must receive private message. But received " + actualMessage);

					chatClientPresenterStorage[3].getView().onReceiveMessage(actualMessage = withCapture());
					assertTrue(actualMessage.contains(expectedMessage),
							"All client must receive private message. But received " + actualMessage);

				}
			};
		}
	}
}
