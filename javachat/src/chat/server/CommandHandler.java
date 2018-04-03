package chat.server;

import static chat.base.CommandName.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import chat.base.ChatCommand;
import chat.base.ChatUser;
import chat.base.CommandName;
import chat.base.WorkerThread;

/**
 * It implements server side part of chat application for one chat client. Handle input/output
 * streams of client connection. Maintain handler storage and user lists.
 * 
 * @see ChatServer
 */
public class CommandHandler extends WorkerThread {

  /** The Constant NAME_ERR_MSG. */
  private static final String NAME_ERR_MSG =
      "Client connection failed. Username already exists or wrong.";

  private static final String USR_NOT_FOUND_ERR_MSG = "User(s) not found. Username list: ";

  /** The Constant WLC_USR_MSG. */
  private static final String WLC_USR_MSG = "login";

  /** The Constant EXT_USR_MSG. */
  private static final String EXT_USR_MSG = "logout";

  /** The client socket. */
  private Socket clientSocket = null;

  /** The input stream. */
  private ObjectInputStream inputStream = null;

  /** The output stream. */
  private ObjectOutputStream outputStream = null;

  /** The client session handler storage. */
  // private CopyOnWriteArrayList<CommandHandler> handlerStorage;
  private ConcurrentHashMap<String, CommandHandler> handlerStorage;

  /** The chat user. */
  private ChatUser chatUser = null;

  /** The is session opened flag. */
  private AtomicBoolean isSessionOpened;

  /**
   * Instantiates a new chat handler.
   *
   * @param clientSocket the client socket
   * @param commandHandlers the handler storage
   */
  public CommandHandler(Socket clientSocket, ConcurrentHashMap<String, CommandHandler> commandHandlers) {
    this.clientSocket = clientSocket;
    this.handlerStorage = commandHandlers;
    this.isSessionOpened = new AtomicBoolean(false);
  }

  /**
   * Override {@link java.lang.Thread#run run()} method. Run while open current socket input stream.
   * Process command received from chat client.
   * 
   * @see java.lang.Thread#run()
   * @see ChatServer
   * 
   */
  @Override
  public void run() {

    // add current handler to handler storage
    // handlerStorage.add(this);

    try {

      // prepare and use input and output client socket streams
      inputStream = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
      outputStream =
          new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));

      // print console message about user connection
      String ip = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress())
          .toString().replace("/", "");
      System.out.println("Accepted client connection from " + ip);

      ChatCommand chatCommand;

      // reads commands from current client socket input until handler running
      while ((chatCommand = (ChatCommand) inputStream.readObject()) != null && isRuning()) {

        // ignore all command except CMDENTER while session not opened
        if (!isSessionOpened.get() && chatCommand.getCommandName() != CMDENTER) {
          // TODO log command and ignore it
          continue;
        }

        // chat command processing
        switch (chatCommand.getCommandName()) {

          case CMDERR:
            System.out.println(chatUser.getUsername() + ": error: " + chatCommand.getMessage());
            // TODO save to log
            break;

          case CMDEXIT:
            stop(); // stop current CommandHandler thread (set isRuning() to false)
            break;

          case CMDENTER:

            // get username
            String userName = chatCommand.getPayload();

            // TODO check for username uniquely

            if (!userName.isEmpty()) { // check for empty username

              // add current handler to handler storage and now we can communicate with other chat
              // clients
              // using user name as a key
              handlerStorage.put(userName, this);

              isSessionOpened.set(true); // set flag that current session is opened

              // create new user
              chatUser = new ChatUser(userName);

              // send ok enter command to confirm session opening
              new ChatCommand(CMDOK, "", CMDENTER.toString()).send(outputStream);

              // TODO what if isSessionOpened set to true but we cant send ok enter command to
              // client
              // send to all users usrlst command
              sendToAllChatClients(new ChatCommand(CMDUSRLST, "", getUserNamesInString()));

              // send to all welcome message
              sendToAllChatClients(new ChatCommand(CMDMSG,
                  getCurrentDateTime() + " " + chatUser.getUsername() + " " + WLC_USR_MSG));

              // print to server console
              System.out.println("Open chat session for user " + userName);

            } else {

              // if username is empty send err to client and print to console
              new ChatCommand(CMDERR, NAME_ERR_MSG).send(outputStream);
              System.out.println(NAME_ERR_MSG);
            }
            break;

          case CMDHLP:
            // TODO complete
            break;

          case CMDMSG:
          case CMDPRVMSG:

            // Get user list from payload
            String[] usrList = new String[0];
            if (!chatCommand.getPayload().isEmpty()) {
              usrList = chatCommand.getPayload().split(CMDULDLM.toString());
            }
            
            Set<String> usrSet = new HashSet<String>(Arrays.asList(usrList));

            //System.out.println(usrSet.toString());
            
            // Prepare message
            String message = getCurrentDateTime() + " " + chatUser.getUsername() + ": "
                + chatCommand.getMessage();

            // IF private message recipient list is empty, send message to all clients
            if (usrSet.size() == 0) {
              sendToAllChatClients(new ChatCommand(CMDMSG, message));

              // Send only for recipient user list
            } else {

              // Create storage for not founded user names
              ArrayList<String> notFoundUserList = new ArrayList<String>();

              // Send message to users in list
              for (String key : usrSet) {

                // Search chatHandler by chat user name string
                CommandHandler commandHandler = handlerStorage.get(key);

                // If found send message
                if (commandHandler != null) {
                  new ChatCommand(CMDMSG, message).send(commandHandler.outputStream);;

                  // If not found, add to list
                } else {
                  notFoundUserList.add(key);
                }
              }

              // If not found user list not empty, send error message back to client
              if (!notFoundUserList.isEmpty()) {
                String errMessage =
                    notFoundUserList.toString().replaceAll("\\[|\\]", "").replaceAll(", ", "\t");
                new ChatCommand(CMDERR, USR_NOT_FOUND_ERR_MSG + errMessage).send(outputStream);
              }

            }

            /*
             * // send private message for (CommandHandler chatHandler : handlerStorage) { if
             * ((usrSet.size() == 0) // send message to all user or only to users in private //
             * message user list || (usrSet.size() > 0 &&
             * usrSet.contains(chatHandler.chatUser.getUsername()))) {
             * 
             * String message = getCurrentDateTime() + " " + chatUser.getUsername() + ": " +
             * chatCommand.getMessage(); new ChatCommand(chatCommand.getCommandName(), message)
             * .send(chatHandler.outputStream); } else { // username not found print message to
             * server console System.out.println("Command \"" + chatCommand.toString() +
             * "\". Username " + chatHandler.chatUser.getUsername() + " not found"); } }
             */
            break;

          default:
            // TODO write unknown command to log file;
            // send client unknown command error message and print to console
            String errMessage = "Unknown command " + chatCommand.toString();
            new ChatCommand(CMDERR, errMessage).send(outputStream);
            System.out.println(errMessage);
        }
      }

    } catch (IOException ioe) {

      // TODO properly catch exception
      System.err.println(ioe.getMessage());

    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

    } finally {

      // remove this handler from handlerStorage storage to prevent receiving messages
      // handlerStorage.remove(this);
      if (chatUser != null) {
        handlerStorage.remove(chatUser.getUsername());
      }

      // print console message about closing connection
      String msg = (chatUser != null) ? " from " + chatUser.getUsername() : "";
      System.out.println(msg);

      // send message to all available clients that current user logout
      sendToAllChatClients(new ChatCommand(CMDMSG,
          getCurrentDateTime() + " " + chatUser.getUsername() + " " + EXT_USR_MSG));

      // update user list on available clients
      sendToAllChatClients(new ChatCommand(CMDUSRLST, "", getUserNamesInString()));

      // dispose User object
      chatUser = null;

      // close client socket and associated streams
      if (clientSocket != null) {
        try {
          clientSocket.close();
          clientSocket = null;
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
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
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    return currentTime;
  }

  /**
   * Send chat command to all chat clients.
   *
   * @param command the command to send
   */
  private void sendToAllChatClients(ChatCommand command) {
    for (CommandHandler commandHandler : handlerStorage.values()) {
      command.send(commandHandler.outputStream);
    }
  }

  /**
   * Return all chat user names in one string separated by {@link CommandName#CMDDLM}. Used in
   * {@link CommandName#CMD_USRLST usrlst} command.
   *
   * @return the string of user names
   */
  private String getUserNamesInString() {

    // TODO refactor to return key set from hashmap
    String res = "";
    String username = "";

    for (CommandHandler commandHandler : handlerStorage.values()) {
      // if we can't get chatUser object ignore it
      if ((commandHandler.chatUser != null)
          && (username = commandHandler.chatUser.getUsername()) != null) {
        res += (res.length() == 0) ? username : CMDDLM + username;
      }
    }
    return res;
  }


  // Send to all users except current updated usrlst command
  /*
   * HashSet<CommandHandler> excludeChatHandler = new HashSet<>(); excludeChatHandler.add(this);
   * sendToAllChatClients(new ChatCommand(CMDUSRLST, "", getUserNamesInString()),
   * excludeChatHandler);
   */

  // Send to all exit message

  /*
   * sendToAllChatClients(new ChatCommand(CMDMSG, currentTime + " " + chatUser.getUsername() + " " +
   * EXT_USR_MSG), excludeChatHandler);
   */

  /**
   * Send to all chat clients.
   *
   * @param command the command
   * @param excludeChatHandlerList the exclude chat handler list
   */
  /*
   * private void sendToAllChatClients(ChatCommand command, Set<CommandHandler> excludeChatHandlerList)
   * { for (CommandHandler chatHandler : handlerStorage) { if
   * (!excludeChatHandlerList.contains(chatHandler)) { command.send(chatHandler.outputStream); } } }
   */

}

