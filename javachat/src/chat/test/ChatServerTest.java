package chat.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import chat.server.ChatServer;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;

class ChatServerTest {

  // @Tested
  // ChatServer chatServerMock;
  // @Injectable
  // ServerSocket serverSocketMock;

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

  //@Disabled
  @DisplayName("Test start stop server without error.")
  @Test
  void serverStartStopTest() throws InterruptedException {

    // redirect System.out & System.err
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    ChatServer chatServer = new ChatServer();

    int timeout = 1;

    // wait while chatServer started
    while (!chatServer.isStarted() && (timeout <= 10)) {
      TimeUnit.SECONDS.sleep(1);
      timeout++;
    }

    assertNotEquals(outContent.toString().length(), 0);
    assertEquals(errContent.toString().length(), 0,
        "It looks like we have some errors in err-stream.");
    assertTrue("Maybe server not started correctly? Check ChatServer() constructor.",
        outContent.toString().contains("Server started."));

    assertTrue("Server not closed correctly.", chatServer.close());

    assertNotEquals(outContent.toString().length(), 0);
    assertEquals(errContent.toString().length(), 0,
        "It looks like we have some errors in err-stream.");
    assertTrue("Maybe server not stopped correctly? Check ChatServer() constructor.",
        outContent.toString().contains("Server stopped."));

    // restore
    System.setOut(System.out);
    System.setErr(System.err);

  }

  @DisplayName("Test chat server behavior on IOException error while create ServerSocket.")
  @Test
  void newServerSocketIOExceptionTest(@Mocked ServerSocket serverSocket)
      throws InterruptedException, IOException {

    // redirect System.out & System.err
    System.setErr(new PrintStream(errContent));

    // throw IOException on ServerSocket create
    new Expectations() {
      {
        new ServerSocket(anyInt);
        result = new IOException();
      }
    };

    // create chet server
    ChatServer chatServer = new ChatServer();

    int timeout = 1;
    // wait while chatServer started
    while (!chatServer.isStarted() && (timeout <= 10) && (errContent.toString().length() == 0)) {
      System.out.println(chatServer == null);
      TimeUnit.SECONDS.sleep(1);
      timeout++;
    }

    assertTrue("Chat server not properly catch IOException on new ServerSocket.",
        errContent.toString().contains("Failed to create server socket on port"));

    // restore
    System.setErr(System.err);

  }

  @Disabled
  @Test
  // void acceptServerSocketIOExceptionTest(@Mocked ChatServer chatServerMock, @Injectable
  // ServerSocket serverSocketMock) throws IOException, InterruptedException {
  // void acceptServerSocketIOExceptionTest(@Mocked ChatServer chatServerMock) throws
  // InterruptedException {
  void acceptServerSocketIOExceptionTest(@Mocked ChatServer chatServerMock,
      @Injectable ServerSocket serverSocket) throws InterruptedException, IOException {

    new Expectations() {
      {

        // chatServer.close(); result = false;
        // new ChatServer(); result = chatServerMock;
        // chatServerMock.isStarted(); result = true;
        new ServerSocket();
        result = new IOException();
        // serverSocketMock.accept();
        // result = new IOException();
      }
    };

    // chatServerMock.

    /*
     * assertThrows(IOException.class, () -> { new ChatServer(); });
     */

    // ChatServer chatServer = new ChatServer();
    // chatServerMock1 = new ChatServer();

    ChatServer chatServer = new ChatServer();

    int timeout = 1;

    // wait while chatServer started
    while (!chatServer.isStarted() && (timeout <= 10)) {
      TimeUnit.SECONDS.sleep(1);
      System.out.println(timeout);
      timeout++;
    }

    chatServer.close();


    /*
     * new FullVerifications() { { chatServerMock; chatServerMock.isStarted();
     * chatServerMock.close(); }};
     */

  }

}
