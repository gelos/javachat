package chat.client.mvp.swing;

/**
 * The Interface View.
 */
public interface View {

  /**
   * Sets the presenter.
   *
   * @param presenter the new presenter
   */
  void setPresenter(Presenter presenter);

  /**
   * Show user list.
   */
  void showUserList();

  void showMessageWindow(Object message, String title, int messageType);

  void showChatMessage(String message);

  void clearChatPane();

  String getEnterTextField();
  
  void clearEnterTextField();
}
