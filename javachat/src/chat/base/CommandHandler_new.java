package chat.base;

import static chat.base.CommandName.CMDENTER;
import static chat.base.Constants.MSG_ACCPT_CLIENT;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.MDC;

public class CommandHandler_new extends WorkerThread {

  protected Socket clientSocket = null;
  /** The input stream. */
  protected ObjectInputStream inputStream = null;
  /** The output stream. */
  // TODO create send method and change variable to protected
  public ObjectOutputStream outputStream = null;
  
  private AtomicBoolean isOutputStreamOpened;
  
  private ChatSession chatSession;

  public CommandHandler_new(Socket clientSocket, ChatSession chatSession) {
    super();
    synchronized (clientSocket) {
      this.clientSocket = clientSocket;
    }
    this.chatSession = chatSession;
    isOutputStreamOpened = new AtomicBoolean(false);
  }

  protected synchronized void openOutputStream() throws IOException {
    outputStream = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
    isOutputStreamOpened.set(true);
    //outputStream.flush();
    System.out.println("open output stream");
  }

  public final Boolean getIsOutputStreamOpened() {
    return isOutputStreamOpened.get();
  }

  protected synchronized void openInputStream() throws IOException {
    inputStream = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
    System.out.println("open input stream");
  }

  @Override
  public void stop() {

    super.stop();

    try {
      closeClientSocket();
    } catch (IOException e) {
      // TODO add logger
      System.out.println(e.getStackTrace());
      // logger.error(getClass().getSimpleName() + ".stop()", e); //$NON-NLS-1$
    }
  }

  protected synchronized void closeClientSocket() throws IOException {

    if (clientSocket != null) {
      clientSocket.close();
      clientSocket = null;
    }
  }

  @Override
  public void run() {
    try {
      openOutputStream();

      //new Command(CMDENTER, "", "").send(outputStream);
      openInputStream();

      /*String ip = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress())
          .toString().replace("/", "");
      CommandHandler.logger.info("run() - {}", MSG_ACCPT_CLIENT + ip); //$NON-NLS-1$
      System.out.println(MSG_ACCPT_CLIENT + ip);*/

      // Reading commands from the current client input socket while the handler is
      // running
      while (isRunning()) {

        Command command = (Command) inputStream.readObject();
        if (isRunning()) {
          //loggerDebugMDC.debug(command.toString());
          chatSession.processCommand(command);
        }

      }

      
      // } catch (SocketException | EOFException e) {
    } catch (SocketException e) {
      // TODO: handle exception
      System.out.println("normal close");
    }
    catch (EOFException e) {
      // TODO: handle exception
      System.out.println("server close connection ???");
    }
    catch (IOException | ClassNotFoundException e) {
      // TODO Auto-generated catch block
      System.out.println("ClientCommandHandler.run() catch " + e.getMessage());
      // System.out
      // .println("ClientCommandHandler.run() clientSocket.isClosed() " + clientSocket.isClosed());
      e.printStackTrace();
    } finally {
      MDC.clear();
      //user = null;

    }

  }

}
