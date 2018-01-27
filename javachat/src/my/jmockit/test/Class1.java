package my.jmockit.test;

public class Class1 {

  public Class2 class2;
  
  public Class1() {
    System.out.println("We are in Class 1 constructor.");
    this.class2 = new Class2();
  }

  public boolean method1Class1() {
    System.out.println("We are in original method 1");
    return true;
  };
  
  public int method2Class1() {
    System.out.println("We are in original method 2");
    return -100;
  }
  
}
