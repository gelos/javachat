package chat.test;

import static chat.base.CommandName.CMDDLM;
import static chat.base.CommandName.CMDPRVMSG;
import static chat.base.CommandName.CMDUDLM;
import static chat.base.CommandName.CMDULDLM;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import chat.base.ChatClientPresenter;
import chat.base.Presenter;
import chat.base.View;
import chat.server.ChatServer;
import chat.server.CommandHandler;
import mockit.Capturing;
import mockit.FullVerifications;
import mockit.Verifications;

@DisplayName("Send private message ")
class SendPrivateMessageIntegrationTest {

  // MAX_NUMBER_OF_CLIENTS must be equal sum number of @Capturing View variables
  public static final int MAX_NUMBERS_OF_USERS = 4;
  public static final String USER_NAME_PREFIX = "client";
  public static final String MESSAGE_PREFIX = "message";

  @Capturing
  View senderView;
  @Capturing
  View receiverView1;
  @Capturing
  View receiverView2;
  @Capturing
  View notReceiverView;

  private ChatServer chatServer;

  // Chat clients storage
  private Presenter[] chatClients = new Presenter[MAX_NUMBERS_OF_USERS];

  // Factory method to create presenter and view Swing
  private ChatClientPresenter createChatClientFactory(String username, View view) {
    ChatClientPresenter presenter = new ChatClientPresenter();
    view.setPresenter(presenter);
    presenter.setView(view);
    return presenter;
  }

  @BeforeEach
  void BeforeAll() throws Exception {

    // Create server
    chatServer = new ChatServer();
    int timeout = 1;

    // Wait while chatServer started
    while (!chatServer.isStarted() && (timeout <= 10)) {
      TimeUnit.SECONDS.sleep(1);
      timeout++;
    }

    View view;
    // Create clients
    for (int i = 0; i < chatClients.length; i++) {
      switch (i) {
        case 0:
          view = senderView;
          break;
        case 1:
          view = receiverView1;
          break;
        case 2:
          view = receiverView2;
          break;
        default:
          view = notReceiverView;
          break;
      }
      chatClients[i] = createChatClientFactory(USER_NAME_PREFIX + i, view);
    }

    // Connect client to server
    for (int i = 0; i < chatClients.length; i++) {
      chatClients[i].openConnection(USER_NAME_PREFIX + i);
    }

  }

  @AfterEach
  void AfterAll() throws Exception {

    // Stop server
    chatServer.stop();

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

      // client 0 send message, clients 1-(n-2) must receive it, client n-1 must not receive
      assumeTrue(MAX_NUMBERS_OF_USERS >= 4, "Client number must be >= 4.");

      String privateMessageRecepientList = "";
      for (int i = 1; i <= NUMBER_OF_PRIVATE_MSG_RECEPIENTS; i++) {
        String userName = USER_NAME_PREFIX + i;
        privateMessageRecepientList += ((i != 1) ? CMDULDLM : "") + userName;
      }

      String chatMessage = MESSAGE_PREFIX + 0;
      String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList
          + CMDUDLM + CMDDLM + chatMessage;
      chatClients[0].sendMessage(privateCommand);

      new Verifications() {
        {
          chatClients[0].getView().onSendMessage();
          times = 1;

          String expectedMessage = USER_NAME_PREFIX + 0 + ": " + chatMessage;
          String actualMessage;

          for (int i = 0; i <= NUMBER_OF_PRIVATE_MSG_RECEPIENTS; i++) {
            chatClients[i].getView().onReceiveMessage(actualMessage = withCapture());
            assertTrue(actualMessage.contains(expectedMessage),
                "Clients in recepient list must receive private message.");
          }

          // Checking that last user not received the private message. The statement 'times = 1'
          // means getting only one welcome login message initiated by sequential clients startup.
          chatClients[MAX_NUMBERS_OF_USERS - 1].getView()
              .onReceiveMessage(actualMessage = withCapture());
          times = 1;
          assertFalse(actualMessage.contains(expectedMessage),
              "Last client must not receive private message.");

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
      String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList
          + CMDUDLM + CMDDLM + chatMessage;
      chatClients[0].sendMessage(privateCommand);

      new Verifications() {
        {
          senderView.onSendMessage();
          times = 1;

          String expectedMessage = USER_NAME_PREFIX + 0 + ": " + chatMessage;
          String actualMessage;

          for (int i = 0; i < MAX_NUMBERS_OF_USERS; i++) {
            chatClients[i].getView().onReceiveMessage(actualMessage = withCapture());
            assertTrue(actualMessage.contains(expectedMessage),
                "All client must receive private message.");
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
      String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList
          + CMDUDLM + CMDDLM + chatMessage;
      chatClients[0].sendMessage(privateCommand);

      new Verifications() {
        {

          String expectedMessage = USER_NAME_PREFIX + 0 + ": " + chatMessage;
          String actualMessage;

          for (int i = 0; i < chatClients.length; i++) {
            chatClients[i].getView().onReceiveMessage(actualMessage = withCapture());
            assertTrue(actualMessage.contains(expectedMessage),
                "All client must receive private message only once.");
            if (i == 1) {

              // Checking that user1 received the private message only once. The statement 'times =
              // 4' means getting three welcome login messages initiated by sequential clients
              // startup plus one private message.

              times = 4;
            } else if (i == 3) {

              // Checking that user3 received the private message only once. The statement 'times =
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
      privateMessageRecepientList =
          unknowUser1 + CMDULDLM + privateMessageRecepientList + CMDULDLM + unknowUser2;

      String chatMessage = MESSAGE_PREFIX + 0;
      String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList
          + CMDUDLM + CMDDLM + chatMessage;
      chatClients[0].sendMessage(privateCommand);

      new Verifications() {
        {
          String expectedMessage =
              CommandHandler.USR_NOT_FOUND_ERR_MSG + unknowUser1 + CMDULDLM + unknowUser2;
          Object actualObject = null;

          chatClients[0].getView().showErrorWindow(actualObject = withCapture(), anyString);
          assertTrue(actualObject.toString().contains(expectedMessage),
              "Message \"" + CommandHandler.USR_NOT_FOUND_ERR_MSG + "\" not received.");
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
        String userNameCaseSensitivity =
            Character.toUpperCase(userName.charAt(0)) + userName.substring(1);
        privateMessageRecepientList +=
            ((i != 1) ? CMDULDLM : "") + userNameCaseSensitivity + CMDULDLM + userName;
        privateMessageRecepientListCaseSensitivity +=
            ((i != 1) ? CMDULDLM : "") + userNameCaseSensitivity;
      }

      String chatMessage = MESSAGE_PREFIX + 0;
      String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList
          + CMDUDLM + CMDDLM + chatMessage;
      /*
       * System.out
       * .println("SendPrivateMessageIntegrationTest.AdditionalSpaces.testwithCaseSensitivity()"
       * + privateCommand);
       */
      chatClients[0].sendMessage(privateCommand);

      final String innerPrivateMessageRecepientList = privateMessageRecepientListCaseSensitivity;
      new Verifications() {
        {
          String expectedErrorMessage =
              CommandHandler.USR_NOT_FOUND_ERR_MSG + innerPrivateMessageRecepientList;
          Object actualObject = null;

          chatClients[0].getView().showErrorWindow(actualObject = withCapture(), anyString);
          /*
           * System.out.println(actualObject.toString()); System.out.println(expectedErrorMessage);
           */
          assertTrue(actualObject.toString().contains(expectedErrorMessage),
              "Message \"" + CommandHandler.USR_NOT_FOUND_ERR_MSG + "\" not received.");

          String expectedMessage = USER_NAME_PREFIX + 0 + ": " + chatMessage;
          String actualMessage;

          for (int i = 0; i < MAX_NUMBERS_OF_USERS; i++) {
            chatClients[i].getView().onReceiveMessage(actualMessage = withCapture());
            assertTrue(actualMessage.contains(expectedMessage),
                "All client must receive private message.");
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

      String privateMessageRecepientList =
          USER_NAME_PREFIX + 1 + CMDULDLM + CMDDLM + CMDDLM + USER_NAME_PREFIX + 3;

      String chatMessage = MESSAGE_PREFIX + 0;
      String privateCommand = "" + CMDPRVMSG + CMDDLM + CMDUDLM + privateMessageRecepientList
          + CMDUDLM + CMDDLM + chatMessage;
      System.out.println(
          "SendPrivateMessageIntegrationTest.AdditionalSpaces.testwithCaseSensitivity()"
              + privateCommand);
      chatClients[0].sendMessage(privateCommand);

      new Verifications() {
        {
          // TODO Additional spaces in usernames must be removed
          
          String expectedMessage = USER_NAME_PREFIX + 0 + ": " + chatMessage;
          String actualMessage;

          chatClients[1].getView().onReceiveMessage(actualMessage = withCapture());
          assertTrue(actualMessage.contains(expectedMessage),
              "All client must receive private message.");

          chatClients[3].getView().onReceiveMessage(actualMessage = withCapture());
          assertTrue(actualMessage.contains(expectedMessage),
              "All client must receive private message.");

        }
      };
    }
  }
}
