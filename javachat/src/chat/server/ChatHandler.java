package chat.server;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Implements server side part of chat application. Handle input/output streams of client socket
 * connection. Maintain handler storage.
 * 
 * @see ChatServer
 */
// TODO try to change on implement Runnable
public class ChatHandler extends Thread {

  /** The client socket. */
  Socket s;

  /** The Buffer to cache client input stream. */
  BufferedReader br;

  /** The pw. */
  PrintWriter pw;

  /** The temporary String storage. */
  String temp;

  /** The client session handlers storage. */
  ArrayList<ChatHandler> handlers;

  /**
   * Instantiates a new chat handler.
   *
   * @param s the client socket
   * @param handlers the handlers storage
   */
  public ChatHandler(Socket s, ArrayList handlers) {
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

      temp = "";

      // Read all strings from current client socket input
      while ((temp = br.readLine()) != null) {

        // Write pre-read string to all clients output using handler storage
        for (ChatHandler ch : handlers) {
          ch.pw.println(temp);
        }

        // Write string to server console
        System.out.println(temp);

      }
    } catch (IOException ioe) {

      System.out.println(ioe.getMessage());

    } finally {

      // Remove this handler from handlers storage
      handlers.remove(this);
    }
  }
}

