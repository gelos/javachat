package chat.server;

import static chat.base.CommandName.CMDDLM;
import static chat.base.CommandName.CMDENTER;
import static chat.base.CommandName.CMDERR;
import static chat.base.CommandName.CMDMSG;
import static chat.base.CommandName.CMDOK;
import static chat.base.CommandName.CMDULDLM;
import static chat.base.CommandName.CMDUSRLST;
import static chat.base.Constants.ERR_NAME_EXISTS_MSG;
import static chat.base.Constants.ERR_USRS_NOT_FOUND;
import static chat.base.Constants.MSG_CLOSE_CONNECTION;
import static chat.base.Constants.MSG_EXIT_USR;
import static chat.base.Constants.MSG_OPEN_CONNECTION;
import static chat.base.Constants.MSG_WLC_USR;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import chat.base.ChatSession;
import chat.base.Command;
import chat.base.CommandName;
import chat.base.Constants;
import chat.base.User;

// TODO: Auto-generated Javadoc
/**
 * The Class ServerChatSession. Chat session on server side. Extends {@link ChatSession}.
 */
public class ServerChatSession extends ChatSession {

  /** The session handlers thread-safe storage. */
  private ConcurrentHashMap<String, ChatSession> chatSessionStorage;

  /**
   * Instantiates a new server chat session.
   *
   * @param clientSocket the client socket
   * @param chatSessionStorage the chat session storage
   */
  public ServerChatSession(Socket clientSocket,
      ConcurrentHashMap<String, ChatSession> chatSessionStorage) {

    // Create ChatSession with server tag in ComandHandler thread name
    super(Constants.THREAD_NAME_SRV);
    runCommandHandler(clientSocket);

    this.chatSessionStorage = chatSessionStorage;
  }

  /**
   * Server implementation of {@link ChatSession#open(String)} method.
   *
   * @param userName the user name
   * @see chat.base.ChatSession#open(String)
   */
  @Override
  public void open(String userName) {

    super.open(userName);

    if (isUserNameValid(userName)) {

      // Adds current ChatSession to sessions storage and now we can communicate with other chat
      // clients using user name as a key
      chatSessionStorage.put(userName, this);

      // Set flag that current session is opened
      isSessionOpenedFlag.set(true);

      // Create new user
      this.user = new User(userName);

      // Send OK command with ENTER payload to confirm session opening
      send(new Command(CMDOK, "", CMDENTER.toString()));

      // Send command to update all chat client user lists
      sendToAllChatClients(new Command(CMDUSRLST, "", getUserNamesListInString()));

      // Send to all chat clients welcome message
      sendToAllChatClients(
          new Command(CMDMSG, getCurrentDateTime() + " " + user.getUsername() + " " + MSG_WLC_USR));

      // Print to server console & save to log
      String msg = MSG_OPEN_CONNECTION + userName;
      logger.info(this.getClass().getSimpleName() + "." + "openSession() {}", msg);
      System.out.println(msg);

    } else {

      // If username not valid send error command to client, print to console and save to log
      String msg = ERR_NAME_EXISTS_MSG + " " + userName;
      send(new Command(CMDERR, msg));
      logger.error(this.getClass().getSimpleName() + "." + "openSession() {}", msg);
      System.out.println(msg);

    }

  }

  /**
   * Server implementation of {@link ChatSession#receive(Command)} method.
   *
   * @param command the command
   * @see chat.base.ChatSession#receive(chat.base.Command)
   */
  @Override
  public void receive(Command command) {

    super.receive(command);

    // Ignoring all commands except CMDENTER while session not open
    if (!getIsSessionOpenedFlag() && command.getCommandName() != CMDENTER) {
      logger.warn(this.getClass().getSimpleName() + "." + "processCommand(Command) {}", //$NON-NLS-1$
          Constants.MSG_SESSION_NOT_OPENED_BUT_COMMAND_RECEIVED + " " + command);
      return;
    }

    String errMessage = "";

    switch (command.getCommandName()) {

      case CMDERR:

        errMessage = user.getUsername() + " " + command.getMessage();

        System.err.println(errMessage);

        logger.error(this.getClass().getSimpleName() + "." + "processCommand() {}", errMessage);
        break;

      case CMDEXIT:

        // if EXT command received, closing session without sending EXT command back
        boolean sendEXTCMD = false;
        close(sendEXTCMD);
        break;

      case CMDENTER:

        String userName = command.getPayload();
        open(userName);
        break;

      case CMDHLP:
        // TODO complete
        System.out.println(Constants.MSG_COMMAND_NOT_IMPLEMENTED_YET + " " + CommandName.CMDHLP);
        break;

      case CMDMSG:
      case CMDPRVMSG:

        processMSGCommand(command);
        break;

      default:

        errMessage = Constants.WRN_UNKNOWN_COMMAND_MSG + " " + command;
        send(new Command(CMDERR, errMessage));
        loggerDebugMDC.debug(this.getClass().getSimpleName() + "." + "processCommand() {}",
            errMessage);

    }

  }

  /**
   * Server implementation of {@link ChatSession#close(boolean)} method.
   *
   * @param sendEXTCMD the send EXTCMD
   * @see chat.base.ChatSession#close(boolean)
   */
  @Override
  public void close(boolean sendEXTCMD) {

    // First of all we remove this handler from chatSessionStorage storage to
    // prevent receiving messages
    if (user != null) {
      chatSessionStorage.remove(user.getUsername());
    }

    // print console message about closing connection
    String msg = (user != null) ? user.getUsername() : "";
    msg = MSG_CLOSE_CONNECTION + msg;
    System.out.println(msg);

    // Send a message to all clients about the current user's exit
    sendToAllChatClients(
        new Command(CMDMSG, getCurrentDateTime() + " " + user.getUsername() + " " + MSG_EXIT_USR));

    // Send update user list command
    sendToAllChatClients(new Command(CMDUSRLST, "", getUserNamesListInString()));

    super.close(sendEXTCMD);

  }

  /**
   * Checks if is user name valid.
   *
   * @param userName the user name
   * @return true, if is user name valid & unique
   */
  private boolean isUserNameValid(String userName) {
    // TODO check for username uniqueness
    return !userName.isEmpty();
  }

  /**
   * Process MSG command.
   *
   * @param command the command
   */
  private void processMSGCommand(Command command) {

    // Get user list from payload
    String[] usrList = new String[0];
    if (!command.getPayload().isEmpty()) {
      usrList = command.getPayload().split(CMDULDLM.toString());
    }

    Set<String> usrSet = new HashSet<String>(Arrays.asList(usrList));

    // Prepare message
    String message = getCurrentDateTime() + " " + user.getUsername() + ": " + command.getMessage();

    // IF private message recipient list is empty, send message to all clients
    if (usrSet.size() == 0) {
      sendToAllChatClients(new Command(CMDMSG, message));

      // Send only for recipient user list
    } else {

      // Add sender to recipient list
      usrSet.add(user.getUsername());

      // Create storage for not founded user names
      ArrayList<String> notFoundUserList = new ArrayList<String>();

      // Send message to users in list
      for (String key : usrSet) {

        // Search chatHandler by chat user name string
        ChatSession serverCommandHandler = chatSessionStorage.get(key);

        // If found send message
        if (serverCommandHandler != null) {

          serverCommandHandler.send(new Command(CMDMSG, message));

          // If not found, add to list
        } else {
          notFoundUserList.add(key);
        }
      }

      // If not found user list not empty, send error message back to client
      if (!notFoundUserList.isEmpty()) {
        String errMessage = ERR_USRS_NOT_FOUND + notFoundUserList.toString()
            .replaceAll("\\[|\\]", "").replaceAll(", ", CMDULDLM.toString());

        loggerDebugMDC.debug(this.getClass().getSimpleName() + "." + "processMSGCommand() {} ",
            errMessage);
        send(new Command(CMDERR, errMessage));
      }
    }
  }

  /**
   * Gets the current date time.
   *
   * @return the current date time string
   */
  private String getCurrentDateTime() {

    String currentTime =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    return currentTime;

  }

  /**
   * Send chat command to all chat clients.
   *
   * @param command the command to send
   */
  private void sendToAllChatClients(Command command) {

    for (ChatSession serverCommandHandler : chatSessionStorage.values()) {
      serverCommandHandler.send(command);
    }

  }

  /**
   * Return all chat user names in one string separated by {@link CommandName#CMDDLM}. Used in
   * {@link CommandName#CMD_USRLST usrlst} command.
   *
   * @return the list of user names in string
   */
  private String getUserNamesListInString() {

    return chatSessionStorage.keySet().toString().replaceAll("\\[|\\]", "").replaceAll(", ",
        CMDDLM.toString());
  }

}
