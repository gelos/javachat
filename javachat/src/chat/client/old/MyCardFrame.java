package chat.client.old;

// MyCardFrame
import java.awt.*;
import java.awt.event.*;

public class MyCardFrame extends Frame {
  LoginPanel lp;
  MyChatPanel cp;

  public MyCardFrame() {
    setLayout(new CardLayout());
    setTitle("Chat");
    setSize(500, 500);
    lp = new LoginPanel(this);
    cp = new MyChatPanel();
    add(lp, "login");
    add(cp, "chat");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        System.exit(0);
      }
    });

    setVisible(true);
  }

  public static void main(String[] args) {

    MyCardFrame mcf = new MyCardFrame();
  }
}


class LoginPanel extends Panel implements ActionListener {
  TextField tf;
  MyCardFrame mcf;

  public LoginPanel(MyCardFrame mcf) {
    this.mcf = mcf;
    tf = new TextField(20);
    tf.addActionListener(this);
    add(tf);
  }

  public void actionPerformed(ActionEvent ae) {
    mcf.cp.setUserName(tf.getText()); // call setUserName of chatpanel which is a member of
                                      // mycardframe
    CardLayout cl = (CardLayout) (mcf.getLayout());
    cl.next(mcf);
    tf.setText("");

  }


}


class MyChatPanel extends Panel {
  Label label1, label2;
  String userName;

  public MyChatPanel() {
    label1 = new Label("Chat Panel: ");
    label2 = new Label("Name will go here");
    add(label1);
    add(label2);
  }

  public void setUserName(String userName) {
    this.userName = userName;
    label2.setText(getUserName());
  }

  public String getUserName() {
    return userName;
  }
}

