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
  //void testClass1(@Mocked final Class1 class1, @Mocked Class2 class2) {
  void testClass1(@Injectable Class2 class2Mock) {
    Class1 class1Mock = new Class1();
    //Class2 class2Mock = null;
    
    new Expectations(class1Mock) {{
      //class1.method1Class1(); result = false;
      //class2.method1Class2(); result = false;
      //new Class2(); result = new IOException();
      //new Class1();
      class1Mock.method1Class1(); result = false;
    }};
    
    new Expectations() {{
      class2Mock.method1Class2(); result = false;
    }};
  
    //class1Mock = new Class1();
    
    assertFalse(class1Mock.method1Class1());
    assertFalse(class1Mock.class2.method1Class2());
    //class1 = new Class1();
    //boolean res = class1.class2.method1Class2(); 
    //assertTrue(res);
    
  }

}
