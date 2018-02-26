/*
 * 
 */
package chat.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
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

  /** The client session handlers storage. */
  //private CopyOnWriteArrayList<ChatHandler> handlers = new CopyOnWriteArrayList<ChatHandler>();
  private CopyOnWriteArrayList<ChatHandler> handlers;

  /** The chat user. */
  private ChatUser chatUser;

  /**
   * Instantiates a new chat handler.
   *
   * @param clientSocket the client socket
   * @param handlers the handlers storage
   */
  public ChatHandler(Socket s, CopyOnWriteArrayList<ChatHandler> handlers) {
    this.clientSocket = s;
    this.handlers = handlers;
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
    handlers.add(this);

    try {

      // Prepare and use input and output client socket flows
      inputStream = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
      outputStream =
          new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));

      // check for enter command and read username
      String userName = waitForEnterCmd(inputStream);

      // if username not empty
      if (!userName.isEmpty()) {

        // TODO check for username uniquety

        // create new user
        chatUser = new ChatUser(userName);

        // Send ok enter command to confirm session opening
        new ChatCommand(CommandName.CMDOK, "", CommandName.CMDENTER.toString()).send(outputStream);

        // Send to all users usrlst command
        sendToAllChatClients(new ChatCommand(CommandName.CMDUSRLST, "", getUserNamesInString()));

        String currentTime =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Send to all welcome message
        sendToAllChatClients(new ChatCommand(CommandName.CMDMSG,
            currentTime + " " + chatUser.getUsername() + " " + WLC_USR_MSG));

        ChatCommand chatCommand;

        // Read all strings from current client socket input
        while ((chatCommand = (ChatCommand) inputStream.readObject()) != null && isRuning()) {

          currentTime =
              LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

          switch (chatCommand.getCommand()) {

            case CMDERR:
              //TODO complete
                break;
            
            case CMDEXIT:

              // Send to all exit message
              sendToAllChatClients(new ChatCommand(CommandName.CMDMSG,
                  currentTime + " " + chatUser.getUsername() + " " + EXT_USR_MSG));

              // Send to all users usrlst command
              sendToAllChatClients(
                  new ChatCommand(CommandName.CMDUSRLST, "", getUserNamesInString()));

              break;
                         
            case CMDENTER:
              //TODO remove wait for Enter command method
              break;
                
            case CMDHLP:
              //TODO complete
              break;
                         
            case CMDMSG:
            case CMDPRVMSG:

              // get user list from payload
              String[] usrList = chatCommand.getPayload().split(" ");
              Set<String> set = new HashSet<String>(Arrays.asList(usrList));

              // Write pre-read string to all clients output using handler storage
              for (ChatHandler chatHandler : handlers) {
                if ((set.size() == 0) // send message to all user or only to users in private
                                      // message user list
                    || (set.size() > 0 && set.contains(chatHandler.chatUser.getUsername()))) {

                  String message =
                      currentTime + " " + chatUser.getUsername() + ": " + chatCommand.getMessage();
                  new ChatCommand(chatCommand.getCommand(), message).send(chatHandler.outputStream);
                }
              }

              break;
            default:
              // TODO write uncnown command to log file;
              System.out.println("Unknown command " + chatCommand.toString());  
          }
        }
      } else if (isRuning()) { // if username empty send to client error message and exit command

        // ChatUtils.sendCommand(new ChatCommand(CommandName.CMDERR, NAME_ERR_MSG), outputStream);
        new ChatCommand(CommandName.CMDERR, "", NAME_ERR_MSG).send(outputStream);

        System.out.println(NAME_ERR_MSG);
      }

      // ChatUtils.sendCommand(new ChatCommand(CommandName.CMDEXIT, NAME_ERR_MSG), outputStream);
      new ChatCommand(CommandName.CMDERR, "", NAME_ERR_MSG).send(outputStream);

    } catch (IOException ioe) {

      System.err.println(ioe.getMessage());

    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {

      // dispose User object
      chatUser = null;

      closeInputStream();
      closeOutputStream();
      closeSocket();

      // Remove this handler from handlers storage
      handlers.remove(this);
    }
  }

  private void sendToAllChatClients(ChatCommand command) {
    for (ChatHandler chatHandler : handlers) {
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
    for (ChatHandler chatHandler : handlers) {
      res += " " + chatHandler.chatUser.getUsername();
    }
    return res.trim();
  }

  /**
   * Read input stream while not received {@link CommandParser#CMD_ENTER enter} command from chat
   * client command.
   *
   * @param inputStream the BufferedReader of input chat client stream
   * @return the user name or empty string if an error occurred
   */
  private String waitForEnterCmd(ObjectInputStream inputStream) {

    String userName = "";

    try {

      ChatCommand chatCommand;

      while ((chatCommand = (ChatCommand) inputStream.readObject()) != null && isRuning()) {

        System.out.println(
            "ChatHandler.waitForEnterCmd() read command " + chatCommand.getCommand().toString());

        if (chatCommand.getCommand() == CommandName.CMDENTER) {
          userName = chatCommand.getPayload();
          break;
        }

      }

    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return userName;

  }

}

