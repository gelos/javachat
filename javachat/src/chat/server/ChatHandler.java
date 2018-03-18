package chat.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
public class ChatHandler extends WorkerThread {

  /** The Constant NAME_ERR_MSG. */
  private static final String NAME_ERR_MSG =
      "Client connection failed. Username already exists or wrong.";

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
  private CopyOnWriteArrayList<ChatHandler> handlerStorage;

  /** The chat user. */
  private ChatUser chatUser = null;

  /** The is session opened flag. */
  private AtomicBoolean isSessionOpened;

  /**
   * Instantiates a new chat handler.
   *
   * @param clientSocket the client socket
   * @param handlerStorage the handler storage
   */
  public ChatHandler(Socket clientSocket, CopyOnWriteArrayList<ChatHandler> handlerStorage) {
    this.clientSocket = clientSocket;
    this.handlerStorage = handlerStorage;
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
    handlerStorage.add(this);

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
        if (!isSessionOpened.get() && chatCommand.getCommandName() != CommandName.CMDENTER) {
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
            stop(); // stop current ChatHandler thread (set isRuning() to false)
            break;

          case CMDENTER:
            // TODO check for username uniquely

            // get username
            String userName = chatCommand.getPayload();

            if (!userName.isEmpty()) { // check for empty username

              isSessionOpened.set(true); // set flag that current session is opened

              // create new user
              chatUser = new ChatUser(userName);

              // send ok enter command to confirm session opening
              new ChatCommand(CommandName.CMDOK, "", CommandName.CMDENTER.toString())
                  .send(outputStream);

              // TODO what if isSessionOpened set to true but we cant send ok enter command to
              // client
              // send to all users usrlst command
              sendToAllChatClients(
                  new ChatCommand(CommandName.CMDUSRLST, "", getUserNamesInString()));

              // send to all welcome message
              sendToAllChatClients(new ChatCommand(CommandName.CMDMSG,
                  getCurrentDateTime() + " " + chatUser.getUsername() + " " + WLC_USR_MSG));

              // print to server console
              System.out.println("Open chat session for user " + userName);

            } else {

              // if username is empty send err to client and print to console
              new ChatCommand(CommandName.CMDERR, NAME_ERR_MSG).send(outputStream);
              System.out.println(NAME_ERR_MSG);
            }
            break;

          case CMDHLP:
            // TODO complete
            break;

          case CMDMSG:
          case CMDPRVMSG:

            // TODO complete send PRVMSG
            // get user list from payload
            // String[] usrList = chatCommand.getPayload().split(" ", 1);

            String[] usrList = new String[0];

            if (!chatCommand.getPayload().isEmpty()) {
              usrList = chatCommand.getPayload().split(" ", 1);
            }
            Set<String> set = new HashSet<String>(Arrays.asList(usrList));

            // Write pre-read string to all clients output using handler storage
            for (ChatHandler chatHandler : handlerStorage) {
              if ((set.size() == 0) // send message to all user or only to users in private
                                    // message user list
                  || (set.size() > 0 && set.contains(chatHandler.chatUser.getUsername()))) {

                String message = getCurrentDateTime() + " " + chatUser.getUsername() + ": "
                    + chatCommand.getMessage();
                new ChatCommand(chatCommand.getCommandName(), message)
                    .send(chatHandler.outputStream);
              }
            }
            break;

          default:
            // TODO write unknown command to log file;
            // send client unknown command error message and print to console
            String errMessage = "Unknown command " + chatCommand.toString();
            new ChatCommand(CommandName.CMDERR, errMessage).send(outputStream);
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
      handlerStorage.remove(this);

      // print console message about closing connection 
      String msg = (chatUser != null) ? " from " + chatUser.getUsername() : "";
      System.out.println(msg);
      
      // send message to all available clients that current user logout 
      sendToAllChatClients(new ChatCommand(CommandName.CMDMSG,
          getCurrentDateTime() + " " + chatUser.getUsername() + " " + EXT_USR_MSG));

      // update user list on available clients
      sendToAllChatClients(new ChatCommand(CommandName.CMDUSRLST, "", getUserNamesInString()));
      
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
    for (ChatHandler chatHandler : handlerStorage) {
      command.send(chatHandler.outputStream);
    }
  }

  /**
   * Return all chat user names in one string separated by {@link CommandName#CMDDLM}. Used in
   * {@link CommandName#CMD_USRLST usrlst} command.
   *
   * @return the string of user names
   */
  private String getUserNamesInString() {
    String res = "";
    String username = "";

    for (ChatHandler chatHandler : handlerStorage) {
      // if we can't get chatUser object ignore it
      if ((chatHandler.chatUser != null)
          && (username = chatHandler.chatUser.getUsername()) != null) {
        res += (res.length() == 0) ? username : CommandName.CMDDLM + username;
      }
    }
    return res;
  }


  // Send to all users except current updated usrlst command
  /*
   * HashSet<ChatHandler> excludeChatHandler = new HashSet<>(); excludeChatHandler.add(this);
   * sendToAllChatClients(new ChatCommand(CommandName.CMDUSRLST, "", getUserNamesInString()),
   * excludeChatHandler);
   */

  // Send to all exit message

  /*
   * sendToAllChatClients(new ChatCommand(CommandName.CMDMSG, currentTime + " " +
   * chatUser.getUsername() + " " + EXT_USR_MSG), excludeChatHandler);
   */

  /**
   * Send to all chat clients.
   *
   * @param command the command
   * @param excludeChatHandlerList the exclude chat handler list
   */
  /*
   * private void sendToAllChatClients(ChatCommand command, Set<ChatHandler> excludeChatHandlerList)
   * { for (ChatHandler chatHandler : handlerStorage) { if
   * (!excludeChatHandlerList.contains(chatHandler)) { command.send(chatHandler.outputStream); } } }
   */

}

