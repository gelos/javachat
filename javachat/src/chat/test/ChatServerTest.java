package chat.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import chat.server.ChatServer;
import mockit.Expectations;
import mockit.Mocked;

class ChatServerTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

  //@Disabled
  @DisplayName("Test starting, stoping server.")
  @Test
  void serverStartStopTest() throws Throwable {

    // set exception handler to throw other thread exceptions in current thread
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    // redirect System.out & System.err
    PrintStream systemOut = System.out;
    PrintStream systemErr = System.err;
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    ChatServer chatServer = new ChatServer();

    int timeout = 1;

    // wait while chatServer started
/*    while (!chatServer.isStarted() && (timeout <= 10) && (errContent.toString().length() == 0)) {
      TimeUnit.SECONDS.sleep(1);
      timeout++;
    }
*/
    // check for lack of errors and correctness standard output on server starting
    assertNotEquals(outContent.toString().length(), 0);
    assertEquals(errContent.toString().length(), 0,
        "It looks like we have some errors in err-stream.");
    assertTrue("Maybe server not started correctly? Check ChatServer() constructor.",
        outContent.toString().contains("Server started."));

    // try to stop server
    chatServer.stop();

    // try to stop server, wait for return true
    //assertTrue("Server not stopped correctly.", chatServer.isStopped());

    fail("Check console output for \"Server stopped.\" message.");
    
    // check for lack of errors and correctness standard output on server stopping
    assertNotEquals(outContent.toString().length(), 0);
    assertEquals(errContent.toString().length(), 0,
        "It looks like we have some errors in err-stream.");
    assertTrue("Maybe server not stopped correctly? Check ChatServer() constructor.",
        outContent.toString().contains("Server stopped."));


    // if we get other thread exception throw it in current thread
    if (exception.get() != null) {
      throw exception.get();
    }

    // restore
    System.setOut(systemOut);
    System.setErr(systemErr);


    // try another start/stop without controlling standard output and errors
    chatServer = new ChatServer();

    timeout = 1;

/*    // wait while chatServer started
    while (!chatServer.isStarted() && (timeout <= 10) && (errContent.toString().length() == 0)) {
      TimeUnit.SECONDS.sleep(1);
      timeout++;
    }*/

    // try to stop server
    chatServer.stop();

    // try to stop server, wait for return true
    //assertTrue("Server not stopped correctly on second try.", chatServer.isStopped());
    
    fail("Check console output for \"Server stopped.\" message.");

    // if we get other thread exception throw it in current thread
    if (exception.get() != null) {
      throw exception.get();
    }

  }

  @Disabled
  @DisplayName("Test chat server behavior on IOException error while create ServerSocket.")
  @Test
  void newServerSocketIOExceptionTest(@Mocked ServerSocket serverSocket) throws Throwable {

    // set exception handler to throw other thread exceptions in current thread
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    // redirect System.err
    PrintStream systemErr = System.err;
    System.setErr(new PrintStream(errContent));

    // throw IOException on ServerSocket create
    new Expectations() {
      {
        new ServerSocket(anyInt);
        result = new IOException();
      }
    };

    // create chat server
    ChatServer chatServer = new ChatServer();

    int timeout = 1;

    // wait while chatServer started
/*    while (!chatServer.isStarted() && (timeout <= 10) && (errContent.toString().length() == 0)) {
      System.out.println(chatServer == null);
      TimeUnit.SECONDS.sleep(1);
      timeout++;
    }*/

    assertTrue("Chat server not properly catch IOException on new ServerSocket.",
        errContent.toString().contains("Failed to create server socket on port"));

    // if we get other thread exception throw it in current thread
    if (exception.get() != null) {
      throw exception.get();
    }

    // restore
    System.setErr(systemErr);

  }

  @Disabled
  @DisplayName("Test chat server behavior on IOException error on ServerSocket.accept(), using patial mocking.")
  @Test
  void acceptServerSocketIOExceptionTest() throws Throwable {

    // set exception handler to throw other thread exceptions in current thread
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    // redirect System.err
    PrintStream systemErr = System.err;
    System.setErr(new PrintStream(errContent));

    ServerSocket serverSocketPartialMock = new ServerSocket();

    new Expectations(ServerSocket.class) {
      {
        serverSocketPartialMock.accept();
        result = new IOException();
      }
    };

    ChatServer chatServer = new ChatServer();

    int timeout = 1;

    // wait while chatServer started
/*    while (!chatServer.isStarted() && (timeout <= 10) && (errContent.toString().length() == 0)) {
      TimeUnit.SECONDS.sleep(1);
      System.out.println(timeout);
      timeout++;
    }*/

    assertTrue("Chat server not properly catch IOException on ServerSocket.accept()",
        errContent.toString().contains("Chat client acception failed."));

    // if we get other thread exception throw it in current thread
    if (exception.get() != null) {
      throw exception.get();
    }

    // restore
    System.setErr(systemErr);

  }

}
