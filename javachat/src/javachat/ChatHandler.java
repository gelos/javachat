package javachat;

// ChatHandler
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatHandler extends Thread {
  Socket s;
  BufferedReader br;
  PrintWriter pw;
  String temp;
  ArrayList handlers;

  public ChatHandler(Socket s, ArrayList handlers) {
    this.s = s;
    this.handlers = handlers;
  }


  public void run() {
    try {
      handlers.add(this);
      br = new BufferedReader(new InputStreamReader(s.getInputStream()));
      pw = new PrintWriter(s.getOutputStream(), true);
      temp = "";
      while ((temp = br.readLine()) != null) {
        for (ChatHandler ch : handlers) {
          ch.pw.println(temp);
        }
        System.out.println(temp);
      }
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } finally {
      handlers.remove(this);
    }
  }
}

