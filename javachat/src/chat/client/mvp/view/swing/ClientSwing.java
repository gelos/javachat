package chat.client.mvp.view.swing;

import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import chat.client.mvp.presenter.ClientPresenter;
import chat.client.mvp.presenter.Presenter;

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
          final Presenter presenter = new ClientPresenter();
          final ClientViewSwing mainWindow = new ClientViewSwing();
          presenter.setView(mainWindow);
          mainWindow.setPresenter(presenter);

          // Show main window
          mainWindow.setVisible(true);
          presenter.onViewStart();

        } catch (Exception e) {
          logger.error(this.getClass().getSimpleName() + "." + "run()", e); //$NON-NLS-1$
        }
      }
    });
  }
}
