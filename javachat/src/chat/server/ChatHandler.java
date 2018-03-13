/*
 * 
 */
package chat.server;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import chat.base.ChatCommand;
import chat.base.ChatUser;
import chat.base.CommandName;
import chat.base.WorkerThread;

// TODO: Auto-generated Javadoc
/**
 * Implements server side part of chat application. Handle input/output streams of client socket
 * connection. Maintain handler storage.
 * 
 * @see ChatServer
 */
public class ChatHandler extends WorkerThread {

  /** The Constant NAME_ERR_MSG. */
  private static final String NAME_ERR_MSG =
      "Client connection failed. Username already exists or wrong.";

  private static final String WLC_USR_MSG = "login";

  private static final String EXT_USR_MSG = "logout";

  /** The client socket. */
  private Socket clientSocket;

  /** The Buffer to cache client input stream. */
  private ObjectInputStream inputStream = null;

  /** The outputStream. */
  private ObjectOutputStream outputStream = null;

  /** The client session handlerStorage storage. */
  // private CopyOnWriteArrayList<ChatHandler> handlerStorage = new
  // CopyOnWriteArrayList<ChatHandler>();
  private CopyOnWriteArrayList<ChatHandler> handlerStorage;

  /** The chat user. */
  private ChatUser chatUser;

  /*
   * public final String getChatUserName() { return chatUser.getUsername(); }
   */

  private AtomicBoolean isSessionOpened;

  /**
   * Instantiates a new chat handler.
   *
   * @param clientSocket the client socket
   * @param handlerStorage the handlerStorage storage
   */
  public ChatHandler(Socket s, CopyOnWriteArrayList<ChatHandler> handlers) {
    this.clientSocket = s;
    this.handlerStorage = handlers;
    this.isSessionOpened = new AtomicBoolean(false);
  }


  /**
   * Override {@link java.lang.Thread#run run()} method Run while open current socket input stream.
   * Write all string from current input socket to server console and all clients using handler
   * storage. Use {@link #BufferReader()} to cache current socket input stream.
   * 
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {

    // Add current handler to handler storage
    handlerStorage.add(this);

    try {

      // Prepare and use input and output client socket flows
      inputStream = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
      outputStream =
          new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));

      String ip = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress())
          .toString().replace("/", "");
      System.out.println("Accepted client connection from " + ip);

      ChatCommand chatCommand;

      // Read all strings from current client socket input
      while ((chatCommand = (ChatCommand) inputStream.readObject()) != null && isRuning()) {

        String currentTime =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        /*
         * System.out.println("ChatHandler.run() " + chatCommand);
         * System.out.println((!isSessionOpened.get()));
         */

        if (!isSessionOpened.get() && chatCommand.getCommandName() != CommandName.CMDENTER) {
          // TODO log command and ignore it
          /*
           * System.out.println("ChatHandler.run() ignoring " + chatCommand);
           * System.out.println((!isSessionOpened.get()));
           * System.out.println((chatCommand.getCommand() != CommandName.CMDENTER));
           */
          continue;
        }

        switch (chatCommand.getCommandName()) {

          case CMDERR:
            // TODO complete
            break;

          case CMDEXIT:

            // Remove this handler from handlerStorage storage
            // handlerStorage.remove(this);

            // Send to all users usrlst command

            HashSet<ChatHandler> excludeChatHandler = new HashSet<>();
            excludeChatHandler.add(this);
            sendToAllChatClients(new ChatCommand(CommandName.CMDUSRLST, "", getUserNamesInString()),
                excludeChatHandler);

            // sendToAllChatClients(
            // new ChatCommand(CommandName.CMDUSRLST, "", getUserNamesInString()));

            // Send to all exit message

            sendToAllChatClients(
                new ChatCommand(CommandName.CMDMSG,
                    currentTime + " " + chatUser.getUsername() + " " + EXT_USR_MSG),
                excludeChatHandler);

            //sendToAllChatClients(new ChatCommand(CommandName.CMDMSG,
            //    currentTime + " " + chatUser.getUsername() + " " + EXT_USR_MSG));

            stop();
            break;

          case CMDENTER:
            // TODO check for username uniquely

            String userName = chatCommand.getPayload();

            if (!userName.isEmpty()) {

              isSessionOpened.set(true);

              // create new user
              chatUser = new ChatUser(userName);

              // Send ok enter command to confirm session opening
              new ChatCommand(CommandName.CMDOK, "", CommandName.CMDENTER.toString())
                  .send(outputStream);

              // Send to all users usrlst command
              sendToAllChatClients(
                  new ChatCommand(CommandName.CMDUSRLST, "", getUserNamesInString()));

              // Send to all welcome message
              sendToAllChatClients(new ChatCommand(CommandName.CMDMSG,
                  currentTime + " " + chatUser.getUsername() + " " + WLC_USR_MSG));

              System.out.println("Open chat session for user " + userName);

            } else {
              new ChatCommand(CommandName.CMDERR, "", NAME_ERR_MSG).send(outputStream);
              System.out.println(NAME_ERR_MSG);
            }

            break;

          case CMDHLP:
            // TODO complete
            break;

          case CMDMSG:
          case CMDPRVMSG:

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

                String message =
                    currentTime + " " + chatUser.getUsername() + ": " + chatCommand.getMessage();
                new ChatCommand(chatCommand.getCommandName(), message).send(chatHandler.outputStream);
              }
            }

            break;

          default:
            // TODO write unknown command to log file;
            System.out.println("Unknown command " + chatCommand.toString());
        }
      }

    } catch (IOException ioe) {

      System.err.println(ioe.getMessage());

    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {

      // Remove this handler from handlerStorage storage
      handlerStorage.remove(this);

      // dispose User object
      chatUser = null;

      closeInputStream();
      closeOutputStream();
      closeSocket();

    }
  }

  private void sendToAllChatClients(ChatCommand command, Set<ChatHandler> excludeChatHandlerList) {
    for (ChatHandler chatHandler : handlerStorage) {
      if (!excludeChatHandlerList.contains(chatHandler)) {
        command.send(chatHandler.outputStream);
      }
    }
  }

  private void sendToAllChatClients(ChatCommand command) {
    for (ChatHandler chatHandler : handlerStorage) {
      command.send(chatHandler.outputStream);
    }
  }


  private boolean closeInputStream() {
    boolean res = true;
    if (inputStream != null) {
      try {
        inputStream.close();
        inputStream = null;
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        res = false;
      }
    }
    return res;
  }

  private void closeOutputStream() {
    if (outputStream != null) {
      try {
        outputStream.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      outputStream = null;
    }
  }

  private boolean closeSocket() {
    boolean res = true;
    if (clientSocket != null) {
      try {
        clientSocket.close();
        clientSocket = null;
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        res = false;
      }
    }
    return res;
  }

  /**
   * Return the all chat user names in one string. Used in {@link CommandParser#CMD_USRLST usrlst}
   * command.
   *
   * @return the string of user name
   */
  private String getUserNamesInString() {
    String res = "";
    String username = "";

    // TODO check for thread consistency in operation with ChatHandler

    for (ChatHandler chatHandler : handlerStorage) {
      if ((chatHandler.chatUser != null)
          && (username = chatHandler.chatUser.getUsername()) != null) {
        // res += " " + chatHandler.chatUser.getUsername();
        res += " " + username;
      }
    }
    return res.trim();
  }

  /*
   * private void sendExitCmdToClient(ChatHandler chatHandler) { new
   * ChatCommand(CommandName.CMDEXIT, "");
   * 
   * }
   */

}

