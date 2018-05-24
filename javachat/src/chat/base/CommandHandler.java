package chat.base;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chat.server.ServerCommandHandler;

public class CommandHandler extends WorkerThread {

	/**
	 * Logger for this class
	 */
	protected static final Logger logger = LoggerFactory.getLogger(ServerCommandHandler.class);
	protected static final Logger loggerDebugMDC = LoggerFactory.getLogger("debug.MDC");

	/** The client socket. */
	protected Socket clientSocket = null;
	/** The input stream. */
	protected ObjectInputStream inputStream = null;
	/** The output stream. */
	protected ObjectOutputStream outputStream = null;
	/** The chat user. */
	protected User user = null;
	/** The is session opened flag. */
	protected AtomicBoolean isSessionOpened;

	public CommandHandler(Socket clientSocket) {
		super();
		this.clientSocket = clientSocket;
		this.isSessionOpened = new AtomicBoolean(false);
	}

	protected synchronized void openOutputStream() throws IOException {
		outputStream = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
	}

	protected synchronized void openInputStream() throws IOException {
		inputStream = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
	}

	protected synchronized void closeInputStream() throws IOException {
		if (clientSocket != null && inputStream != null) {
			inputStream.close();
			inputStream = null;
		}
	}

	protected synchronized void closeClientSocket() throws IOException {

		if (clientSocket != null) {
			clientSocket.close();
			clientSocket = null;
		}
	}

	public void processCommand(Command Command) {
		
	}

}