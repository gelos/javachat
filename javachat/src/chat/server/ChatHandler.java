/*
 * 
 */
package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CopyOnWriteArrayList;
import chat.base.ChatCommand;
import chat.base.ChatUser;
import chat.base.ChatUtils;
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

  /** The client socket. */
  private Socket clientSocket;

  /** The Buffer to cache client input stream. */
  private BufferedReader inputStream;

  /** The outputStream. */
  private PrintWriter outputStream;

  /** The client session handlers storage. */
  private CopyOnWriteArrayList<ChatHandler> handlers = new CopyOnWriteArrayList<ChatHandler>();

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
      inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      outputStream = new PrintWriter(clientSocket.getOutputStream(), true);

      // check for enter command and read username
      String inputString = waitForEnterCmd(inputStream);

      // if username not empty
      if (!inputString.isEmpty()) {

        // TODO check for username uniquety

        System.out.println("user: " + inputString);

        // create new user
        chatUser = new ChatUser(inputString);

        // send to client usrlst command
        // TODO use serialization
        // https://stackoverflow.com/questions/26245306/send-objects-and-objects-arrays-through-socket

        // Send ok enter command to confirm session opening
        ChatUtils.sendCommand(new ChatCommand(CommandName.CMDOK, CommandName.CMDENTER.toString()),
            outputStream);

        // Send to all users usrlst command
        sendToAllChatClients(new ChatCommand(CommandName.CMDUSRLST, getUserNamesInString()));

        String currentTime =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Send to all welcome message
        sendToAllChatClients(new ChatCommand(CommandName.CMDMSG,
            currentTime + " " + chatUser.getUsername() + " " + WLC_USR_MSG));

        inputString = "";

        // Read all strings from current client socket input
        while ((inputString = inputStream.readLine()) != null && isRuning()) {

          ChatCommand cmd = ChatUtils.parseMessage(inputString);

          switch (cmd.getCommand()) {

            case CMDEXIT:
              // TODO change it

              break;
            case CMDPRVMSG:
            case CMDMSG:
              // Write pre-read string to all clients output using handler storage
              for (ChatHandler ch : handlers) {

                currentTime =
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String payload =
                    currentTime + " " + chatUser.getUsername() + ": " + cmd.getPayload();
                // ch.pw.println(currentTime + " " + chatUser.getUsername() + ": " +
                // cmd.getPayload());
                ChatUtils.sendCommand(new ChatCommand(cmd.getCommand(), payload), ch.outputStream);

              }

              break;
            default:
              break;

          }

          // Write string to server console
          System.out.println(inputString);

        }
      } else if (isRuning()) { // if username empty send to client error message and exit command

        ChatUtils.sendCommand(new ChatCommand(CommandName.CMDERR, NAME_ERR_MSG), outputStream);

        System.out.println(NAME_ERR_MSG);
      }

      ChatUtils.sendCommand(new ChatCommand(CommandName.CMDEXIT, NAME_ERR_MSG), outputStream);

    } catch (IOException ioe) {

      System.err.println(ioe.getMessage());

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
    for (ChatHandler ch : handlers) {
      ChatUtils.sendCommand(command, ch.outputStream);
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
      outputStream.close();
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
    for (ChatHandler ch : handlers) {
      res += " " + ch.chatUser.getUsername();
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
  private String waitForEnterCmd(BufferedReader br) {

    String res = "";
    try {

      String inputString;

      while ((inputString = br.readLine()) != null && isRuning()) {

        ChatCommand cmd = ChatUtils.parseMessage(inputString);

        if (cmd.getCommand() == CommandName.CMDENTER) {
          res = cmd.getPayload();
          break;
        }

      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return res;

  }

}

