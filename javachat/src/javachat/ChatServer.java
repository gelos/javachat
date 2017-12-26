package javachat;

// ChatServer
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
  Socket s;
  ArrayList handlers;

  public ChatServer() {
    try {
      ServerSocket ss = new ServerSocket(3000);
      handlers = new ArrayList();
      for (;;) {
        s = ss.accept();
        new ChatHandler(s, handlers).start();
      }
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    }
  }

  public static void main(String[] args) {
    ChatServer tes = new ChatServer();
  }
}

