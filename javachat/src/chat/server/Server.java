package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import chat.base.ChatSession;
import chat.base.Constants;
import chat.base.WorkerThread;


// TODO: Auto-generated Javadoc
// TODO: use netty+protobuf or ZeroMQ
// TODO add log support logback https://stackify.com/logging-logback/
// TODO use Callable, Future, CompleteCallable
/**
 * It implements the server part of the chat application. Accepts client sessions through
 * {@link ProcessClientConnectionsThread}, allows you to use the console commands through
 * {@link ProcessConsoleInputThread}.
 * 
 */

public class Server {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  /** The thread to process client connections. */
  private ProcessClientConnectionsThread processClientConnectionsThread;

  /** The process console input thread. */
  private ProcessConsoleInputThread processConsoleInputThread;

  /** The thread to stop server. */
  private StopServerThread stopServerThread;


  /**
   * Instantiates a new chat server on default SERVER_PORT.
   */
  public Server() {
    this(Constants.SERVER_PORT);
  }


  /**
   * Instantiates a new chat server.
   *
   * @param serverPort the port for accept client requests
   */
  public Server(int serverPort) {


    logger.info(this.getClass().getSimpleName() + "." + "Server(int) - {}", //$NON-NLS-1$ //$NON-NLS-2$
        Constants.MSG_SERVER_STARTING);
    System.out.println(Constants.MSG_SERVER_STARTING);

    stopServerThread = new StopServerThread();
    stopServerThread.start(stopServerThread.getClass().getSimpleName());

    // Start thread for create clients handler
    processClientConnectionsThread = new ProcessClientConnectionsThread(serverPort);
    processClientConnectionsThread.start(processClientConnectionsThread.getClass().getSimpleName());

    // Start thread for process console command input
    processConsoleInputThread = new ProcessConsoleInputThread();
    processConsoleInputThread.start(processConsoleInputThread.getClass().getSimpleName());

    // Check that all threads successfully running
    if (processClientConnectionsThread.isRunning() && processConsoleInputThread.isRunning()
        && stopServerThread.isRunning()) {

      logger.info(this.getClass().getSimpleName() + "." + "Server(int) - {}", //$NON-NLS-1$
          Constants.MSG_SERVER_STARTED);
      System.out.println(Constants.MSG_COMMAND_TO_SHUTDOWN_SERVER);
      System.out.println(Constants.MSG_SERVER_STARTED);

    } else {

      // Forcing the server to stop
      stopServerThread.stop();

    }
  }

  /**
   * The stop method stop all running thread.
   */
  public void stop() {

    logger.info(this.getClass().getSimpleName() + "." + "stop() - {}", //$NON-NLS-1$
        Constants.MSG_STOPPING_SERVER_THREADS);
    System.out.println(Constants.MSG_STOPPING_SERVER_THREADS);

    if (processClientConnectionsThread != null) {

      processClientConnectionsThread.stop();

      try {

        processClientConnectionsThread.getThread().join();
        processClientConnectionsThread = null;

      } catch (InterruptedException e) {
        logger.error(this.getClass().getSimpleName() + "." + "stop()", e); //$NON-NLS-1$
      }

    }

    if (processConsoleInputThread != null) {

      processConsoleInputThread.stop();

      try {

        processConsoleInputThread.getThread().join(Constants.THR_WAIT_TIMEOUT);
        processConsoleInputThread = null;

      } catch (InterruptedException e) {
        logger.error(this.getClass().getSimpleName() + "." + "stop()", e); //$NON-NLS-1$
      }
    }

    logger.info(this.getClass().getSimpleName() + "." + "stop() - {}", //$NON-NLS-1$
        Constants.MSG_SERVER_STOPPED);
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


  /**
   * Calling the Server.stop() method after StopServerThread.stop() was called from another thread.
   */
  private class StopServerThread extends WorkerThread {

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

      while (this.isRunning()) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          logger.error(this.getClass().getSimpleName() + "." + "run()", e); //$NON-NLS-1$
        }
      }
      Server.this.stop();
    }

  }

  /**
   * Processes client chat connections with a {@link ServerChatSession} object.
   * 
   */
  private class ProcessClientConnectionsThread extends WorkerThread {

    /** The client session handlers thread-safe storage. */
    private ConcurrentHashMap<String, ChatSession> chatSessionStorage;

    /** The server socket. */
    private ServerSocket serverSocket;

    /**
     * Instantiates a new process client connections thread.
     *
     * @param port the port
     */
    public ProcessClientConnectionsThread(int port) {
      super();

      chatSessionStorage = new ConcurrentHashMap<String, ChatSession>();

      openServerSocket(port);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

      try {

        // Waiting for client connection in circle
        while (this.isRunning()) {

          synchronized (serverSocket) {

            Socket socket = serverSocket.accept();
            new ServerChatSession(socket, chatSessionStorage);

          }
        }

      } catch (SocketException e) { // Throws when calling ServerSocket.close() to interrupt the
                                    // ServerSocket.accept()

        // If Server socket closed and thread trying to stop (isRunning() == false) ignore error
        // because it is normal situation when we are stopping server

        if (!isServerSocketClosed() || this.isRunning()) {

          // Something wrong, write error to log
          logger.error(this.getClass().getSimpleName() + ". run() - " //$NON-NLS-1$
              + Constants.ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED, e);

        }

      } catch (IOException e) { // stop server on IOException

        logger.error(this.getClass().getSimpleName() + "." + "run() - " //$NON-NLS-1$
            + Constants.ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED, e);


      } finally {

        // Close all chat sessions

        logger.info(this.getClass().getSimpleName() + "." + "run() - {}", //$NON-NLS-1$
            Constants.MSG_STOPPING_CHAT_CLIENT_HANDLERS);
        System.out.println(Constants.MSG_STOPPING_CHAT_CLIENT_HANDLERS);

        // TODO change serial stop to parallel stop with distinct thread for stopping each
        // serverCommandHandler

        if (chatSessionStorage != null) {

          for (ChatSession chatSession : chatSessionStorage.values()) {
            boolean sendEXTCMD = true;
            chatSession.closeSession(sendEXTCMD);
          }
        }

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

    /**
     * Checks if ServerSocket closed.
     *
     * @return true, if is server socket closed
     */
    private synchronized boolean isServerSocketClosed() {
      return (serverSocket == null || serverSocket.isClosed());
    }

    /**
     * Open server socket.
     *
     * @param serverSocketPort the server socket port
     */
    private synchronized void openServerSocket(int serverSocketPort) {
      try {
        serverSocket = new ServerSocket(serverSocketPort);
        logger.info(getClass().getSimpleName() + ".run() - {}", //$NON-NLS-1$
            Constants.MSG_CONNECTION_SOCKET_1 + serverSocketPort
                + Constants.MSG_CONNECTION_SOCKET_2);
        System.out.println(Constants.MSG_CONNECTION_SOCKET_1 + serverSocketPort
            + Constants.MSG_CONNECTION_SOCKET_2);
      } catch (BindException e) {
        logger.error(this.getClass().getSimpleName() + "." + "openServerSocket(int) - " //$NON-NLS-1$
            + Constants.ERR_PORT_IN_USE_1 + serverSocketPort + Constants.ERR_PORT_IN_USE_2, e); // $NON-NLS-2$

      } catch (IOException e) {
        logger.error(this.getClass().getSimpleName() + "." + "openServerSocket(int) - " //$NON-NLS-1$
            + Constants.ERR_MSG_FAILED_TO_CREATE_SERVER_SOCKET + serverSocketPort, e);
      }
    }

    /**
     * Close server socket.
     */
    private synchronized void closeServerSocket() {
      if (serverSocket != null) {
        // Close server socket to release blocking on while circle in
        // processClientConnectionsThread on clientSocket.accept(). Throw SocketException
        // and stop thread.
        try {
          serverSocket.close();
        } catch (IOException e) {
          logger.error(this.getClass().getSimpleName() + "." + "closeServerSocket()", e); //$NON-NLS-1$
        }
        serverSocket = null;
      }
    }

  }

  /**
   * The thread for processing input from the console.
   */
  private class ProcessConsoleInputThread extends WorkerThread {

    /** The console input. */
    private BufferedReader consoleInput;

    /**
     * Instantiates a new process console input thread.
     */
    public ProcessConsoleInputThread() {
      super();
      consoleInput = new BufferedReader(new InputStreamReader(System.in));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
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
              // Forcing the server to stop
              stopServerThread.stop();
              break;
            } else {
              System.out.println(Constants.MSG_COMMAND_TO_SHUTDOWN_SERVER);
            }
          }
        }

        consoleInput.close();

      } catch (IOException | InterruptedException e) {
        logger.error(this.getClass().getSimpleName() + "." + "run()", e); //$NON-NLS-1$
      }
    }
  }

};
