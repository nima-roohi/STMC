package edu.stmc;

import prism.PrismCL;

enum Res {
  POS, NEG, ZERO
}

class C1 {
  C1(double value) {
    step = value;
  }
  Res a() {
    d += step;
    return d == 0 ? Res.ZERO :
           d > 0 ? Res.POS :
           Res.NEG;
  }
  double d = 0.0;
  double step;
}

class C2 {
  int a() {
    d += 0.1;
    return d == 0 ? 0 :
           d > 0 ? 1 :
           -1;
  }
  double d = 0.0;
}

public class Main {

  public static void main(String[] args) {
    // PrismCL.main(new String[]{"-help","cconst"});
    C1 c1 = new C1((args.length + 1) / 10.0);
    C2 c2 = new C2();
    int a = 0;

    long t1 = System.currentTimeMillis();
    for (int i = 0; i < 2000000000; i++)
      if(c1.a() == Res.ZERO) a++;
    long t2 = System.currentTimeMillis();
    for (int i = 0; i < 2000000000; i++)
      if(c2.a() == 0) a++;
    long t3 = System.currentTimeMillis();

    System.out.println("time: " + (t2 - t1) / 1000.0 + "\t\tignore: " + ((C1) c1).d);
    System.out.println("time: " + (t3 - t2) / 1000.0 + "\t\tignore: " + c2.d);
  }

}
