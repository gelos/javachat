package chat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static chat.base.CommandName.*;
/*import static chat.base.CommandName.CMDENTER;
import static chat.base.CommandName.CMDERR;
import static chat.base.CommandName.CMDMSG;
import static chat.base.CommandName.CMDOK;
import static chat.base.CommandName.CMDULDLM;
import static chat.base.CommandName.CMDUSRLST;
*/
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.MDC;

import chat.base.Command;
import chat.base.CommandHandler;
import chat.base.CommandName;
import chat.base.Constants;
import chat.base.User;

import static chat.base.Constants.*;

/**
 * It implements server side part of chat application for one chat client.
 * Handle input/output streams of client connection. Maintain handler storage
 * and user lists.
 * 
 * @see Server
 */
public class ServerCommandHandler extends CommandHandler {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ServerCommandHandler.class);

	/** The client session handler storage. */
	private ConcurrentHashMap<String, ServerCommandHandler> serverCommandHandlers;

	/**
	 * Instantiates a new chat handler.
	 *
	 * @param clientSocket
	 *            the client socket
	 * @param serverCommandHandlers
	 *            the handler storage
	 */
	public ServerCommandHandler(Socket clientSocket,
			ConcurrentHashMap<String, ServerCommandHandler> serverCommandHandlers) {
		super(clientSocket);
		this.serverCommandHandlers = serverCommandHandlers;
		// this.isSessionOpened = new AtomicBoolean(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see chat.server.CommandHandler#run()
	 */
	@Override
	public void run() {

		try {

			openInputStream();
			openOutputStream();

			String ip = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString()
					.replace("/", "");
			CommandHandler.logger.info("run() - {}", MSG_ACCPT_CLIENT + ip); //$NON-NLS-1$
			System.out.println(MSG_ACCPT_CLIENT + ip);

			// Reading commands from the current client input socket while the handler is
			// running
			while (isRunning()) {

				Command Command = (Command) inputStream.readObject();
				processCommand(Command);

			}

		} catch (ClassNotFoundException | IOException e) {
			logger.error("run()", e); //$NON-NLS-1$

			CommandHandler.logger.error("run()", e); //$NON-NLS-1$

		} finally {

			// First of all we remove this handler from serverCommandHandlers storage to
			// prevent receiving messages
			if (user != null) {
				serverCommandHandlers.remove(user.getUsername());
			}

			// print console message about closing connection
			String msg = (user != null) ? user.getUsername() : "";
			msg = MSG_CLOSE_CONNECTION + msg;
			CommandHandler.logger.info("run() - {}", msg); //$NON-NLS-1$
			System.out.println(msg);

			// Send a message to all clients about the current user's exit
			sendToAllChatClients(
					new Command(CMDMSG, getCurrentDateTime() + " " + user.getUsername() + " " + MSG_EXIT_USR));

			// Send update user list command
			sendToAllChatClients(new Command(CMDUSRLST, "", getUserNamesListInString()));

			MDC.clear();
			user = null;
			try {
				closeClientSocket();
			} catch (IOException e) {
				logger.error("run()", e); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void stop() {
		super.stop();
		// First close the input stream to release the while circle in run() method
		try {
			closeInputStream();
		} catch (IOException e) {
			logger.error("ServerCommandHandler.stop()", e); //$NON-NLS-1$
		}
	}

	/**
	 * Gets the current date time.
	 *
	 * @return the current date time string
	 */
	String getCurrentDateTime() {

		String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
		return currentTime;

		// return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd
		// HH:mm:ss"));
		// return LocalDateTime.now().toString();
		// return "1";
	}

	/**
	 * Send chat command to all chat clients.
	 *
	 * @param Command
	 *            the command to send
	 */
	void sendToAllChatClients(Command Command) {
		for (ServerCommandHandler serverCommandHandler : serverCommandHandlers.values()) {
			Command.send(serverCommandHandler.outputStream);
		}
	}

	/**
	 * Return all chat user names in one string separated by
	 * {@link CommandName#CMDDLM}. Used in {@link CommandName#CMD_USRLST usrlst}
	 * command.
	 *
	 * @return the list of user names in string
	 */
	String getUserNamesListInString() {

		return serverCommandHandlers.keySet().toString().replaceAll("\\[|\\]", "").replaceAll(", ", CMDDLM.toString());
	}

	@Override
	public void processCommand(Command Command) {
		// loggerRoot.debug("processCommand(Command) - user {}, command {}", ((user ==
		// null) ? "" :
		// user.getUsername()), //$NON-NLS-1$
		// command);

		// System.out.println(((user == null) ? "" : user.getUsername()) + command);

		loggerDebugMDC.debug(Command.toString());

		// ignore all command except CMDENTER while session not opened
		if (!isSessionOpened.get() && Command.getCommandName() != CMDENTER) {
			// loggerRoot.debug("processCommand(Command) - end"); //$NON-NLS-1$
			return;
		}

		// chat command processing
		switch (Command.getCommandName()) {

		case CMDERR:
			// TODO test it with unit test
			System.err.println(user.getUsername() + ": error: " + Command.getMessage());
			// getView().show WarningWindow(command.toString(), WRN_UNKNOWN_COMMAND_MSG);
			logger.error("ProcessCommandThread.run() {}", Command.getMessage());
			break;

		case CMDEXIT:
			stop(); // stop current ServerCommandHandler thread (set isRuning() to false)
			break;

		case CMDENTER:

			// get username
			String userName = Command.getPayload();

			// TODO check for username uniquely

			if (!userName.isEmpty()) { // check for empty username

				// add current handler to handler storage and now we can communicate with other
				// chat
				// clients
				// using user name as a key
				MDC.put("username", userName);
				serverCommandHandlers.put(userName, this);

				isSessionOpened.set(true); // set flag that current session is opened

				// create new user
				user = new User(userName);

				// send ok enter command to confirm session opening
				new Command(CMDOK, "", CMDENTER.toString()).send(outputStream);

				// TODO what if isSessionOpened set to true but we cant send ok enter command to
				// client check with unit tests
				// send to all users usrlst command
				sendToAllChatClients(new Command(CMDUSRLST, "", getUserNamesListInString()));

				// send to all welcome message
				sendToAllChatClients(
						new Command(CMDMSG, getCurrentDateTime() + " " + user.getUsername() + " " + MSG_WLC_USR));

				// print to server console
				String msg = MSG_OPEN_CONNECTION + userName;
				logger.info("run() {}", msg);
				System.out.println(msg);

			} else {

				// if username is empty send error to client, print to console and save to log
				String msg = ERR_NAME_EXISTS_MSG + " " + userName;
				new Command(CMDERR, msg).send(outputStream);
				logger.warn("ServerCommandHandler.processCommand() {}", msg);
				System.out.println(msg);
			}
			break;

		case CMDHLP:
			// TODO complete
			break;

		case CMDMSG:
		case CMDPRVMSG:

			// Get user list from payload
			String[] usrList = new String[0];
			if (!Command.getPayload().isEmpty()) {
				usrList = Command.getPayload().split(CMDULDLM.toString());
			}

			Set<String> usrSet = new HashSet<String>(Arrays.asList(usrList));

			// System.out.println(usrSet.toString());

			// Prepare message
			String message = getCurrentDateTime() + " " + user.getUsername() + ": " + Command.getMessage();

			// IF private message recipient list is empty, send message to all clients
			if (usrSet.size() == 0) {
				sendToAllChatClients(new Command(CMDMSG, message));

				// Send only for recipient user list
			} else {

				// Add sender to recepient list
				usrSet.add(user.getUsername());

				// System.out.println("ServerCommandHandler.run()" + usrSet.toString());

				// Create storage for not founded user names
				ArrayList<String> notFoundUserList = new ArrayList<String>();

				// Send message to users in list
				for (String key : usrSet) {

					// Search chatHandler by chat user name string
					ServerCommandHandler serverCommandHandler = serverCommandHandlers.get(key);

					// If found send message
					if (serverCommandHandler != null) {
						new Command(CMDMSG, message).send(serverCommandHandler.outputStream);
						;

						// If not found, add to list
					} else {
						notFoundUserList.add(key);
					}
				}

				// If not found user list not empty, send error message back to client
				if (!notFoundUserList.isEmpty()) {
					String errMessage = notFoundUserList.toString().replaceAll("\\[|\\]", "").replaceAll(", ",
							CMDULDLM.toString());
					System.out.println("ServerCommandHandler.run()" + notFoundUserList.toString());
					new Command(CMDERR, ERR_USRS_NOT_FOUND + errMessage).send(outputStream);
				}

			}

			/*
			 * // send private message for (ServerCommandHandler chatHandler :
			 * serverCommandHandlers) { if ((usrSet.size() == 0) // send message to all user
			 * or only to users in private // message user list || (usrSet.size() > 0 &&
			 * usrSet.contains(chatHandler.chatUser.getUsername()))) {
			 * 
			 * String message = getCurrentDateTime() + " " + user.getUsername() + ": " +
			 * chatCommand.getMessage(); new Command(chatCommand.getCommandName(), message)
			 * .send(chatHandler.outputStream); } else { // username not found print message
			 * to server console System.out.println("Command \"" + chatCommand.toString() +
			 * "\". Username " + chatHandler.chatUser.getUsername() + " not found"); } }
			 */
			break;

		default:
			String errMessage = Constants.WRN_UNKNOWN_COMMAND_MSG + " " + Command;
			new Command(CMDERR, errMessage).send(outputStream);
			logger.warn(errMessage);
			System.out.println(errMessage);
		}

	}

	// Send to all users except current updated usrlst command
	/*
	 * HashSet<ServerCommandHandler> excludeChatHandler = new HashSet<>();
	 * excludeChatHandler.add(this); sendToAllChatClients(new Command(CMDUSRLST, "",
	 * getUserNamesInString()), excludeChatHandler);
	 */

	// Send to all exit message

	/*
	 * sendToAllChatClients(new Command(CMDMSG, currentTime + " " +
	 * user.getUsername() + " " + MSG_EXIT_USR), excludeChatHandler);
	 */

	/**
	 * Send to all chat clients.
	 *
	 * @param command
	 *            the command
	 * @param excludeChatHandlerList
	 *            the exclude chat handler list
	 */
	/*
	 * private void sendToAllChatClients(Command command, Set<ServerCommandHandler>
	 * excludeChatHandlerList) { for (ServerCommandHandler chatHandler :
	 * serverCommandHandlers) { if (!excludeChatHandlerList.contains(chatHandler)) {
	 * command.send(chatHandler.outputStream); } } }
	 */

}
