package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import chat.base.ChatSession;
import chat.base.Command;
import chat.base.CommandHandler;
import chat.base.Constants;
import chat.base.WorkerThread;

// TODO: use netty+protobuf or ZeroMQ
// TODO add log support logback https://stackify.com/logging-logback/
// TODO use Callable, Future, CompleteCallable
/**
 * Implements the server part of the chat application. Accepts client sessions through
 * {@link ProcessClientConnectionsThread}, allows you to use the console commands through
 * {@link ProcessConsoleInputThread}.
 * 
 */

public class Server {

  // Constructor

  private static final int CMD_HNDL_STOP_TIMEOUT = 100;

  /** The server serverSocketPort number. */
  public final static int SERVER_PORT = 3000;

  /** The server host ip address. */
  public final static String SERVER_IP = "127.0.0.1";

  /**
   * Logger for this class
   */
  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  /** The client session handlers thread-safe storage. */
  // private CopyOnWriteArrayList<ServerCommandHandler> serverCommandHandlers;
  private ConcurrentHashMap<String, ServerCommandHandler> serverCommandHandlers;
  //private ConcurrentHashMap<String, ChatSession> serverCommandHandlers;

  /** The chat client communication thread. */
  private ProcessClientConnectionsThread processClientConnectionsThread;

  /** The process console input thread. */
  private ProcessConsoleInputThread processConsoleInputThread;

  private StopServerThread stopServerThread;

  private AtomicBoolean stopServerFlag;

  /**
   * Instantiates a new chat server on default SERVER_PORT.
   */
  public Server() {

    // start server on SERVER_SOCKET
    this(SERVER_PORT);

  }

  /**
   * Instantiates a new chat server.
   *
   * @param serverSocketPort the serverSocketPort to start
   */
  public Server(int serverPort) {

    // Start thread that stop server on stopServerFlag set
    stopServerFlag = new AtomicBoolean(false);
    stopServerThread = new StopServerThread();
    stopServerThread.start(stopServerThread.getClass().getSimpleName());

    logger.info("Server.Server(int) - {}", Constants.MSG_SERVER_STARTING); //$NON-NLS-1$ //$NON-NLS-2$
    System.out.println(Constants.MSG_SERVER_STARTING);

    // Initialize client handlers storage
    serverCommandHandlers = new ConcurrentHashMap<String, ServerCommandHandler>();
    //serverCommandHandlers = new ConcurrentHashMap<String, ChatSession>();

    // Start thread for create clients handler
    processClientConnectionsThread = new ProcessClientConnectionsThread(serverPort);
    processClientConnectionsThread.start(processClientConnectionsThread.getClass().getSimpleName());

    // Start thread for process console command input
    processConsoleInputThread = new ProcessConsoleInputThread();
    processConsoleInputThread.start(processConsoleInputThread.getClass().getSimpleName());

    // Check that both thread successfully running
    if (processClientConnectionsThread.isRunning() && processConsoleInputThread.isRunning()) {

      logger.info("Server.Server(int) - {}", Constants.MSG_SERVER_STARTED); //$NON-NLS-1$

      System.out.println(Constants.MSG_COMMAND_TO_SHUTDOWN_SERVER);
      System.out.println(Constants.MSG_SERVER_STARTED);

    } else {

      // Forcing the server to close
      stopServerFlag.set(true);

    }
  }

  /**
   * Stop.
   */
  public void stop() {

    logger.info("Server.stop() - {}", Constants.MSG_STOPPING_SERVER_THREADS); //$NON-NLS-1$
    System.out.println(Constants.MSG_STOPPING_SERVER_THREADS);

    logger.info("Server.stop() - {}", Constants.MSG_STOPPING_CHAT_CLIENT_HANDLERS); //$NON-NLS-1$
    System.out.println(Constants.MSG_STOPPING_CHAT_CLIENT_HANDLERS);

    // TODO change serial stop to parallel stop with distinct thread for stopping each
    // serverCommandHandler

    if (serverCommandHandlers != null) {

      //for (ChatSession serverCommandHandler : serverCommandHandlers.values()) {
      for (ServerCommandHandler serverCommandHandler : serverCommandHandlers.values()) {

        serverCommandHandler.stop();

        try {

          // TODO check why we cannot run with join()?

          serverCommandHandler.getThread().join(CMD_HNDL_STOP_TIMEOUT);
          serverCommandHandler = null;

        } catch (InterruptedException e) {
          logger.error("stop()", e); //$NON-NLS-1$
        }

      }
    }

    if (processClientConnectionsThread != null) {

      processClientConnectionsThread.stop();

      try {

        processClientConnectionsThread.getThread().join();
        processClientConnectionsThread = null;

      } catch (InterruptedException e) {
        logger.error("stop()", e); //$NON-NLS-1$
      }

    }

    if (processConsoleInputThread != null) {

      processConsoleInputThread.stop();

      try {

        processConsoleInputThread.getThread().join();
        processConsoleInputThread = null;

      } catch (InterruptedException e) {
        logger.error("stop()", e); //$NON-NLS-1$
      }
    }

    logger.info("Server.stop() - {}", Constants.MSG_SERVER_STOPPED); //$NON-NLS-1$
    System.out.println(Constants.MSG_SERVER_STOPPED);

  }

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    new Server();
  }

  // Constructor

  public final void setStopServerFlag(Boolean stopServerFlag) {
    this.stopServerFlag.set(stopServerFlag);
  }

  private class StopServerThread extends WorkerThread {

    @Override
    public void run() {

      while (!stopServerFlag.get()) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          logger.error("$Runnable.run()", e); //$NON-NLS-1$
        }
      }
      Server.this.stop();
    }

  }

  /**
   * Creates an {@link ServerCommandHandler} object for each new client connection.
   * 
   */
  private class ProcessClientConnectionsThread extends WorkerThread {

    private static final String THREAD_NAME_SRV = "server-";

    private int serverSocketPort;

    /** The client connection socket. */
    private Socket clientSocket;

    /** The server socket. */
    private ServerSocket serverSocket;

    private ServerChatSession serverChatSession;

    public ProcessClientConnectionsThread(int port) {
      this.serverSocketPort = port;
    }

    @Override
    public void run() {

      if (!openServerSocket(serverSocketPort)) {
        // stop();
        return;
      }

      logger.info("ProcessClientConnectionsThread.run() - {}", //$NON-NLS-1$
          Constants.MSG_CONNECTION_SOCKET_1 + serverSocketPort + Constants.MSG_CONNECTION_SOCKET_2);
      System.out.println(
          Constants.MSG_CONNECTION_SOCKET_1 + serverSocketPort + Constants.MSG_CONNECTION_SOCKET_2);

      try {

        // Waiting for client connection in circle
        while (this.isRunning()) {

          synchronized (serverSocket) {

            clientSocket = serverSocket.accept();

            System.out.println("create new client socket on  server");
            new ServerCommandHandler(clientSocket, serverCommandHandlers).start(THREAD_NAME_SRV);
            //serverChatSession = new ServerChatSession(clientSocket, serverCommandHandlers);
          }
        }

      } catch (SocketException e) { // Throws when calling ServerSocket.close

        if (isServerSocketClosed() && !this.isRunning()) {

          // if Server socket not opened and thread trying to stop ignore error because it
          // is normal situation when we stopping server

        } else {

          // Something wrong write error and stop server
          logger.error("run() - " + Constants.ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED, e); //$NON-NLS-1$
        }

      } catch (IOException e) { // stop server on IOException

        logger.error("run() - " + Constants.ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED, e); //$NON-NLS-1$

      } /*
         * finally {
         * 
         * stop();
         * 
         * }
         */
    }

    private synchronized boolean isServerSocketClosed() {
      return (serverSocket == null || serverSocket.isClosed());
    }

    private synchronized boolean openServerSocket(int serverSocketPort) {
      try {
        serverSocket = new ServerSocket(serverSocketPort);
      } catch (BindException e) {
        logger.error("openServerSocket(int) - " + Constants.ERR_PORT_IN_USE_1 + serverSocketPort //$NON-NLS-1$
            + Constants.ERR_PORT_IN_USE_2, e); // $NON-NLS-2$
        return false;

      } catch (IOException e) {
        logger.error("openServerSocket(int) - " + Constants.ERR_MSG_FAILED_TO_CREATE_SERVER_SOCKET //$NON-NLS-1$
            + serverSocketPort, e);
        return false;
      }
      return true;
    }

    private synchronized void closeServerSocket() {
      if (serverSocket != null) {
        // Close server socket to release blocking on while circle in
        // processClientConnectionsThread on clientSocket.accept(). Throw SocketException
        // and stop thread.
        try {
          serverSocket.close();
        } catch (IOException e) {
          logger.error("closeServerSocket()", e); //$NON-NLS-1$
        }
        serverSocket = null;
      }
    }

    /**
     * Do not move closeServerSocket method to {@link ProcessClientConnectionsThread#run()}. It is
     * placed here to interrupt clientSocket = clientSocket.accept() in
     * {@link ProcessClientConnectionsThread#run()} with SocketException.
     */

    @Override
    public void stop() {
      super.stop();
      closeServerSocket();
    }

  }

  /** The thread for processing input from the console. */
  private class ProcessConsoleInputThread extends WorkerThread {

    private BufferedReader consoleInput;

    public ProcessConsoleInputThread() {
      consoleInput = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {

      try {

        // Wait for console input
        while (this.isRunning()) {

          while (!consoleInput.ready()) {
            if (!this.isRunning()) {
              break;
            }
            Thread.sleep(10);
          }

          if (this.isRunning()) {
            String s = consoleInput.readLine();

            // Check input for stop command
            if (s.equalsIgnoreCase(Constants.SERVER_CMD_STOP)) {
              Server.this.setStopServerFlag(true);
              break;
            }
          }
        }

        consoleInput.close();

      } catch (IOException | InterruptedException e) {
        logger.error("ProcessConsoleInputThread.run()", e); //$NON-NLS-1$
      }
    }
  }



};
