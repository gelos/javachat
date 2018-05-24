package chat.test;

import static org.junit.Assert.assertTrue;
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

import chat.base.Constants;
import chat.server.Server;
import mockit.Expectations;
import mockit.Mocked;

class ChatServerTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

  @DisplayName("Start and stop server.")
  @Test
  void serverStartStopTest() throws Throwable {

    // Set exception handler to throw other thread exceptions in current thread
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    // Redirect System.out & System.err to current thread to allow checking standard and error
    // output streams
    PrintStream systemOut = System.out;
    PrintStream systemErr = System.err;
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    Server server = new Server();

    // Checking for lack of errors and correctness standard output on server starting
    assertEquals(errContent.toString().length(), 0, "There are some errors in error stream.");
    assertTrue("Server not started correctly. Check output stream.",
        outContent.toString().contains(Constants.MSG_SERVER_STARTED));

    // Pause between starting and stopping the server to allow console message prints in right
    // order.
    TimeUnit.SECONDS.sleep(1);

    server.stop();

    // Checking for lack of errors and correctness standard output on server stopping
    assertEquals(errContent.toString().length(), 0, "There are some errors in error stream.");
    assertTrue("Server not stopped correctly. Check output stream.",
        outContent.toString().contains(Constants.MSG_SERVER_STOPPED));

    // Restore streams
    System.setOut(systemOut);
    System.setErr(systemErr);

    // If we have exceptions in other threads, throw them into the current thread
    if (exception.get() != null) {
      throw exception.get();
    }

  }

  @Disabled
  @DisplayName("Testing the behavior of the chat server, when an IOException occurs when creating the new ServerSocket()")
  @Test
  void newServerSocketIOExceptionTest(@Mocked ServerSocket serverSocket) throws Throwable {

    // Set exception handler to throw other thread exceptions in current thread
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    // Redirect System.err to current thread to allow checking error output streams
    PrintStream systemErr = System.err;
    System.setErr(new PrintStream(errContent));

    // Mocking a new ServerSocket to throws IOException
    new Expectations() {
      {
        new ServerSocket(anyInt);
        result = new IOException();
      }
    };

    Server server = new Server();

    //TimeUnit.SECONDS.sleep(1);

    assertTrue("Chat server not properly catch IOException on new ServerSocket.",
        errContent.toString().contains(Constants.ERR_MSG_FAILED_TO_CREATE_SERVER_SOCKET));

    server.stop();


    // Restore streams
    System.setErr(systemErr);

    // If we have exceptions in other threads, throw them into the current thread
    if (exception.get() != null) {
      throw exception.get();
    }

  }

  @Disabled
  @DisplayName("Testing the behavior of the chat server, when an IOException occurs in the ServerSocket.accept() method, using partial mocking.")
  @Test
  void acceptServerSocketIOExceptionTest() throws Throwable {

    // Set exception handler to throw other thread exceptions in current thread
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    // Redirect System.err to current thread to allow checking error output streams
    PrintStream systemErr = System.err;
    System.setErr(new PrintStream(errContent));

    // Mock only ServerSocket.accept() to throw IOException
    ServerSocket serverSocketPartialMock = new ServerSocket();
    new Expectations(ServerSocket.class) {
      {
        serverSocketPartialMock.accept();
        result = new IOException();
      }
    };

    Server server = new Server();

    // A pause between starting and stopping the server to ensure that we arrive at the
    // ServerSocket.accept () command.
    TimeUnit.SECONDS.sleep(1);

    assertTrue("Chat server not properly catch IOException on ServerSocket.accept()",
        errContent.toString().contains(Constants.ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED));

    server.stop();

    serverSocketPartialMock.close();
    
    // Restore streams
    System.setErr(systemErr);

    // If we have exceptions in other threads, throw them into the current thread
    if (exception.get() != null) {
      throw exception.get();
    }
    
  }

}
