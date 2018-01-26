package chat.test;

import mockit.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Executable;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ServerSocketFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import chat.client.mvp.swing.ChatClientSwingPresenter;
import chat.server.ChatServer;
import chat.server.SocketFactory;
import chat.server.SocketFactoryImpl;

class ChatTest {

  // ChatServer server = null;
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
        //server = new ChatServer();
      }
    });
    th.start();

    // Thread.sleep(5000);

    // assertNotNull(th. .server);
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

    // server = null;

    Thread chatServerThread = new Thread() {
      @Override
      public void run() {
        number.set(number.get() + 1);
        // number.incrementAndGet();
        // assertNotNull(server.get(), "after start in thread");
        assertEquals(1, number.get());
        // System.out.println("Start thread");

        // server = new ChatServer();
        //server.set(new ChatServer());
        System.out.println("Start thread");
        assertNotNull(server.get(), "after start in thread");


      }

    };

    // ChatServer server = new ChatServer();

    // assertEquals(0, number);
    assertNull(server.get(), "before start");
    chatServerThread.start();
    assertNull(server.get(), "after start");
    // number += 1;
    assertEquals(0, number.get());

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertNotNull(server.get(), "after timeout");
    assertEquals(1, number.get(), "after timeout");
    // assertNull(server.get());
    assertTrue(server.get().close());
    // ChatClientSwingPresenter client = new ChatClientSwingPresenter();

    // fail("Not yet implemented");

    if (exception.get() != null) {
      throw exception.get();
    }

  }

    //@DisplayName("Test server behavior on IOException during creating ServerSocket")
  @Disabled
  @Test
  void serverStartStopTest() {
    //ChatServer chatServer = new ChatServer();
    // ChatServer chatServer = new ChatServer(ChatServer.SERVER_PORT,)
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    //assertTrue(chatServer.close());

  }

   //@Injectable SocketFactoryImpl socketFactoryImpl;
  @DisplayName("Tests IOException catching while ServerSocket creating using Jmockit")
  @Test
  @Disabled
  void serverIOExceptionTestJMockit() throws IOException {
        
    new Expectations() {
      {
        socketFactoryImpl.createSocketFor(anyInt);
        result = new IOException();
      }
    };
    
    assertThrows(IOException.class, () -> {
      new ChatServer(ChatServer.SERVER_PORT, socketFactoryImpl);
    });
    
    new Verifications() { {
      socketFactoryImpl.createSocketFor(anyInt);
    }};

  }
 

  @Mocked
  SocketFactoryImpl socketFactoryImpl; 
  
  @Injectable
  ServerSocket serverSocket;
  
  @Test
  void createServerSocketIOExceptionTest() throws IOException {       
    
    new Expectations() {
      {
        socketFactoryImpl.createSocketFor(anyInt);
        result = new IOException();
      }
    };
    
    assertThrows(IOException.class, () -> {
      new ChatServer(ChatServer.SERVER_PORT, socketFactoryImpl);
    });    
  }

  @Test
  void acceptServerSocketIOExceptionTest() throws Throwable {
    
    final AtomicReference<Throwable> exception = new AtomicReference<>();
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.compareAndSet(null, e);
      }
    });
    
    new Expectations() {{
      //socketFactoryImpl.createSocketFor(anyInt); result = serverSocket;
      serverSocket.accept(); result = new IOException();
    }};
    
   //ChatServer chatServer = new ChatServer(ChatServer.SERVER_PORT, socketFactory);
    
 //   ChatClientSwingPresenter chatPresenter1 = new ChatClientSwingPresenter();
 //   ChatClientSwingPresenter
    
    assertThrows(IOException.class, () -> {
      new ChatServer(ChatServer.SERVER_PORT, socketFactoryImpl);
    });
  
   if (exception.get() != null) {
     throw exception.get();
   }

   
  }
  
}
