package chat.test;

import mockit.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ServerSocketFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import chat.server.ChatServer;
import chat.server.SocketFactory;
import chat.server.SocketFactoryImpl;

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
  
  
  @Disabled
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

  @DisplayName("Test server behavior on IOException during creating ServerSocket")
  @Test
  void serverStartStopTest() {
    ChatServer chatServer = new ChatServer();
    //ChatServer chatServer = new ChatServer(ChatServer.SERVER_PORT,)
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertTrue(chatServer.close());
    
  }
  
  //TODO http://jmockit.github.io/about.html
  
  @DisplayName("Test server behavior on IOException during creating ServerSocket")
  @Test
  void serverStartIOExceptionTest() {
    // ChatServer chatServer = new ChatServer();
    
    //SocketFactory mock = MockUp.newEmptyProxy(SocketFactory.class);
    new MockUp<SocketFactoryImpl>() {
      
    };
    
    //mock.doSomething();
    
    ChatServer chatServer = new ChatServer(ChatServer.SERVER_PORT, socketFactory )
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertTrue(chatServer.close());
    
  }
  
}
