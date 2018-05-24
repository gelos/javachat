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

import chat.base.WorkerThread;

// TODO: use netty+protobuf or ZeroMQ
// TODO add log support logback https://stackify.com/logging-logback/
// TODO use Callable, Future, CompleteCallable
/**
 * Implements server side of chat application. Initialize server part, accept
 * client connection and create new thread as ChartHandler object for every new
 * client connection. Use stop command from console to shutdown server.
 * 
 * @see ServerCommandHandler
 */

public class Server {
	// Constructor

	private static final String CMD_STOP = "stop";

	public static final String MSG_SERVER_STARTED = "Server started.";

	public static final String MSG_SERVER_STOPPED = "The server is stopped.";

	private static final String MSG_SERVER_STARTING = "Chat server starting...";

	private static final String MSG_COMMAND_TO_SHUTDOWN_SERVER = "Type \"" + CMD_STOP
			+ "\" in console to shutdown server.";

	private static final String MSG_STOPPING_CHAT_CLIENT_HANDLERS = "Stopping the chat client handlers...";

	private static final String MSG_STOPPING_SERVER_THREADS = "Stopping server threads...";

	private static final String MSG_CONNECTION_SOCKET_1 = "Server socket on ";

	private static final String MSG_CONNECTION_SOCKET_2 = " port created.";

	public static final String ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED = "Chat client acception failed.";

	public static final String ERR_MSG_FAILED_TO_CREATE_SERVER_SOCKET = "Failed to create server socket on serverSocketPort ";

	private static final String ERR_PORT_IN_USE_1 = "Port ";

	private static final String ERR_PORT_IN_USE_2 = " already in use.";

	// Constructor

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

	/** The chat client communication thread. */
	private ProcessClientHandlersThread processClientHandlersThread;

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
	 * @param serverSocketPort
	 *            the serverSocketPort to start
	 */
	public Server(int serverPort) {

		// Start thread that stop server on stopServerFlag set
		stopServerFlag = new AtomicBoolean(false);
		stopServerThread = new StopServerThread();
		stopServerThread.start(stopServerThread.getClass().getSimpleName());

		logger.info("Server.Server(int) - {}", MSG_SERVER_STARTING); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(MSG_SERVER_STARTING);

		// Initialize client handlers storage
		serverCommandHandlers = new ConcurrentHashMap<String, ServerCommandHandler>();

		// Start thread for create clients handler
		processClientHandlersThread = new ProcessClientHandlersThread(serverPort);
		processClientHandlersThread.start(processClientHandlersThread.getClass().getSimpleName());

		// Start thread for process console command input
		processConsoleInputThread = new ProcessConsoleInputThread();
		processConsoleInputThread.start(processConsoleInputThread.getClass().getSimpleName());

		// Check that both thread successfully running
		if (processClientHandlersThread.isRunning() && processConsoleInputThread.isRunning()) {

			logger.info("Server.Server(int) - {}", MSG_SERVER_STARTED); //$NON-NLS-1$

			System.out.println(MSG_COMMAND_TO_SHUTDOWN_SERVER);
			System.out.println(MSG_SERVER_STARTED);

		} else {

			// Forcing the server to close
			stopServerFlag.set(true);

		}
	}

	/**
	 * Stop.
	 */
	public void stop() {

		logger.info("Server.stop() - {}", MSG_STOPPING_SERVER_THREADS); //$NON-NLS-1$
		System.out.println(MSG_STOPPING_SERVER_THREADS);

		logger.info("Server.stop() - {}", MSG_STOPPING_CHAT_CLIENT_HANDLERS); //$NON-NLS-1$
		System.out.println(MSG_STOPPING_CHAT_CLIENT_HANDLERS);

		if (serverCommandHandlers != null) {

			for (ServerCommandHandler serverCommandHandler : serverCommandHandlers.values()) {

				serverCommandHandler.stop();

				try {
					serverCommandHandler.getThread().join();
					serverCommandHandler = null;

				} catch (InterruptedException e) {
					logger.error("stop()", e); //$NON-NLS-1$
				}

			}
		}

		if (processClientHandlersThread != null) {

			processClientHandlersThread.stop();

			try {

				processClientHandlersThread.getThread().join();
				processClientHandlersThread = null;

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

		logger.info("Server.stop() - {}", MSG_SERVER_STOPPED); //$NON-NLS-1$
		System.out.println(MSG_SERVER_STOPPED);

	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
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

	/** The distinct thread to process chat client connections. */
	private class ProcessClientHandlersThread extends WorkerThread {

		private static final String THREAD_NAME_SRV = "server-";

		private int serverSocketPort;

		/** The client connection socket. */
		private Socket clientSocket;

		/** The server socket. */
		private ServerSocket serverSocket;

		public ProcessClientHandlersThread(int port) {
			this.serverSocketPort = port;
		}

		@Override
		public void run() {

			if (!openServerSocket(serverSocketPort)) {
				// stop();
				return;
			}

			logger.info("ProcessClientHandlersThread.run() - {}", //$NON-NLS-1$
					Server.MSG_CONNECTION_SOCKET_1 + serverSocketPort + Server.MSG_CONNECTION_SOCKET_2);
			System.out.println(Server.MSG_CONNECTION_SOCKET_1 + serverSocketPort + Server.MSG_CONNECTION_SOCKET_2);

			try {

				// Waiting for client connection in circle
				while (this.isRunning()) {

					synchronized (serverSocket) {

						clientSocket = serverSocket.accept();

						new ServerCommandHandler(clientSocket, serverCommandHandlers).start(THREAD_NAME_SRV);
					}
				}

			} catch (SocketException e) { // Throws when calling ServerSocket.close

				if (isServerSocketClosed() && !this.isRunning()) {

					// if Server socket not opened and thread trying to stop ignore error because it
					// is normal situation when we stopping server

				} else {

					// Something wrong write error and stop server
					logger.error("run() - " + Server.ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED, e); //$NON-NLS-1$
				}

			} catch (IOException e) { // stop server on IOException

				logger.error("run() - " + Server.ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED, e); //$NON-NLS-1$

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
				logger.error("openServerSocket(int) - " + Server.ERR_PORT_IN_USE_1 + serverSocketPort //$NON-NLS-1$
						+ Server.ERR_PORT_IN_USE_2, e); // $NON-NLS-2$
				return false;

			} catch (IOException e) {
				logger.error(
						"openServerSocket(int) - " + Server.ERR_MSG_FAILED_TO_CREATE_SERVER_SOCKET + serverSocketPort, //$NON-NLS-1$
						e);
				return false;
			}
			return true;
		}

		private synchronized void closeServerSocket() {
			if (serverSocket != null) {
				// Close server socket to release blocking on while circle in
				// processClientHandlersThread on serverSocket.accept(). Throw SocketException
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
		 * Do not move closeServerSocket method to
		 * {@link ProcessClientHandlersThread#run()}. It is placed here to
		 * interrupt clientSocket = serverSocket.accept() in
		 * {@link ProcessClientHandlersThread#run()} with SocketException.
		 */

		@Override
		public void stop() {
			super.stop();
			closeServerSocket();
		}

	}

	/** The thread to process console input. */
	private class ProcessConsoleInputThread extends WorkerThread {

		private BufferedReader consoleInput;

		public ProcessConsoleInputThread() {
			consoleInput = new BufferedReader(new InputStreamReader(System.in));
		}

		@Override
		public void run() {

			// Wait for console input
			while (this.isRunning()) {

				try {
					while (!consoleInput.ready()) {
						try {
							if (!this.isRunning()) {
								break;
							}
							Thread.sleep(10);
						} catch (InterruptedException e) {
							logger.error("run()", e); //$NON-NLS-1$
						}
					}
					if (this.isRunning()) {
						String s = consoleInput.readLine();

						// Check input for stop command
						if (s.equalsIgnoreCase(Server.CMD_STOP)) {
							Server.this.setStopServerFlag(true);
							break;
						}
					} else {
						consoleInput.close();
					}

				} catch (IOException e1) {
					logger.error("run()", e1); //$NON-NLS-1$
				}

			}

		}

	}

};
