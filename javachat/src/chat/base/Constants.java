package chat.base;

/**
 * The Class Constants. Used in the client and the server part.
 */
public final class Constants {

  /** The Constant MSG_OPEN_CONNECTION. */
  public static final String MSG_OPEN_CONNECTION = "Open connection for user ";

  /** The Constant MSG_ACCPT_CLIENT. */
  public static final String MSG_ACCPT_CLIENT = "Accepted client connection from ";

  /** The Constant ERR_USRS_NOT_FOUND. */
  public static final String ERR_USRS_NOT_FOUND = "User(s) not found. Username list: ";
  /** The Constant ERR_NAME_EXISTS_MSG. */
  public static final String ERR_NAME_EXISTS_MSG = "Username already exists or wrong.";
  /** The Constant MSG_WLC_USR. */
  public static final String MSG_WLC_USR = "login";
  /** The Constant MSG_EXIT_USR. */
  public static final String MSG_EXIT_USR = "logout";

  /** The Constant MSG_CLOSE_CONNECTION. */
  public static final String MSG_CLOSE_CONNECTION = "Close connection for ";

  /** The Constant MSG_ASK_FOR_USERNAME. */
  public final static String MSG_ASK_FOR_USERNAME = "Enter username to start сhat: ";

  /** The Constant MSG_EMPTY_USRENAME. */
  public final static String MSG_EMPTY_USRENAME = "Username cannot be empty.";

  /** The Constant MSG_CANT_CON_SRV. */
  public final static String MSG_CANT_CON_SRV = "Can't connect to server " + Constants.SERVER_IP
      + ":" + Constants.SERVER_PORT + ". Server not started.";
  /** The Constant DEFAULT_WINDOW_NAME. */
  public static final String DEFAULT_WINDOW_NAME = "Java Chat Client";

  /** The Constant THREAD_NAME_CLIENT. */
  public static final String THREAD_NAME_CLIENT = "client-";

  /** The Constant WRN_UNKNOWN_COMMAND_MSG. */
  public static final String WRN_UNKNOWN_COMMAND_MSG = "Received unknown command";

  /** The Constant SERVER_CMD_STOP. */
  public static final String SERVER_CMD_STOP = "stop";

  /** The Constant MSG_SERVER_STARTED. */
  public static final String MSG_SERVER_STARTED = "Server started.";

  /** The Constant MSG_SERVER_STOPPED. */
  public static final String MSG_SERVER_STOPPED = "The server is stopped.";

  /** The Constant MSG_SERVER_STARTING. */
  public static final String MSG_SERVER_STARTING = "Chat server starting...";

  /** The Constant MSG_COMMAND_TO_SHUTDOWN_SERVER. */
  public static final String MSG_COMMAND_TO_SHUTDOWN_SERVER =
      "Type \"" + SERVER_CMD_STOP + "\" in console to shutdown server.";

  /** The Constant MSG_STOPPING_CHAT_CLIENT_HANDLERS. */
  public static final String MSG_STOPPING_CHAT_CLIENT_HANDLERS =
      "Closing the chat client sessions...";

  /** The Constant MSG_STOPPING_SERVER_THREADS. */
  public static final String MSG_STOPPING_SERVER_THREADS = "Stopping server threads...";

  /** The Constant MSG_CONNECTION_SOCKET_1. */
  public static final String MSG_CONNECTION_SOCKET_1 = "Server socket on ";

  /** The Constant MSG_CONNECTION_SOCKET_2. */
  public static final String MSG_CONNECTION_SOCKET_2 = " port created.";

  /** The Constant ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED. */
  public static final String ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED = "Chat client acception failed.";

  /** The Constant ERR_MSG_FAILED_TO_CREATE_SERVER_SOCKET. */
  public static final String ERR_MSG_FAILED_TO_CREATE_SERVER_SOCKET =
      "Failed to create server socket on serverSocketPort ";

  /** The Constant ERR_PORT_IN_USE_1. */
  public static final String ERR_PORT_IN_USE_1 = "Port ";

  /** The Constant ERR_PORT_IN_USE_2. */
  public static final String ERR_PORT_IN_USE_2 = " already in use.";


  /** The default waiting time for thread termination. */
  public static final long THR_WAIT_TIMEOUT = 1000;

  /** The server serverSocketPort number. */
  public final static int SERVER_PORT = 3000;

  /** The server host ip address. */
  public final static String SERVER_IP = "127.0.0.1";

  public static final String THREAD_NAME_SRV = "server-";

  /** The Constant MAX_TIMEOUT_SESSION_OPEN_MS. */
  public final static int MAX_TIMEOUT_SESSION_OPEN_MS = 100;

  /** The Constant MAX_TIMEOUT_OUT_STREAM_OPEN_MS. */
  public static final int MAX_TIMEOUT_OUT_STREAM_OPEN_MS = 100;

  public static final String MSG_COMMAND_NOT_IMPLEMENTED_YET = "Command not implemented yet.";

  public static final String MSG_SESSION_NOT_OPENED_BUT_COMMAND_RECEIVED =
      "Session not opened, but command received.";

  public static final String MSG_TIMEOUT_OK_CMD = "Timeout for the OK command, ";

  public static final String MSG_TIMEOUT_OUTPUT_STREAM = "Timeout for opening output stream, ";

  public static final String MSG_CAN_T_CONNECT_TO_THE_SERVER_TIMEOUT_2 =
      ". Check server, try again or increase open session timeout.";

  public static final String MSG_CLOSING_CLIENT = "Closing client...";

  public static final String MSG_CLIENT_STOPPED = "Client stopped.";

  /** The Constant THREAD_NAME_CLN. */
  private static final String THREAD_NAME_CLN = "client-";

  public static final String MSG_WRONG_FORMAT_OR_COMMAND_2 = "\" not supported.";

  public static final String MSG_WRONG_FORMAT_OR_COMMAND_1 = "Wrong format or command \"";

}
