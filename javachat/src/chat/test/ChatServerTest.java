package chat.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
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

  @Disabled
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

  @Test
  // void acceptServerSocketIOExceptionTest(@Mocked ChatServer chatServerMock, @Injectable
  // ServerSocket serverSocketMock) throws IOException, InterruptedException {
  //void acceptServerSocketIOExceptionTest(@Mocked ChatServer chatServerMock) throws InterruptedException {
  void acceptServerSocketIOExceptionTest(@Mocked ChatServer chatServerMock, @Injectable ServerSocket serverSocket) throws InterruptedException, IOException {
    
    new Expectations() {
      {
        
        //chatServer.close(); result = false;
        //new ChatServer(); result = chatServerMock;
        //chatServerMock.isStarted(); result = true;
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

    //ChatServer chatServer = new ChatServer();
    //chatServerMock1 = new ChatServer();

    ChatServer chatServer = new ChatServer();
    
    int timeout = 1;

    // wait while chatServer started
    while (!chatServer.isStarted() && (timeout <= 10)) {
      TimeUnit.SECONDS.sleep(1);
      System.out.println(timeout);
      timeout++;
    }

    chatServer.close();

    
      /*new FullVerifications() { {
        chatServerMock;
        chatServerMock.isStarted();
        chatServerMock.close();
      }};*/
     
  }

}
