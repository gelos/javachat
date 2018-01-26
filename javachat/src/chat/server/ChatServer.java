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

  // Constants

  /** The server port number. */
  public final static int SERVER_PORT = 3000;

  /** The server host ip address. */
  public final static String SERVER_IP = "127.0.0.1";

  // Variables

  /** The server socket. */
  private ServerSocket serverSocket;

  /** The client connection socket. */
  private Socket clientConnection;

  /** The client session handlers thread-safe storage. */
  private CopyOnWriteArrayList<ChatHandler> chatHandlers;

  /** The chat client communication thread. */
  private ChatClientCommunicationThreadClass chatClientCommunicationThread;

  /** The process console input thread. */
  private ProcessConsoleInputThreadClass processConsoleInputThread;

  /**  Started flag. */
  private boolean started;
  
  // Constructor

  /**
   * Instantiates a new chat server.
   */
  public ChatServer() {
    this(SERVER_PORT, new SocketFactoryImpl());
  }

  /**
   * Instantiates a new chat server.
   *
   * @param port the port
   * @param socketFactory the socket factory
   */
  public ChatServer(int port, SocketFactory socketFactory) {
    
    // Set state to not started yet
    started = false;

    System.out.println("Chat server starting...");

    // Initialize client session handlers storage
    chatHandlers = new CopyOnWriteArrayList<ChatHandler>();

    // try {

    // Initialize server socket with _SERVER_SOCKET port
    try {
      //serverSocket = socketFactory.createSocketFor(port);
      serverSocket = new ServerSocket(port);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.err.println("Failed to create server socket on port " + port);
      System.exit(1);
    }

    System.out.println("Connection socket on port " + port + " created.");


    /*
     * // } catch (IOException ioe) {
     * 
     * System.out.println(ioe.getMessage()); System.exit(1);
     * 
     * }
     */
    // Start thread to process chat client communications
    chatClientCommunicationThread = new ChatClientCommunicationThreadClass();
    chatClientCommunicationThread.start();

    // Start thread to process console command input
    processConsoleInputThread = new ProcessConsoleInputThreadClass();
    processConsoleInputThread.start();

    // support to close, using the command line.
    System.out.println("Type quit in console to shutdown server.");

    System.out.println("Server started.");

    // Set state to already started
    started = true;
    
  }

  
  /**
   * Checks if is started.
   *
   * @return true, if constructor complete execution, then we agree what server started
   */
  public boolean isStarted() {
    return started;
  }


  /**
   * Close.
   *
   * @return true, if successful
   */
  public boolean close() {

    try {

      System.out.println("Stopping server thread...");

      // Try to stop thread, set running flag to false
      chatClientCommunicationThread.stop();

      // Close server socket to release blocking on while circle in chatClientCommunicationThread on
      // serverSocket.accept(). Throw SocketException and stop thread.
      serverSocket.close();
      serverSocket = null;

      processConsoleInputThread.stop();


      System.out.println("Closing all client handlers...");

      // Close all ChatHadnlers
      for (ChatHandler ch : chatHandlers) {
        if (!ch.close()) {
          System.out.println("Client handlers closing error.");
        } ;
      }

      System.out.println("Server stopped.");
      // serverSocket.close();
      // serverSocket = null;
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

  /** The distinct thread to process chat client connections. */
  private class ChatClientCommunicationThreadClass implements Runnable {

    /** The worker. */
    private Thread worker;

    /** The running. */
    // Start-stop thread flag
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Stop.
     */
    public void stop() {
      running.set(false);
    }

    /**
     * Start.
     */
    public void start() {

      // Setting running flag for while circle
      running.set(true);

      // Start new thread
      worker = new Thread(this);
      worker.start();

    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

      // Waiting for client connection in circle
      while (running.get()) {

        try {

          // Accept client connection and return new client socket
          clientConnection = serverSocket.accept();

          // Create new ChatHandler with existing client handlers and new client socket as thread
          new ChatHandler(clientConnection, chatHandlers).start();

          String ip = (((InetSocketAddress) clientConnection.getRemoteSocketAddress()).getAddress())
              .toString().replace("/", "");
          System.out.println("Accepted client connection from " + ip);

        } catch (SocketException e) { // Throws when calling ServerSocket.close

          if ((serverSocket == null || serverSocket.isClosed()) && !running.get()) {

            // if Server socket not opened and thread trying to stop ignore error because it is
            // normal situation when we stopping server

          } else {

            // Something wrong write error
            e.printStackTrace();

          }

        } catch (IOException e) { // Exit program on IOException
          System.err.println("Accept failed.");
          System.exit(1);
        }
      }
    }


  }


  /** The thread to process console input. */
  private class ProcessConsoleInputThreadClass implements Runnable {

    /** The worker. */
    private Thread worker;
    
    /** The running. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Stop.
     */
    public void stop() {
      running.set(false);
    }

    /**
     * Start.
     */
    public void start() {
      running.set(true);
      worker = new Thread(this);
      worker.start();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

      Scanner consoleInput = new Scanner(System.in);
      String s = consoleInput.next();

      // Wait for console input
      while (running.get()) {

        // check inputs for "quit" command
        if (s.equalsIgnoreCase("quit")) {
          break;
        }
        s = consoleInput.next();

      }

      consoleInput.close();

      // try to close server
      close();

    }
  };


}

