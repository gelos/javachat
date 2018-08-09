package chat.base;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.MDC;
import java.net.InetSocketAddress;

public class CommandHandler_new extends WorkerThread {

  private Socket clientSocket = null;
  /** The input stream. */
  private ObjectInputStream inputStream = null;
  /** The output stream. */
  private ObjectOutputStream outputStream = null;

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

  @Override
  public void run() {
    try {

      openStreams();

      String ipStr = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress())
          .toString().replace("/", "");
      logger.info(this.getClass().getSimpleName() + "." + "run() - {}",
          Constants.MSG_ACCPT_CLIENT + ipStr); // $NON-NLS-1$
      System.out.println(Constants.MSG_ACCPT_CLIENT + ipStr);


      // Reading commands from the current client input socket while the handler is
      // running
      while (this.isRunning()) {

        Command command = (Command) inputStream.readObject();
        if (isRunning()) {

          chatSession.processCommand(command);
        }

      }


      // } catch (SocketException | EOFException e) {
    } catch (SocketException e) {
      // TODO: handle exception
      System.out.println("normal close");
    } catch (EOFException e) {
      // TODO: handle exception
      System.out.println("server close connection ???");
    } catch (IOException | ClassNotFoundException e) {
      // TODO Auto-generated catch block
      System.out.println("ClientCommandHandler.run() catch " + e.getMessage());
      // System.out
      // .println("ClientCommandHandler.run() clientSocket.isClosed() " + clientSocket.isClosed());
      e.printStackTrace();
    } finally {
      MDC.clear();
      // user = null;

    }

  }

  @Override
  public void stop() {

    super.stop();

    try {
      closeClientSocket();
    } catch (IOException e) {
      logger.error(this.getClass().getSimpleName() + "." + "stop()", e); //$NON-NLS-1$
    }
  }

  public final Boolean getIsOutputStreamOpened() {
    return isOutputStreamOpened.get();
  }

  public void send(Command command) {
    command.send(outputStream);
  }

  private synchronized void openStreams() throws IOException {

    // Output stream must be opened first, see http://www.jguru.com/faq/view.jsp?EID=333392

    outputStream = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
    isOutputStreamOpened.set(true);
    inputStream = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
  }


  private synchronized void closeClientSocket() throws IOException {

    if (clientSocket != null) {
      clientSocket.close();
      clientSocket = null;
    }
  }

}
