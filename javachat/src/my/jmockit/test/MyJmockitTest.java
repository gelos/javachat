package my.jmockit.test;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mockit.*;

class MyJmockitTest {

  @BeforeEach
  void setUp() throws Exception {}

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void testClass1(@Injectable Class1 class1, @Injectable Class2 class2) {
    
    new Expectations() {{
      new Class2(); result = new IOException();
      new Class1();
    }};
  
    class1 = new Class1();
  }

}
