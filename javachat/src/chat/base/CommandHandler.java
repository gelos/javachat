package chat.base;

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

/**
 * The Class CommandHandler. Class for send/receive {@link Command}.
 */
public class CommandHandler extends WorkerThread {

  /** The client socket. */
  private Socket clientSocket = null;
  /** The input stream. */
  private ObjectInputStream inputStream = null;
  /** The output stream. */
  private ObjectOutputStream outputStream = null;

  /** The is output stream opened. */
  private AtomicBoolean isOutputStreamOpened;

  /** The chat session. */
  private Session session;

  /**
   * Instantiates a new command handler.
   *
   * @param clientSocket the client socket
   * @param session the chat session
   */
  public CommandHandler(Socket clientSocket, Session session) {
    super();
    synchronized (clientSocket) {
      this.clientSocket = clientSocket;
    }
    this.session = session;
    isOutputStreamOpened = new AtomicBoolean(false);
  }

  /**
   * Gets the checks if is output stream opened.
   *
   * @return the checks if is output stream opened
   */
  public final Boolean getIsOutputStreamOpened() {
    return isOutputStreamOpened.get();
  }

  /**
   * Send {@link Command} to output stream.
   *
   * @param command the command
   */
  public void send(Command command) {

    try {

      outputStream.writeObject(command);
      outputStream.flush();

    } catch (IOException e) {

      logger.error(this.getClass().getSimpleName() + "." + "send(command)", e); //$NON-NLS-1$

    }

  }

  /**
   * Skeleton method for processing input command. Read {@link Command} from input stream and run
   * {@link Session#receive(Command)}.
   * 
   * @see java.lang.Runnable#run()
   */
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

          session.receive(command);
        }

      }
    } catch (SocketException e) {
      // TODO: handle exception
      System.out.println("normal close");
    } catch (EOFException e) {
      // TODO: handle exception
      System.out.println("server close connection ???");
    } catch (IOException | ClassNotFoundException e) {
      logger.error(this.getClass().getSimpleName() + "." + "run() {}", e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see chat.base.WorkerThread#stop()
   */
  @Override
  public void stop() {

    super.stop();

    try {

      closeClientSocket();

    } catch (IOException e) {

      logger.error(this.getClass().getSimpleName() + "." + "stop()", e); //$NON-NLS-1$

    }
  }

  /**
   * Open streams.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private synchronized void openStreams() throws IOException {

    // Output stream must be opened first, see http://www.jguru.com/faq/view.jsp?EID=333392

    outputStream = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
    isOutputStreamOpened.set(true);
    inputStream = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
  }


  /**
   * Close client socket.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private synchronized void closeClientSocket() throws IOException {

    if (clientSocket != null) {
      clientSocket.close();
      clientSocket = null;
    }
  }

}
