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
  // ArrayList handlers;
  CopyOnWriteArrayList<ChatHandler> chatHandlers;

  /** The distinct thread to accept client connections. */
  private Thread acception = new Thread("Acception Thread") {

    //private final AtomicBoolean running = new AtomicBoolean(false);
    
    @Override
    public void run() {

      try {
        while (true) {
          System.out.println("Waiting for client connection...");
          // new EchoServer2(serverSocket.accept());

          // Accept client connection and return new client socket
          clientConnection = serverSocket.accept();

          String ip = (((InetSocketAddress) clientConnection.getRemoteSocketAddress()).getAddress())
              .toString().replace("/", "");

          System.out.println("Accept client connection from " + ip);

          // Create new ChatHandler with existing client handlers and new client socket as thread
          new ChatHandler(clientConnection, chatHandlers).start();
        }
      } catch (SocketException e) {
        if (serverSocket == null || serverSocket.isClosed())
          System.out.println("Server stoped.");
      } catch (IOException e) {
        System.err.println("Accept failed.");
        System.exit(1);
      }

    }
  };

  /**
   * Instantiates a new chat server.
   */
  public ChatServer() {
    try {

      System.out.println("Chat server starting...");

      // Init client session handlers storage
      // handlers = new ArrayList();
      chatHandlers = new CopyOnWriteArrayList<ChatHandler>();

      // Init server socket with _SERVER_SOCKET port
      serverSocket = new ServerSocket(SERVER_PORT);

      System.out.println("Connection socket on port " + SERVER_PORT + " created.");
      acception.start();

      /*
       * new Thread() {
       * 
       * @Override public void run() { System.out.println("Type exit to exit;-)"); Console c =
       * System.console(); String msg = c.readLine(); if (msg.equals("exit")) { // some cleaning up
       * code... System.exit(0); } } }.start();
       * 
       */


      /*
       * Scanner in = new Scanner(System.in);
       * 
       * int i = in.nextInt(); String s = in.next();
       */


      // Wait for client connection
      // for (;;) {
      // System.in
      // while (in.next .next())

      // Accept client connection and return new client socket
      // s = serverSocket.accept();

      // Create new ChatHandler with existing client handlers and new client socket as thread
      // new ChatHandler(s, handlers).start();


      // }
    } catch (

    IOException ioe) {
      System.out.println(ioe.getMessage());

    }

    // support to close, using the command line.

    System.out.println("Server started, type quit to shutdown server: ");

    Scanner scn = new Scanner(System.in);
    String s = scn.next();

    while (true) {
      if (s.equalsIgnoreCase("quit")) {
        close();
        break;
      }
      s = scn.next();
    }

  }

  public boolean close() {
    try {
      
      System.out.println("Stopping acception thread...");
      //acception.sto
      
      System.out.println("Closing all client handlers...");
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
    ChatServer tes = new ChatServer();
  }
}

