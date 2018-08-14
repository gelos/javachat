package chat.client.mvp.presenter;

import chat.base.Command;
import chat.client.mvp.view.View;

/**
 * The Interface Presenter. Used in MVP model.
 */
public interface Presenter {

  /**
   * Open connection method.
   *
   * @param username the username
   */
  void openConnection(String username);

  /**
   * Close connection method.
   */
  void closeConnection();

  /**
   * Close connection method with send EXIT {@link Command}.
   *
   * @param sendEXTCMD the send EXTCMD
   */
  void closeConnection(boolean sendEXTCMD);

  /**
   * Send command. Parse the commandString to {@link Command} and send it.
   *
   * @param commandString the command string
   */
  void sendCommand(String commandString);

  /**
   * Sets the view. Using to bind with {@link View} in MVP model. 
   *
   * @param view the new view
   */
  void setView(View view);

  /**
   * Gets the binded {@link View}.
   *
   * @return the view
   */
  View getView();

  /**
   * The method that perform all actions when you run {@link View}.
   */
  void onViewStart();

}
