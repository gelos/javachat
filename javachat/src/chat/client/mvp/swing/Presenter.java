package chat.client.mvp.swing;

public interface Presenter {
   
  void setView(ChatClientView view);
  
  boolean openConnection(String username);

  void closeConnection();

  void updateUserList();

  void sendPrvMsg();

  void sendMsg(String string);

  void printMsg();

}
