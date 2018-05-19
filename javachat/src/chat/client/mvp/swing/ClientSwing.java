package chat.client.mvp.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import chat.base.ClientPresenter;

// TODO test swing app with JUnit http://www.javacreed.com/testing-swing-application/
// TODO use MVC pattern http://www.javacreed.com/testing-swing-application/

/**
 * The Class ClientSwing. Main class to start chat client.
 */
public class ClientSwing {
  /**
   * Logger for this class
   */
  private static final Logger logger = LoggerFactory.getLogger(ClientSwing.class);

  /**
   * The main method. Run GUI in Event Dispatch Thread. Initialize View and Presenter.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          
          // Initialize View and Presenter
          final ClientPresenter presenter = new ClientPresenter();
          final ClientViewSwing mainWindow = new ClientViewSwing();
          presenter.setView(mainWindow);
          mainWindow.setPresenter(presenter);
          
          // Show main window
          mainWindow.setVisible(true);
          presenter.onViewStart();

        } catch (Exception e) {
          logger.error("$Runnable.run()", e); //$NON-NLS-1$
        }
      }
    });
  }
}
