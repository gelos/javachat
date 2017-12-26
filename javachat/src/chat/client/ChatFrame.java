package chat.client;

// ChatFrame.java
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatFrame extends Frame {
  public ChatFrame() {
    setSize(500, 500);
    setTitle("Echo Client");
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        System.exit(0);
      }
    });
    ChatPanel cp = new ChatPanel();
    add(cp, BorderLayout.CENTER);
    setVisible(true);
  }

  public static void main(String[] args) {
    ChatFrame cf = new ChatFrame();
  }
}


class ChatPanel extends Panel implements ActionListener, Runnable {
  TextField tf;
  TextArea ta;
  Socket s;
  PrintWriter send;
  BufferedReader rec;
  Thread t;
  Button connect, disconnect;

  public ChatPanel() {
    setLayout(new BorderLayout());
    tf = new TextField();
    tf.addActionListener(this);
    add(tf, BorderLayout.NORTH);
    ta = new TextArea();
    add(ta, BorderLayout.CENTER);
    // new stuff below
    connect = new Button("Connect");
    connect.addActionListener(this);
    disconnect = new Button("Disconnect");
    disconnect.addActionListener(this);
    disconnect.setEnabled(false);
    Panel p = new Panel();
    p.add(connect);
    p.add(disconnect);
    add(p, BorderLayout.SOUTH);

  }

  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == tf) {
      String temp = tf.getText();
      send.println(temp);
      tf.setText("");
    } else if (ae.getSource() == connect) {
      try {
        s = new Socket("localhost", 3000);
        send = new PrintWriter(s.getOutputStream(), true);
        rec = new BufferedReader(new InputStreamReader(s.getInputStream()));

      } catch (UnknownHostException uhe) {
        System.out.println(uhe.getMessage());
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
      t = new Thread(this, "whatever");
      t.start();
      connect.setEnabled(false);
      disconnect.setEnabled(true);

    } else if (ae.getSource() == disconnect) {
      // how to kill connection?
      send.println("BYE");
      send = null;
      rec = null;
      s = null;
      t = null;

      disconnect.setEnabled(false);
      connect.setEnabled(true);
    }
  }

  public void run() {
    String temp = "";
    try {
      while (((temp = rec.readLine()) != null) && (t != null)) {
        ta.append(temp + "\n");
      }
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    }

  }

}


