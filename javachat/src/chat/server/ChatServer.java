package chat.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import chat.base.WorkerThread;

// TODO: use netty+protobuf or ZeroMQ
// TODO add log support logback https://stackify.com/logging-logback/
/**
 * Implements server side of chat application. Initialize server part, accept client connection and
 * create new thread as ChartHandler object for every new client connection. Use stop command from
 * console to shutdown server.
 * 
 * @see ChatHandler
 */
/**
 * @author Oleg.Olyunin
 *
 */
public class ChatServer {

  // Constants

  /** The server port number. */
  public final static int SERVER_PORT = 3000;

  /** The server host ip address. */
  public final static String SERVER_IP = "127.0.0.1";

  // Variables

  /** The server socket. */
  private ServerSocket serverSocket;

  /** The client connection socket. */
  private Socket clientSocket;

  /** The client session handlers thread-safe storage. */
  private CopyOnWriteArrayList<ChatHandler> chatHandlers;

  /** The chat client communication thread. */
  private ClientCommunicationThreadClass clientCommunicationThread;

  /** The process console input thread. */
  private ProcessConsoleInputThreadClass processConsoleInputThread;

  /** Started flag. */
  private AtomicBoolean started;

  /**
   * Stopped flag. False - after running stop() method indicate that server was not stopped
   * correctly, True - it indicates that stop() method finished successfully.
   */

  private AtomicBoolean stopped;

  // Constructor

  /**
   * Instantiates a new chat server.
   */
  public ChatServer() {

    // start server on SERVER_SOCKET
    this(SERVER_PORT);

  }

  /**
   * Instantiates a new chat server.
   *
   * @param port the port
   * @param socketFactory the socket factory
   */
  public ChatServer(int port) {

    // Create and initialize state flags
    started = new AtomicBoolean(false);
    stopped = new AtomicBoolean(false);

    System.out.println("Chat server starting...");

    // Initialize client session handlers storage
    chatHandlers = new CopyOnWriteArrayList<ChatHandler>();

    // Initialize server socket with port
    try {

      serverSocket = new ServerSocket(port);

    } catch (BindException e) {

      System.err.println("Port " + port + " already in use.");
      e.printStackTrace();

      // try to stop server
      this.stop();
      return;

    } catch (IOException e) {

      System.err.println("Failed to create server socket on port " + port);
      e.printStackTrace();

      // try to stop server
      this.stop();
      return;
    }

    System.out.println("Connection socket on port " + port + " created.");

    // Start thread to process chat client communications
    clientCommunicationThread = new ClientCommunicationThreadClass();
    clientCommunicationThread.start();

    // Start thread to process console command input
    processConsoleInputThread = new ProcessConsoleInputThreadClass();
    processConsoleInputThread.start();

    // Check that both thread successfully running
    if (clientCommunicationThread.isRuning() && processConsoleInputThread.isRuning()) {

      // support to close, using the command line.
      System.out.println("Type stop in console to shutdown server.");

      System.out.println("Server started.");

      // Set state to already started
      started.set(true);

    } else {

      // force to close server
      this.stop();

    }
  }

  /**
   * Checks if is started.
   *
   * @return true, if constructor complete execution, then we agree what server started
   */
  public boolean isStarted() {
    return started.get();
  }


  public boolean isStopped() {
    return stopped.get();
  }

  public void stop() {

    try {

      System.out.println("Stopping server thread...");

      if (clientCommunicationThread != null) {
        // Try to stop thread, set running flag to false
        clientCommunicationThread.stop();
      }

      if (serverSocket != null) {
        // Close server socket to release blocking on while circle in clientCommunicationThread
        // on serverSocket.accept(). Throw SocketException and stop thread.
        serverSocket.close();
        serverSocket = null;
      }

      if (processConsoleInputThread != null) {
        processConsoleInputThread.stop();
      }

      System.out.println("Stopping all client handlers...");

      if (chatHandlers != null) {
        // Close all ChatHadnlers
        for (ChatHandler ch : chatHandlers) {
          ch.stop();
        }
      }

      System.out.println("Server stopped.");

    } catch (IOException e) {

      System.err.println("Could not close port: " + SERVER_PORT + ".");
      e.printStackTrace();

    }

    // set stopped flag
    stopped.set(true);
  }

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    new ChatServer();
  }

  /** The distinct thread to process chat client connections. */
  private class ClientCommunicationThreadClass extends WorkerThread {

    @Override
    public void run() {

      // Waiting for client connection in circle
      while (this.isRuning()) {

        try {

          // Accept client connection and return new client socket
          clientSocket = serverSocket.accept();

          // Create new ChatHandler with existing client handlers and new client socket as thread
          new ChatHandler(clientSocket, chatHandlers).start();;

        } catch (SocketException e) { // Throws when calling ServerSocket.close

          if ((serverSocket == null || serverSocket.isClosed()) && !this.isRuning()) {

            // if Server socket not opened and thread trying to stop ignore error because it is
            // normal situation when we stopping server

          } else {

            // Something wrong write error and stop server
            System.err.println("Chat client acception failed.");
            e.printStackTrace();
            stop();

          }

        } catch (IOException e) { // stop server on IOException

          System.err.println("Chat client acception failed.");
          e.printStackTrace();
          stop();

        }
      }
    }


  }

  /** The thread to process console input. */
  private class ProcessConsoleInputThreadClass extends WorkerThread {

    @Override
    public void run() {

      Scanner consoleInput = new Scanner(System.in);
      String s = consoleInput.next();

      // Wait for console input
      while (this.isRuning()) {

        // check inputs for "stop" command
        if (s.equalsIgnoreCase("stop")) {
          break;
        }
        s = consoleInput.next();

      }

      consoleInput.close();
      // try to close server
      ChatServer.this.stop();
    }
  };

}

