package chat.client.mvp.swing;

public interface PresenterSwing {
   
  void setView(ChatClientSwingView view);
  
  boolean openConnection(String username);

  void closeConnection();

  void updateUserList();

  void sendPrvMsg();

  //void sendChatMsgToServer(String string);

  void printMsg();

  void showGreetingMsg();

  void sendChatMsg();

}
