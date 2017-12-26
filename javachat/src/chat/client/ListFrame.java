package chat.client;

import java.awt.*;
import java.awt.event.*;
/**
 * The Class ListFrame.
 */
public class ListFrame extends Frame {

  /** The lp. */
  ListPanel lp;

  /**
   * Instantiates a new list frame.
   */
  public ListFrame() {

    setSize(200, 500);
    setTitle("List Client");
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        System.exit(0);
      }
    });

    lp = new ListPanel();
    add(lp, BorderLayout.CENTER);

    setVisible(true);
  }



  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {

    ListFrame lf = new ListFrame();

  }
}


class ListPanel extends Panel implements ActionListener {
  List lst;
  TextField addtf, removetf;

  public ListPanel() {
    setLayout(new BorderLayout());
    lst = new List(4, false);
    lst.add("Mercury");
    lst.add("Venus");
    lst.add("Earth");

    add(lst, BorderLayout.CENTER);

    addtf = new TextField();
    addtf.addActionListener(this);
    add(addtf, BorderLayout.SOUTH);

    removetf = new TextField();
    removetf.addActionListener(this);
    add(removetf, BorderLayout.NORTH);


  }

  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == addtf) {
      String temp = addtf.getText();
      lst.add(temp);
      addtf.setText("");
    } else if (ae.getSource() == removetf) {
      String temp = removetf.getText();
      lst.remove(temp);
      removetf.setText("");
    }
  }
}
