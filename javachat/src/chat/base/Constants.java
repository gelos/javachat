package chat.base;

import chat.server.Server;

public final class Constants {

	public static final String MSG_OPEN_CONNECTION = "Open connection for user ";
	public static final String MSG_ACCPT_CLIENT = "Accepted client connection from ";
	public static final String ERR_USRS_NOT_FOUND = "User(s) not found. Username list: ";
	/** The Constant ERR_NAME_EXISTS_MSG. */
	public static final String ERR_NAME_EXISTS_MSG = "Username already exists or wrong.";
	/** The Constant MSG_WLC_USR. */
	public static final String MSG_WLC_USR = "login";
	/** The Constant MSG_EXIT_USR. */
	public static final String MSG_EXIT_USR = "logout";
	public static final String MSG_CLOSE_CONNECTION = "Close connection for ";
	public final static String MSG_ASK_FOR_USERNAME = "Enter username to start сhat: ";
	public final static String MSG_EMPTY_USRENAME = "Username cannot be empty.";
	public final static String MSG_CANT_CON_SRV = "Can't connect to server " + Server.SERVER_IP + ":"
	+ Server.SERVER_PORT + ". Server not started.";
	/** The Constant DEFAULT_WINDOW_NAME. */
	public static final String DEFAULT_WINDOW_NAME = "Java Chat Client";
	public static final String THREAD_NAME_CLIENT = "client-";
	public static final String WRN_UNKNOWN_COMMAND_MSG = "Unknown command";
	public static final String SERVER_CMD_STOP = "stop";
	public static final String MSG_SERVER_STARTED = "Server started.";
	public static final String MSG_SERVER_STOPPED = "The server is stopped.";
	public static final String MSG_SERVER_STARTING = "Chat server starting...";
	public static final String MSG_COMMAND_TO_SHUTDOWN_SERVER = "Type \"" + SERVER_CMD_STOP
	+ "\" in console to shutdown server.";
	public static final String MSG_STOPPING_CHAT_CLIENT_HANDLERS = "Stopping the chat client handlers...";
	public static final String MSG_STOPPING_SERVER_THREADS = "Stopping server threads...";
	public static final String MSG_CONNECTION_SOCKET_1 = "Server socket on ";
	public static final String MSG_CONNECTION_SOCKET_2 = " port created.";
	public static final String ERR_MSG_CHAT_CLIENT_ACCEPTION_FAILED = "Chat client acception failed.";
	public static final String ERR_MSG_FAILED_TO_CREATE_SERVER_SOCKET = "Failed to create server socket on serverSocketPort ";
	public static final String ERR_PORT_IN_USE_1 = "Port ";
	public static final String ERR_PORT_IN_USE_2 = " already in use.";

}
