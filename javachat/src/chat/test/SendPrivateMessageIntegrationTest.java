package chat.test;

import static chat.base.CommandName.*;
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
import chat.client.mvp.swing.ChatClientViewSwing;
import chat.server.ChatServer;
import mockit.FullVerifications;
import mockit.Mocked;

@DisplayName("Send private message ")
class SendPrivateMessageIntegrationTest {

  public static final int MAX_NUMBERS_OF_CLIENTS = 5;
  public static final String CLIENT_NAME_PREFIX = "client";
  public static final String MESSAGE_PREFIX = "message";

  private ChatServer chatServer;
  private Presenter[] chatClients = new Presenter[MAX_NUMBERS_OF_CLIENTS];

  private ChatClientPresenter createChatClientFactory(String username, @Mocked View view) {
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

    // Create clients
    for (int i = 0; i < chatClients.length; i++) {
      View clientView = new ChatClientViewSwing();
      chatClients[i] = createChatClientFactory(CLIENT_NAME_PREFIX + i, clientView);
    }

    // Connect to server
    for (int i = 0; i < chatClients.length; i++) {
      chatClients[i].openConnection(CLIENT_NAME_PREFIX + i);
    }

  }

  @AfterEach
  void AfterAll() throws Exception {

    // stop server
    chatServer.stop();

  }

  @Nested
  @DisplayName("with normal message")
  class Normal {

    static final int NUMBER_OF_PRIVATE_MSG_RECEPIENTS = MAX_NUMBERS_OF_CLIENTS - 2;

    @Test
    void sendNormalMessage() {

      // Run test only if number of clients >= 4
      assumeTrue(MAX_NUMBERS_OF_CLIENTS >= 4, "Client number must be >= 4.");

      String clientString = "";

      // Create user name recipient list
      for (int i = 1; i <= NUMBER_OF_PRIVATE_MSG_RECEPIENTS; i++) {
        clientString += (i == 1) ? CLIENT_NAME_PREFIX + i : CMDULDLM + CLIENT_NAME_PREFIX + i;
      }

      chatClients[0].sendMessage(
          "" + CMDPRVMSG + CMDDLM + CMDUDLM + clientString + CMDUDLM + CMDDLM + MESSAGE_PREFIX + 0);

      new FullVerifications() {
        {
          // Check that client 0 send message
          // View chatClientView0 = chatClients[0].getView();
          // chatClientView0.onSendMessage();
          chatClients[0].getView().onSendMessage();

          String message;

        /*  try {
            TimeUnit.SECONDS.sleep(100);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }*/

          // Check that all clients in recepient list receive message
          for (int i = 1; i <= NUMBER_OF_PRIVATE_MSG_RECEPIENTS; i++) {
            chatClients[i].getView().onReceiveMessage(message = withCapture());
            System.out.println(message);
            assertTrue(message.contains(CLIENT_NAME_PREFIX + 0 + ": " + MESSAGE_PREFIX + 0));
          }

        }
      };

    }
  }


  @Disabled
  @Nested
  @DisplayName("with empty user list")
  class EmptyUserList {
    @Test
    void sendEmptyUserList() {
      chatClients[0].sendMessage("/prvmsg '' " + MESSAGE_PREFIX + 0);
      new FullVerifications() {
        {
          View chatClientView0 = chatClients[0].getView();
          chatClientView0.onSendMessage();

          String message;
          chatClients[1].getView().onReceiveMessage(message = withCapture());
          assertTrue(message.contains(CLIENT_NAME_PREFIX + 1 + ": " + MESSAGE_PREFIX + 0));

          chatClients[2].getView().onReceiveMessage(message = withCapture());
          assertTrue(message.contains(CLIENT_NAME_PREFIX + 1 + ": " + MESSAGE_PREFIX + 0));
        }
      };

    }
  }

}
