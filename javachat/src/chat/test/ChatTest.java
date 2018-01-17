package chat.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import chat.server.ChatServer;

class ChatTest {

  //ChatServer server = null;
  AtomicReference<ChatServer> server = new AtomicReference<ChatServer>(null);
  AtomicInteger number = new AtomicInteger(0);

  @BeforeEach
  void setUp() throws Exception {}

  @AfterEach
  void tearDown() throws Exception {}

  @Disabled
  @Test
  void testCreateChatServer() {
    Thread th = new Thread(new Runnable() {
      public ChatServer server = null;
            
      public void run() {
        server = new ChatServer();
      }
    });
    th.start();
    
    //Thread.sleep(5000);
    
    //assertNotNull(th. .server);
  }
  
  
  //@Disabled
  @Test
  void testServerStartStop() throws Throwable {

    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });

    //server = null;

    Thread chatServerThread = new Thread() {
      @Override
      public void run() {
        number.set(number.get()+1);
        //number.incrementAndGet();
        //assertNotNull(server.get(), "after start in thread");
        assertEquals(1, number.get());
        //System.out.println("Start thread");
        
        //server = new ChatServer();
        server.set(new ChatServer());
        System.out.println("Start thread");
        assertNotNull(server.get(), "after start in thread");
        
        
      }

    };

    // ChatServer server = new ChatServer();

    //assertEquals(0, number);
    assertNull(server.get(), "before start");
    chatServerThread.start();
    assertNull(server.get(), "after start");
    //number += 1;
    assertEquals(0, number.get());
   
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    assertNotNull(server.get(), "after timeout");
    assertEquals(1, number.get(), "after timeout");
    //assertNull(server.get());
    assertTrue(server.get().close());
    // ChatClientSwingPresenter client = new ChatClientSwingPresenter();

    // fail("Not yet implemented");

    if (exception.get() != null) {
      throw exception.get();
    }

  }

}
