package chat.server;

import java.io.*;
import java.net.*;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * Implements server side of chat application. Initialize server part, accept client connection and
 * create new thread as ChartHandler object for every new client connection.
 * 
 * @see ChatHandler
 */
public class ChatServer {

  /** The server port number */
  final static int _SERVER_SOCKET = 3000;

  /** The client connection socket. */
  Socket s;

  /** The client session handlers storage. */
  ArrayList handlers;

  /**
   * Instantiates a new chat server.
   */
  public ChatServer() {
    try {

      // Init client session handlers storage
      handlers = new ArrayList();

      // Init server socket with _SERVER_SOCKET port
      ServerSocket ss = new ServerSocket(_SERVER_SOCKET);

      // Wait for client connection
      for (;;) {

        // Accept client connection and return new client socket
        s = ss.accept();

        // Create new ChatHandler with existing client handlers and new client socket as thread
        new ChatHandler(s, handlers).start();

      }
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    }
  }

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    ChatServer tes = new ChatServer();
    System.out.println("Chat server started.");
  }
}

