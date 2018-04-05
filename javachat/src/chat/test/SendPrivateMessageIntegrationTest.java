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

  @Disabled
  @Nested
  @DisplayName("with normal message")
  class Normal {

    static final int NUMBER_OF_PRIVATE_MSG_RECEPIENTS = MAX_NUMBERS_OF_USERS - 2;

    @Test
    void testNormalMessage() throws InterruptedException {

      // Run test only if number of clients >= 4
      // client 0 send message, clients 1-(n-2) must receive it, client n-1 must not receive
      assumeTrue(MAX_NUMBERS_OF_USERS >= 4, "Client number must be >= 4.");

      String clientListString = "";

      // Create user name recipient list
      for (int i = 1; i <= NUMBER_OF_PRIVATE_MSG_RECEPIENTS; i++) {
        clientListString += (i == 1) ? USER_NAME_PREFIX + i : CMDULDLM + USER_NAME_PREFIX + i;
      }

      /*
       * for (int i = 0; i < chatClients.length; i++) { System.out .println(i + " " + chatClients[i]
       * + " " + chatClients[i].getView().getClass().getName() + "@" +
       * Integer.toHexString(System.identityHashCode(chatClients[i].getView()))); }
       */

      // Send private message from client0 to all clients except last
      String prvMessageString = "" + CMDPRVMSG + CMDDLM + CMDUDLM + clientListString + CMDUDLM
          + CMDDLM + MESSAGE_PREFIX + 0;
      chatClients[0].sendMessage(prvMessageString);

      new Verifications() {
        {
          // Check that client 0 send private message
          senderView.onSendMessage();
          times = 1;

          // Check that recipient client receive it
          String actualMessage;
          String expectedMessage = USER_NAME_PREFIX + 0 + ": " + MESSAGE_PREFIX + 0;

          senderView.onReceiveMessage(actualMessage = withCapture());
          assertTrue(actualMessage.contains(expectedMessage),
              "Sender must receive private message.");

          receiverView1.onReceiveMessage(actualMessage = withCapture());
          assertTrue(actualMessage.contains(expectedMessage),
              "Recepients must receive private message.");

          receiverView2.onReceiveMessage(actualMessage = withCapture());
          assertTrue(actualMessage.contains(expectedMessage),
              "Recepients must receive private message.");

          // Check that not recipient client not receive it
          notReceiverView.onReceiveMessage(actualMessage = withCapture());
          assertFalse(actualMessage.contains(expectedMessage),
              "Last client must not receive private message.");

        }
      };
    }
  }

  @Disabled
  @Nested
  @DisplayName("with empty user list")
  class EmptyUserList {
    @Test
    void testEmptyUserList() {

      // Send private message with empty user list
      String clientListString = "";
      String prvMessageString = "" + CMDPRVMSG + CMDDLM + CMDUDLM + clientListString + CMDUDLM
          + CMDDLM + MESSAGE_PREFIX + 0;
      chatClients[0].sendMessage(prvMessageString);

      System.out.println();

      new Verifications() {
        {
          // Check that client 0 send private message
          senderView.onSendMessage();
          times = 1;

          // Check that all clients receive it
          String actualMessage;
          String expectedMessage = USER_NAME_PREFIX + 0 + ": " + MESSAGE_PREFIX + 0;

          for (int i = 0; i < chatClients.length; i++) {
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

      String clientListString = "";

      // Create user name recipient list
      for (int i = 1; i < chatClients.length; i++) {
        clientListString += (i == 1) ? USER_NAME_PREFIX + i : CMDULDLM + USER_NAME_PREFIX + i;
      }

      // Add duplicate username for client 1 nad client 3
      clientListString += CMDULDLM + USER_NAME_PREFIX + 1 + CMDULDLM + USER_NAME_PREFIX + 3;

      // Send private message from client0 to all clients
      chatClients[0].sendMessage("" + CMDPRVMSG + CMDDLM + CMDUDLM + clientListString + CMDUDLM
          + CMDDLM + MESSAGE_PREFIX + 0);

      new Verifications() {
        {
          // Check that client 0 send private message
          senderView.onSendMessage();
          times = 1;

          // Check that all clients receive it
          String actualMessage;
          String expectedMessage = USER_NAME_PREFIX + 0 + ": " + MESSAGE_PREFIX + 0;

          for (int i = 0; i < chatClients.length; i++) {
            chatClients[i].getView().onReceiveMessage(actualMessage = withCapture());
            System.out.println(actualMessage);
            assertTrue(actualMessage.contains(expectedMessage),
                "All client must receive private message.");
            if (i == 1) { // Check that client 1 receive private message only once, times = 4 equal
                          // 3 welcome login message from client sequential startup + 1 private
                          // message
              times = 4;
            } else if (i == 3) { // Check that client 3 receive private message only once, times = 2
                                 // equal 1 welcome login message from client sequential startup + 1
                                 // private message
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

      String clientListString = "";
      for (int i = 1; i < MAX_NUMBERS_OF_USERS; i++) {
        clientListString += (i == 1) ? USER_NAME_PREFIX + i : CMDULDLM + USER_NAME_PREFIX + i;
      }

      String unknowUserNamesString1 = USER_NAME_PREFIX + (MAX_NUMBERS_OF_USERS + 1);
      String unknowUserNamesString2 = USER_NAME_PREFIX + (MAX_NUMBERS_OF_USERS + 100);
      clientListString += CMDULDLM + unknowUserNamesString1 + CMDULDLM + unknowUserNamesString2;
      clientListString = CMDUDLM + clientListString + CMDUDLM;

      String chatMessage = MESSAGE_PREFIX + 0;
      chatClients[0].sendMessage("" + CMDPRVMSG + CMDDLM + clientListString + CMDDLM + chatMessage);

      new Verifications() {
        {
          String expectedMessage = CommandHandler.USR_NOT_FOUND_ERR_MSG + unknowUserNamesString1
              + CMDULDLM + unknowUserNamesString2;
          Object actualObject = null;

          chatClients[0].getView().showErrorWindow(actualObject = withCapture(), anyString);
          assertTrue(actualObject.toString().contains(expectedMessage),
              "Message \"" + CommandHandler.USR_NOT_FOUND_ERR_MSG + "\" not received");
        }
      };
    }
  }

  @Disabled
  @Nested
  @DisplayName("with different case sensivity in user names")
  class DifferentCaseSensitivity {
    @Test
    void testDifferentCaseSensitivity() {

      fail("Not implemented yet.");
      String clientListString = "";

      // Create user name recipient list
      for (int i = 1; i < chatClients.length; i++) {
        clientListString += (i == 1) ? USER_NAME_PREFIX + i : CMDULDLM + USER_NAME_PREFIX + i;
      }

      // Add duplicate username for client 1 nad client 3
      clientListString += CMDULDLM + USER_NAME_PREFIX + 1 + CMDULDLM + USER_NAME_PREFIX + 3;

      // Send private message from client0 to all clients
      chatClients[0].sendMessage("" + CMDPRVMSG + CMDDLM + CMDUDLM + clientListString + CMDUDLM
          + CMDDLM + MESSAGE_PREFIX + 0);

      new Verifications() {
        {
          // Check that client 0 send private message
          senderView.onSendMessage();
          times = 1;

          // Check that all clients receive it
          String actualMessage;
          String expectedMessage = USER_NAME_PREFIX + 0 + ": " + MESSAGE_PREFIX + 0;

          for (int i = 0; i < chatClients.length; i++) {
            chatClients[i].getView().onReceiveMessage(actualMessage = withCapture());
            System.out.println(actualMessage);
            assertTrue(actualMessage.contains(expectedMessage),
                "All client must receive private message.");
            if (i == 1) { // Check that client 1 receive private message only once, times = 4 equal
                          // 3 welcome login message from client sequential startup + 1 private
                          // message
              times = 4;
            } else if (i == 3) { // Check that client 3 receive private message only once, times = 2
                                 // equal 1 welcome login message from client sequential startup + 1
                                 // private message
              times = 2;
            }
          }
        }
      };
    }
  }
}
