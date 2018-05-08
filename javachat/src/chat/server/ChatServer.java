package chat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import chat.base.WorkerThread;

// TODO: use netty+protobuf or ZeroMQ
// TODO add log support logback https://stackify.com/logging-logback/
// TODO use Callable, Future, CompleteCallable
/**
 * Implements server side of chat application. Initialize server part, accept client connection and
 * create new thread as ChartHandler object for every new client connection. Use stop command from
 * console to shutdown server.
 * 
 * @see CommandHandler
 */

public class ChatServer {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

  public static final String MSG_COMMAND_TO_SHUTDOWN_SERVER = "Type \"stop\" in console to shutdown server.";

  public static final String MSG_SERVER_STARTED = "Server started.";

  public static final String MSG_SERVER_STOPPED = "The server is stopped.";

  public static final String MSG_STOPPING_CHAT_CLIENT_HANDLERS = "Stopping the chat client handlers...";

  public static final String MSG_STOPPING_SERVER_THREADS = "Stopping server threads...";

  public static final String ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED = "Chat client acception failed.";

  public static final String ERR_MSG_FAILED_TO_CREATE_SERVER_SOCKET = "Failed to create server socket on serverSocketPort ";

  /** The server serverSocketPort number. */
  public final static int SERVER_PORT = 3000;

  /** The server host ip address. */
  public final static String SERVER_IP = "127.0.0.1";

  /** The client session handlers thread-safe storage. */
  // private CopyOnWriteArrayList<CommandHandler> commandHandlers;
  private ConcurrentHashMap<String, CommandHandler> commandHandlers;

  /** The chat client communication thread. */
  private ProcessChatHandlerThreadClass processChatHandlerThread;

  /** The process console input thread. */
  private ProcessConsoleInputThreadClass processConsoleInputThread;

  /** Started flag. */
  //private AtomicBoolean started;

  /**
   * Stopped flag. False - after running stop() method indicate that server was not stopped
   * correctly, True - it indicates that stop() method finished successfully.
   */
  //private AtomicBoolean stopped;


  private AtomicBoolean stoppingChatServerFlag;

  

  

  // Constructor

  public final void setStopingRequestFlag(Boolean stopingRequestFlag) {
    this.stoppingChatServerFlag.set(stopingRequestFlag);
  }

  /**
   * Instantiates a new chat server on default SERVER_PORT.
   */
  public ChatServer() {

    // start server on SERVER_SOCKET
    this(SERVER_PORT);

  }

  /**
   * Instantiates a new chat server.
   *
   * @param serverSocketPort the serverSocketPort to start
   */
  public ChatServer(int serverPort) {

    // create and initialize state flags
    /*started = new AtomicBoolean(false);
    stopped = new AtomicBoolean(false);*/
	  logger.info("Hello World");

    stoppingChatServerFlag = new AtomicBoolean(false);

    System.out.println("Chat server starting...");

    // initialize client session handlers storage
    // commandHandlers = new CopyOnWriteArrayList<CommandHandler>();
    commandHandlers = new ConcurrentHashMap<String, CommandHandler>();

    /*
     * // initialize server socket with serverSocketPort try {
     * 
     * serverSocket = new ServerSocket(serverSocketPort);
     * 
     * } catch (BindException e) {
     * 
     * System.err.println("Port " + serverSocketPort + " already in use."); e.printStackTrace();
     * 
     * // try to stop server stop(); return;
     * 
     * } catch (IOException e) {
     * 
     * System.err.println("Failed to create server socket on serverSocketPort " + serverSocketPort);
     * e.printStackTrace();
     * 
     * // try to stop server stop(); return; }
     * 
     * System.out.println("Connection socket on serverSocketPort " + serverSocketPort +
     * " created.");
     */
    // start thread to process chat client communications
    processChatHandlerThread = new ProcessChatHandlerThreadClass(serverPort);
    processChatHandlerThread.start(processChatHandlerThread.getClass().getSimpleName());

    // start thread to process console command input
    processConsoleInputThread = new ProcessConsoleInputThreadClass();
    processConsoleInputThread.start(processConsoleInputThread.getClass().getSimpleName());
    // System.out.println("ChatServer.ChatServer() " + processConsoleInputThread);

    // check that both thread successfully running
    if (processChatHandlerThread.isRuning() && processConsoleInputThread.isRuning()) {

      // print console message how to stop server from console
      System.out.println(MSG_COMMAND_TO_SHUTDOWN_SERVER);

      System.out.println(MSG_SERVER_STARTED);

      // Set state to already started
      //started.set(true);

    } else {

      // force to close server
      // stop();
      stoppingChatServerFlag.set(true);

    }

    new Thread(null, new Runnable() {

      @Override
      public void run() {
        while (!stoppingChatServerFlag.get()) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        ChatServer.this.stop();
      }
    }, "StopFromConsoleThread").start();


  }

  /**
   * Checks if is started.
   *
   * @return true, if constructor complete execution, then we agree what server started
   */
  /*public boolean isStarted() {
    return started.get();
  }*/


  /**
   * Checks if is stopped.
   *
   * @return true, if is stopped
   */
/*  public boolean isStopped() {
    return stopped.get();
  }*/

  /**
   * Stop.
   */
  public void stop() {

    System.out.println(MSG_STOPPING_SERVER_THREADS);

    if (processChatHandlerThread != null) {
      
      processChatHandlerThread.stop();
      
      try {
      
        processChatHandlerThread.getThread().join();
        processChatHandlerThread = null;

      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    if (processConsoleInputThread != null) {
    
      processConsoleInputThread.stop();
      
      try {
      
        processConsoleInputThread.getThread().join();
        processConsoleInputThread = null;
        
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    System.out.println(MSG_STOPPING_CHAT_CLIENT_HANDLERS);

    if (commandHandlers != null) {

      for (CommandHandler commandHandler : commandHandlers.values()) {
      
        commandHandler.stop();

        try {
          commandHandler.getThread().join();
          commandHandler = null;
          
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      }
    }

    System.out.println(MSG_SERVER_STOPPED);

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
  private class ProcessChatHandlerThreadClass extends WorkerThread {

    private int serverSocketPort;

    /** The client connection socket. */
    private Socket clientSocket;

    /** The server socket. */
    private ServerSocket serverSocket;


    public ProcessChatHandlerThreadClass(int port) {
      this.serverSocketPort = port;
    }

    /*
     * (non-Javadoc)
     * 
     * @see chat.base.WorkerThread#run()
     */
    @Override
    public void run() {

      if (!openServerSocket(serverSocketPort)) {
        stop();
        // closeServerSocket();
        return;
      }
      /*
       * // initialize server socket with serverSocketPort try {
       * 
       * serverSocket = new ServerSocket(serverSocketPort);
       * 
       * } catch (BindException e) {
       * 
       * System.err.println("Port " + serverSocketPort + " already in use."); e.printStackTrace();
       * 
       * // try to stop server stop(); serverSocket = null; return;
       * 
       * } catch (IOException e) {
       * 
       * System.err.println("Failed to create server socket on serverSocketPort " +
       * serverSocketPort); e.printStackTrace();
       * 
       * // try to stop server stop(); serverSocket = null; return; }
       */
      System.out.println("Connection socket on serverSocketPort " + serverSocketPort + " created.");



      try {
        // waiting for client connection in circle
        while (this.isRuning()) {

          synchronized (serverSocket) {
            // accept client connection and return new client socket
            clientSocket = serverSocket.accept();

            // create new CommandHandler with existing client handlers and new client socket as
            // thread
            new CommandHandler(clientSocket, commandHandlers).start();
          }
        }

      } catch (SocketException e) { // Throws when calling ServerSocket.close

        if (isServerSocketClosed() && !this.isRuning()) {

          // if Server socket not opened and thread trying to stop ignore error because it is
          // normal situation when we stopping server
          //System.out.println("ChatServer.ProcessChatHandlerThreadClass.run() socketException");
          //System.out.println("ChatServer.ProcessChatHandlerThreadClass.run() " + this.isRuning());

        } else {

          // Something wrong write error and stop server
          System.err.println(ChatServer.ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED);
          e.printStackTrace();
          // stop();
          // closeServerSocket();
        }

      } catch (IOException e) { // stop server on IOException

        System.err.println(ChatServer.ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED);
        e.printStackTrace();
        // stop();
        // closeServerSocket();
      } finally {
        stop();
        // closeServerSocket();
      }


    }

    private synchronized boolean isServerSocketClosed() {
      return (serverSocket == null || serverSocket.isClosed());
    }

    private synchronized boolean openServerSocket(int serverSocketPort) {
      try {
        serverSocket = new ServerSocket(serverSocketPort);
      } catch (BindException e) {
        System.err.println("Port " + serverSocketPort + " already in use.");
        e.printStackTrace();
        return false;

      } catch (IOException e) {
        // TODO Auto-generated catch block
        System.err
            .println(ChatServer.ERR_MSG_FAILED_TO_CREATE_SERVER_SOCKET + serverSocketPort);
        e.printStackTrace();
        return false;
      }
      return true;
    }

    private synchronized void closeServerSocket() {
      // private void closeServerSocket() {
      if (serverSocket != null) {
        // Close server socket to release blocking on while circle in processChatHandlerThread
        // on serverSocket.accept(). Throw SocketException and stop thread.
        try {
          serverSocket.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        serverSocket = null;
      }
    }


    /**
     * Do not move closeServerSocket method to {@link ProcessChatHandlerThreadClass#run()}. It is
     * placed here to interrupt clientSocket = serverSocket.accept() in
     * {@link ProcessChatHandlerThreadClass#run()} with SocketException.
     */

    @Override
    public void stop() { // TODO Auto-generated method stub
      super.stop();
      closeServerSocket();
    }



  }

  /** The thread to process console input. */
  private class ProcessConsoleInputThreadClass extends WorkerThread {

    // private Scanner consoleInput;
    private BufferedReader consoleInput;
    // BufferedReader br = new BufferedReader(
    // new InputStreamReader(System.in));

    public ProcessConsoleInputThreadClass() {
      // consoleInput = new Scanner(System.in);
      consoleInput = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {

      // String s = consoleInput.next();

      // Wait for console input
      while (this.isRuning()) {

        try {
          while (!consoleInput.ready()) {
            try {
              if (!this.isRuning()) {
                break;
              }
              Thread.sleep(10);
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
          if (this.isRuning()) {
            String s = consoleInput.readLine();

            // check inputs for "stop" command
            if (s.equalsIgnoreCase("stop")) {
              // consoleInput.close();
              // try to close server
              // TODO synchronize this
              // ChatServer.this.stop();
              ChatServer.this.setStopingRequestFlag(true);
              break;
            }
          } else {
            consoleInput.close();
          }

        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

        /*
         * // check inputs for "stop" command if (s.equalsIgnoreCase("stop")) { //
         * consoleInput.close(); // try to close server // TODO synchronize this
         * ChatServer.this.stop(); break; } //s = consoleInput.next();
         */
      }

    }

    /*
     * @Override public void stop() { // TODO Auto-generated method stub
     * System.out.println("ChatServer.ProcessConsoleInputThreadClass.stop() pre stop");
     * super.stop();
     * System.out.println("ChatServer.ProcessConsoleInputThreadClass.stop() after stop"); //
     * synchronized (consoleInput) { // consoleInput.close(); System.out
     * .println("ChatServer.ProcessConsoleInputThreadClass.stop() after consoleInput.close()"); // }
     * }
     */
  };

}

