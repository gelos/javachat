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

  /** The Constant _ERR_MSG_NAME. */
  private static final String _ERR_MSG_NAME =
      "Client connection failed. Wrong enter command or username is empty.";

  /** The client socket. */
  Socket s;

  /** The Buffer to cache client input stream. */
  BufferedReader br;

  /** The pw. */
  PrintWriter pw;

  /** The temporary String storage. */
  String temp;

  /** The client session handlers storage. */
  // ArrayList<ChatHandler> handlers;
  CopyOnWriteArrayList<ChatHandler> handlers = new CopyOnWriteArrayList<ChatHandler>();

  /** The chat user. */
  public ChatUser chatUser;

  /**
   * Instantiates a new chat handler.
   *
   * @param s the client socket
   * @param handlers the handlers storage
   */
  public ChatHandler(Socket s, CopyOnWriteArrayList<ChatHandler> handlers) {
    this.s = s;
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

    try {

      // Add this handler to handler storage
      handlers.add(this);

      // Prepare and use input and output client socket flows
      br = new BufferedReader(new InputStreamReader(s.getInputStream()));
      pw = new PrintWriter(s.getOutputStream(), true);

      // check for enter command and read username
      temp = waitForEnterCmd(br);

      // if username not empty
      if (!temp.isEmpty()) {

        System.out.println("user: " + temp);

        // create new user
        chatUser = new ChatUser(temp);

        // send to client usrlst command
        // TODO use serialization
        // https://stackoverflow.com/questions/26245306/send-objects-and-objects-arrays-through-socket

        // Send OK Enter command

        ChatUtils.sendCommand(new ChatCommand(CommandName.CMDOK, CommandName.CMDENTER.toString()),
            pw);

        // Send to all users usrlst command
        for (ChatHandler ch : handlers) {
          // ch.pw.println(CommandName.CMDUSRLST.toString() + CommandName.CMDDLM.toString() +
          // getUserNamesInString());
          ChatUtils.sendCommand(new ChatCommand(CommandName.CMDUSRLST, getUserNamesInString()),
              ch.pw);
        }

        temp = "";

        // Read all strings from current client socket input
        while ((temp = br.readLine()) != null && isRuning()) {

          ChatCommand cmd = ChatUtils.parseMessage(temp);

          switch (cmd.getCommand()) {

            case CMDEXIT:
              // TODO change it

              break;
            case CMDPRVMSG:
            case CMDMSG:
              // Write pre-read string to all clients output using handler storage
              for (ChatHandler ch : handlers) {

                String currentTime =
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String payload = currentTime + " " + chatUser.getUsername() + ": " + cmd.getPayload();
                //ch.pw.println(currentTime + " " + chatUser.getUsername() + ": " + cmd.getPayload());
                ChatUtils.sendCommand(new ChatCommand(cmd.getCommand(), payload), ch.pw);

              }

              break;
            default:
              break;

          }

          // Write string to server console
          System.out.println(temp);

        }
      } else if (isRuning()) { // if username empty send to client error message

        this.pw.println(_ERR_MSG_NAME);

        // Write string to server console
        System.out.println(_ERR_MSG_NAME);

      }
    } catch (IOException ioe) {

      System.out.println(ioe.getMessage());

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


  private boolean closeInputStream() {
    boolean res = true;
    if (br != null) {
      try {
        br.close();
        br = null;
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        res = false;
      }
    }
    return res;
  }

  private void closeOutputStream() {
    if (pw != null) {
      pw.close();
      pw = null;
    }
  }

  private boolean closeSocket() {
    boolean res = true;
    if (s != null) {
      try {
        s.close();
        s = null;
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
   * Read first string from chat client input stream and match it with
   * {@link CommandParser#CMD_ENTER enter} command.
   *
   * @param br the BufferedReader of input chat client stream
   * @return the user name or empty string if an error occurred
   */
  private String waitForEnterCmd(BufferedReader br) {

    String res = "";
    try {

      while (isRuning()) {

        // read first line
        temp = br.readLine();

        if (temp != null) {

          // trim spaces
          temp = temp.trim();

          // check if string start from enter command with space and at least one char username
          ChatCommand cmd = ChatUtils.parseMessage(temp);

          if (cmd.getCommand() == CommandName.CMDENTER) {
            res = cmd.getPayload();
            break;
          }
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return res;

  }

}

