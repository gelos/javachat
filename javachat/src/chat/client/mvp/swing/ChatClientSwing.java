package chat.client.mvp.swing;

import javax.swing.SwingUtilities;

// TODO test swing app with JUnit http://www.javacreed.com/testing-swing-application/
// TODO use MVC pattern http://www.javacreed.com/testing-swing-application/

/**
 * The Class ChatClientSwing. Main class to start chat client.
 */
public class ChatClientSwing {

  /**
   * The main method. Run GUI in Event Dispatch Thread. Initialize ViewSwing and PresenterSwing.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          
          // Initialize ViewSwing and PresenterSwing
          final ChatClientSwingPresenter presenter = new ChatClientSwingPresenter();
          final ChatClientSwingView mainWindow = new ChatClientSwingView();
          presenter.setView(mainWindow);
          mainWindow.setPresenterSwing(presenter);
          
          // Show main window
          mainWindow.setVisible(true);
          presenter.showGreetingMsg();

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }
}