package chat.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO: use netty+protobuf
// TODO add log support
/**
 * Implements server side of chat application. Initialize server part, accept client connection and
 * create new thread as ChartHandler object for every new client connection. Use quit command from
 * console to shutdown server.
 * 
 * @see ChatHandler
 */
/**
 * @author Oleg.Olyunin
 *
 */
public class ChatServer {

  /** The server port number. */
  public final static int SERVER_PORT = 3000;

  /** The server host ip address. */
  public final static String SERVER_IP = "127.0.0.1";

  /** The server socket. */
  private ServerSocket serverSocket;

  /** The client connection socket. */
  Socket clientConnection;

  /** The client session handlers thread-safe storage. */
  CopyOnWriteArrayList<ChatHandler> chatHandlers;

  /** The distinct thread to accept client connections. */
  // private Thread chatClientThread = new Thread("Chat Client chatClientThread thread.") {
  private Thread chatClientThread = new Thread(new Runnable() {

    @Override
    public void run() {
      // TODO Auto-generated method stub
      try {
        while (true) {

          System.out.println("Waiting for client connection...");

          // Accept client connection and return new client socket
          clientConnection = serverSocket.accept();

          String ip = (((InetSocketAddress) clientConnection.getRemoteSocketAddress()).getAddress())
              .toString().replace("/", "");
          System.out.println("Accept client connection from " + ip);

          // Create new ChatHandler with existing client handlers and new client socket as thread
          new ChatHandler(clientConnection, chatHandlers).start();
        }

      } catch (SocketException e) { // Catch SocketException to avoid error while closing server
        if (serverSocket == null || serverSocket.isClosed()) {
          System.out.println("Server stoped.");
        }

      } catch (IOException e) { // Exit program on IOException
        System.err.println("Accept failed.");
        System.exit(1);
      }
    }
  });

  /** The process console input. */
  private Thread processConsoleInput = new Thread(new Runnable() {

    @Override
    public void run() {

      Scanner consoleInput = new Scanner(System.in);
      String s = consoleInput.next();

      while (true) {

        // on quit command stop server
        if (s.equalsIgnoreCase("quit")) {
          break;
        }
        s = consoleInput.next();

      }
      close();
    }
  });


  /**
   * Instantiates a new chat server.
   */
  public ChatServer() {
    try {

      System.out.println("Chat server starting...");

      // Initialize client session handlers storage
      chatHandlers = new CopyOnWriteArrayList<ChatHandler>();

      // Initialize server socket with _SERVER_SOCKET port
      serverSocket = new ServerSocket(SERVER_PORT);

      System.out.println("Connection socket on port " + SERVER_PORT + " created.");
      chatClientThread.start();

    } catch (IOException ioe) {

      System.out.println(ioe.getMessage());

    }

    // support to close, using the command line.
    System.out.println("Server started, type quit to shutdown server: ");

    // start thread to process console command input
    processConsoleInput.start();

  }

  /**
   * Close.
   *
   * @return true, if successful
   */
  public boolean close() {
    try {

      System.out.println("Stopping chatClientThread thread...");

      System.out.println("Closing all client handlers...");
      
      // Close all ChatHadnlers
      for (ChatHandler ch : chatHandlers) {
        if (!ch.close()) {
          System.out.println("Client handlers closing error.");
        };
      }

      System.out.println("Stoping server...");
      serverSocket.close();
      serverSocket = null;
    } catch (IOException e) {
      System.err.println("Could not close port: " + SERVER_PORT + ".");
      // System.exit(1);
      
      // TODO process return code
      return false;
    }

    return true;

  }


  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    new ChatServer();
  }
}

