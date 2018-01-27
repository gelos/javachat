package my.jmockit.test;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import mockit.*;

class MyJmockitTest {

  public final class FakeClass1 extends MockUp<Class1> {
    @Mock
    public boolean method1Class1() {
      System.out.println("We are in faked method 1");
      return false;
    }
  }

  @DisplayName("Mocking, faking Class1 methods")
  @Test
  void class1Test(@Injectable Class1 mockClass1) {

    Class1 class1 = new Class1();

    // Test original method
    assertTrue(class1.method1Class1());
    assertEquals(-100, class1.method2Class1());

    // Mocking method
    new Expectations() {
      {
        mockClass1.method1Class1();
        result = false;
        mockClass1.method2Class1();
        result = -100;
      }
    };

    // Test mocked method
    assertFalse(mockClass1.method1Class1(), "Mock is not working");
    assertEquals(-100, mockClass1.method2Class1(), "Mock is not working");

    // Verify mocked method invocation
    new Verifications() {
      {
        mockClass1.method1Class1();
        times = 1;
      }
    };

    // Create new fake for Class1
    new FakeClass1();
    Class1 class11 = new Class1();

    // Test faked method
    assertFalse(class11.method1Class1());
    // Test original method
    assertEquals(-100, class11.method2Class1());

  }

  @DisplayName("Partial mocking method of concrete Class1 instance")
  @Test
  void class1PartialMockingTest() {
    Class1 class1 = new Class1();

    // Partial mocking concrete instance (class1)
    new Expectations(class1) {
      {
        class1.method1Class1();
        result = false;
      }
    };

    // Check mocked method
    assertFalse(class1.method1Class1());
    // Check original method
    assertEquals(-100, class1.method2Class1());

    // Check mocked method on new instance
    assertTrue(new Class1().method1Class1());

  }
  
  @DisplayName("Partial mocking method Class1 class and its instances")
  @Test
  void class1PartialMockingTest2() {
    Class1 class1 = new Class1();

    // Partial mocking class/instance
    new Expectations(Class1.class) {
      {
        class1.method1Class1();
        result = false;
      }
    };

    // Check mocked method
    assertFalse(class1.method1Class1());
    // Check original method
    assertEquals(-100, class1.method2Class1());

    // Check mocked method on new instance
    assertFalse(new Class1().method1Class1());

  }

  @DisplayName("Mocking Class2 constructor to throw IOException")
  @Test
  void class2Test(@Mocked Class2 mockClass2) {

    new Expectations() {
      {
        new Class2();
        result = new IOException();
      }
    };

    assertThrows(IOException.class, () -> {
      new Class1();
    });
  }

}
