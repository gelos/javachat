package chat.test;

import java.util.concurrent.CountDownLatch;

import chat.client.mvp.swing.ClientViewSwing;

public class ClientViewSwingTest extends ClientViewSwing {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private CountDownLatch latch;
	
	public final void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}
	
	@Override
	public void onReceiveMessage(String message) {
		latch.countDown();
	}
	
	@Override
	public void onSendMessage() {
		System.out.println("ClientViewSwingTest.onSendMessage() " + this.hashCode() + " " + this.getClass().getSimpleName());
	}
	
	@Override
	public void onConnectionClosed(String message) {
		latch.countDown();
	}

}
