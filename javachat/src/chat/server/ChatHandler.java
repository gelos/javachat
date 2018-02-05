/*
 * 
 */
package chat.server;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import chat.base.WorkerThreadClass;

// TODO: Auto-generated Javadoc
/**
 * Implements server side part of chat application. Handle input/output streams of client socket
 * connection. Maintain handler storage.
 * 
 * @see ChatServer
 */
public class ChatHandler extends WorkerThreadClass {

  /**
   * The Constant _ENTER_CMD. Command to start chat session, pattern /enter username. Initiated by
   * client, processed by server.
   */
  public static final String CMD_ENTER = "/enter";

  /**
   * The Constant _EXIT_CMD. Command to close chat session, pattern /exit. Initiated by client,
   * processed by server.
   */
  public static final String CMD_EXIT = "/exit";

  /**
   * The Constant _USRLST_CMD. Command to update user list in client GUI, pattern /usrlst ulst where
   * ulst string of usernames with space character delimeter. Initiated by server, processed by
   * client.
   */
  public static final String CMD_USRLST = "/usrlst";

  /** The Constant _PRVMSG_CMD. */
  static final String _CMD_PRVMSG = "/prvmsg";

  /** The Constant _MSG_CMD. */
  static final String _CMD_MSG = "/msg";

  /** The Constant _HELP_CMD. */
  static final String _CMD_HELP = "/help";

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
      temp = checkForEnterCmd(br);     

      // if username not empty
      if (!temp.isEmpty()) {

        System.out.println("user: " + temp);
        
        // create new user
        chatUser = new ChatUser(temp);

        // send to client usrlst command
        // TODO use serialization
        // https://stackoverflow.com/questions/26245306/send-objects-and-objects-arrays-through-socket

        // Send to all users usrlst command
        for (ChatHandler ch : handlers) {
          ch.pw.println(CMD_USRLST + " " + getUserNamesInString());
        }

        temp = "";

        // Read all strings from current client socket input
        while ((temp = br.readLine()) != null && isRuning()) {

          // Write pre-read string to all clients output using handler storage
          for (ChatHandler ch : handlers) {
            
            String currentTime  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ch.pw.println(currentTime + " " + chatUser.getUsername() + ": " +  temp);
            
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
   * Return the all chat user names in one string. Used in {@link #CMD_USRLST usrlst} command.
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
   * Read first string from chat client input stream and match it with {@link #CMD_ENTER enter}
   * command.
   *
   * @param br the BufferedReader of input chat client stream
   * @return the user name or empty string if an error occurred
   */
  private String checkForEnterCmd(BufferedReader br) {

    String res = "";
    try {

      while (isRuning()) {

        // read first line
        temp = br.readLine();
        //System.out.println(temp);
        //System.out.println(temp.substring(0, CMD_ENTER.length()));
        //System.out.println(temp.substring(0, CMD_ENTER.length()).length());
     //}

      if (temp != null) {

        // trim spaces
        temp = temp.trim();

        // check if string start from enter command with space and at least one char username
        if (temp.length() >= CMD_ENTER.length() + 2
            && temp.substring(0, CMD_ENTER.length()+1).equalsIgnoreCase(CMD_ENTER + " ")) {

          // return username
          res = temp.substring(CMD_ENTER.length() + 1, temp.length());
          //System.out.println("break");
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

