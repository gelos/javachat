package chat.client.mvp.swing;

public interface Presenter {
   
  void setView(ChatClientSwingView view);
  
  boolean openConnection(String username);

  void closeConnection();
  
  void stop();

  void updateUserList();

  void sendPrvMsg();

  //void sendChatMsgToServer(String string);

  void printMsg();

  void showGreetingMsg();

  void sendChatMsg();

}
