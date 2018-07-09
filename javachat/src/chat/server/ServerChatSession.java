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
import org.slf4j.MDC;
import chat.base.ChatSession;
import chat.base.Command;
import chat.base.CommandHandler_new;
import chat.base.CommandName;
import chat.base.Constants;
import chat.base.User;

public class ServerChatSession extends ChatSession {

  private static final String THREAD_NAME_SRV = "server-";

  /** The client session handler storage. */
  //private ConcurrentHashMap<String, ServerChatSession> serverCommandHandlers;
  //private ConcurrentHashMap<String, ChatSession> serverCommandHandlers;
  private ConcurrentHashMap<String, ServerCommandHandler> serverCommandHandlers;

  private CommandHandler_new commandHandler = null;

  public final CommandHandler_new getCommandHandler() {
    return commandHandler;
  }

  public ServerChatSession(Socket clientSocket,
      ConcurrentHashMap<String, ChatSession> serverCommandHandlers) {
    super();
//    this.serverCommandHandlers = serverCommandHandlers;
    commandHandler = new CommandHandler_new(clientSocket, this);
    commandHandler.start(THREAD_NAME_SRV);
  }

  @Override
  public void processCommand(Command Command) {
    // loggerDebugMDC.debug(Command.toString());

    // ignore all command except CMDENTER while session not opened
    if (!getIsSessionOpenedFlag() && Command.getCommandName() != CMDENTER) {
      // loggerRoot.debug("processCommand(Command) - end"); //$NON-NLS-1$
      return;
    }

    // chat command processing
    switch (Command.getCommandName()) {

      case CMDERR:
        // TODO test it with unit test
        System.err.println(user.getUsername() + ": error: " + Command.getMessage());
        // getView().show WarningWindow(command.toString(), WRN_UNKNOWN_COMMAND_MSG);
        // logger.error("ProcessCommandThread.run() {}", Command.getMessage());
        break;

      case CMDEXIT:
        // TODO

        commandHandler.stop();
        try {
          commandHandler.getThread().join();
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        // stop(); // stop current ServerCommandHandler thread (set isRuning() to false)
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
         // serverCommandHandlers.put(userName, this);

          isSessionOpenedFlag.set(true); // set flag that current session is opened
          // isChatSessionOpenedFlag.set(true);

          // create new user
          this.user = new User(userName);

          // send ok enter command to confirm session opening
          new Command(CMDOK, "", CMDENTER.toString()).send(commandHandler.outputStream);

          // TODO what if isSessionOpenedFlag set to true but we cant send ok enter command to
          // client check with unit tests
          // send to all users usrlst command
          sendToAllChatClients(new Command(CMDUSRLST, "", getUserNamesListInString()));

          // send to all welcome message
          sendToAllChatClients(new Command(CMDMSG,
              getCurrentDateTime() + " " + user.getUsername() + " " + MSG_WLC_USR));

          // print to server console
          String msg = MSG_OPEN_CONNECTION + userName;
          // logger.info("run() {}", msg);
          System.out.println(msg);

        } else {

          // if username is empty send error to client, print to console and save to log
          String msg = ERR_NAME_EXISTS_MSG + " " + userName;
          new Command(CMDERR, msg).send(commandHandler.outputStream);
          // TODO add logger to the class
          // logger.warn("ServerCommandHandler.processCommand() {}", msg);
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
        String message =
            getCurrentDateTime() + " " + user.getUsername() + ": " + Command.getMessage();

        // IF private message recipient list is empty, send message to all clients
        if (usrSet.size() == 0) {
          sendToAllChatClients(new Command(CMDMSG, message));

          // Send only for recipient user list
        } else {

          // Add sender to recipient list
          usrSet.add(user.getUsername());

          // System.out.println("ServerCommandHandler.run()" + usrSet.toString());

          // Create storage for not founded user names
          ArrayList<String> notFoundUserList = new ArrayList<String>();

          // Send message to users in list
          for (String key : usrSet) {

            // Search chatHandler by chat user name string
            //ServerChatSession serverCommandHandler = serverCommandHandlers.get(key);
            ServerCommandHandler serverCommandHandler = serverCommandHandlers.get(key);

            // If found send message
            if (serverCommandHandler != null) {
              //new Command(CMDMSG, message)
               //   .send(serverCommandHandler.getCommandHandler().outputStream);;
              new Command(CMDMSG, message)
                 .send(serverCommandHandler.outputStream);;

              // If not found, add to list
            } else {
              notFoundUserList.add(key);
            }
          }

          // If not found user list not empty, send error message back to client
          if (!notFoundUserList.isEmpty()) {
            String errMessage = notFoundUserList.toString().replaceAll("\\[|\\]", "")
                .replaceAll(", ", CMDULDLM.toString());
            System.out.println("ServerCommandHandler.run()" + notFoundUserList.toString());
            new Command(CMDERR, ERR_USRS_NOT_FOUND + errMessage).send(commandHandler.outputStream);
          }

        }

        /*
         * // send private message for (ServerCommandHandler chatHandler : serverCommandHandlers) {
         * if ((usrSet.size() == 0) // send message to all user or only to users in private //
         * message user list || (usrSet.size() > 0 &&
         * usrSet.contains(chatHandler.chatUser.getUsername()))) {
         * 
         * String message = getCurrentDateTime() + " " + user.getUsername() + ": " +
         * chatCommand.getMessage(); new Command(chatCommand.getCommandName(), message)
         * .send(chatHandler.outputStream); } else { // username not found print message to server
         * console System.out.println("Command \"" + chatCommand.toString() + "\". Username " +
         * chatHandler.chatUser.getUsername() + " not found"); } }
         */
        break;

      default:
        String errMessage = Constants.WRN_UNKNOWN_COMMAND_MSG + " " + Command;
        new Command(CMDERR, errMessage).send(commandHandler.outputStream);
        // logger.warn(errMessage);
        System.out.println(errMessage);
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

    // return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd
    // HH:mm:ss"));
    // return LocalDateTime.now().toString();
    // return "1";
  }

  /**
   * Send chat command to all chat clients.
   *
   * @param Command the command to send
   */
  private void sendToAllChatClients(Command Command) {
    //for (ServerChatSession serverCommandHandler : serverCommandHandlers.values()) {
    for (ServerCommandHandler serverCommandHandler : serverCommandHandlers.values()) {
      //Command.send(serverCommandHandler.getCommandHandler().outputStream);
      Command.send(serverCommandHandler.outputStream);
    }
  }

  /**
   * Return all chat user names in one string separated by {@link CommandName#CMDDLM}. Used in
   * {@link CommandName#CMD_USRLST usrlst} command.
   *
   * @return the list of user names in string
   */
  private String getUserNamesListInString() {

    return serverCommandHandlers.keySet().toString().replaceAll("\\[|\\]", "").replaceAll(", ",
        CMDDLM.toString());
  }

}
