package chat.test;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import chat.base.ChatClientPresenter;
import chat.base.Presenter;
import chat.base.View;
import chat.server.ChatServer;
import mockit.Capturing;
import mockit.FullVerifications;
import mockit.Verifications;

@DisplayName("Send private message ")
class SendPrivateMessageIntegrationTest {

  // MAX_NUMBER_OF_CLIENTS must be equal sum number of @Capturing View variables
  public static final int MAX_NUMBERS_OF_CLIENTS = 4;
  public static final String CLIENT_NAME_PREFIX = "client";
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
  private Presenter[] chatClients = new Presenter[MAX_NUMBERS_OF_CLIENTS];

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
      chatClients[i] = createChatClientFactory(CLIENT_NAME_PREFIX + i, view);
    }

    // Connect client to server
    for (int i = 0; i < chatClients.length; i++) {
      chatClients[i].openConnection(CLIENT_NAME_PREFIX + i);
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

    static final int NUMBER_OF_PRIVATE_MSG_RECEPIENTS = MAX_NUMBERS_OF_CLIENTS - 2;

    @Test
    void sendNormalMessage() throws InterruptedException {

      // Run test only if number of clients >= 4
      // client 0 send message, clients 1-(n-2) must receive it, client n-1 must not receive
      assumeTrue(MAX_NUMBERS_OF_CLIENTS >= 4, "Client number must be >= 4.");

      String clientString = "";

      // Create user name recipient list
      for (int i = 1; i <= NUMBER_OF_PRIVATE_MSG_RECEPIENTS; i++) {
        clientString += (i == 1) ? CLIENT_NAME_PREFIX + i : CMDULDLM + CLIENT_NAME_PREFIX + i;
      }

   /*   for (int i = 0; i < chatClients.length; i++) {
        System.out
            .println(i + " " + chatClients[i] + " " + chatClients[i].getView().getClass().getName()
                + "@" + Integer.toHexString(System.identityHashCode(chatClients[i].getView())));
      }*/

      // Send private message from client0 to all clients except last
      chatClients[0].sendMessage(
          "" + CMDPRVMSG + CMDDLM + CMDUDLM + clientString + CMDUDLM + CMDDLM + MESSAGE_PREFIX + 0);

      new Verifications() {
        {
          // Check that client 0 send private message
          senderView.onSendMessage();
          times = 1;

          // Check that recipient client receive it
          String expectedMessage;
          String actualMessage = CLIENT_NAME_PREFIX + 0 + ": " + MESSAGE_PREFIX + 0;

          receiverView1.onReceiveMessage(expectedMessage = withCapture());
          assertTrue(expectedMessage.contains(actualMessage));

          receiverView2.onReceiveMessage(expectedMessage = withCapture());
          assertTrue(expectedMessage.contains(actualMessage));

          // Check that not recipient client not receive it
          notReceiverView.onReceiveMessage(expectedMessage = withCapture());
          assertFalse(expectedMessage.contains(actualMessage));

        }
      };
    }
  }

  @Nested
  @DisplayName("with empty user list")
  class EmptyUserList {
    @Test
    void sendEmptyUserList() {
      chatClients[0].sendMessage("/prvmsg '' " + MESSAGE_PREFIX + 0);
      new Verifications() {
        {
          // Check that client 0 send private message
          senderView.onSendMessage();
          times = 1;       

          String message;
          chatClients[1].getView().onReceiveMessage(message = withCapture());
          assertTrue(message.contains(CLIENT_NAME_PREFIX + 0 + ": " + MESSAGE_PREFIX + 0));

          chatClients[2].getView().onReceiveMessage(message = withCapture());
          assertTrue(message.contains(CLIENT_NAME_PREFIX + 0 + ": " + MESSAGE_PREFIX + 0));
        }
      };

    }
  }

}
