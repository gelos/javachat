package my.jmockit.test;

public class Class1 {

  public Class2 class2;
  
  public Class1() {
    System.out.println("We are in Class 1 constructor.");
    this.class2 = new Class2();
  }

  public boolean method1Class1() {
    return true;
  };
  
}
