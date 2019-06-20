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
    return d == 0 ? 0 :
           d > 0 ? 1 :
           -1;
  }
  double d = 0.0;
}

public class Main {

  public static void main(String[] args) {
    // PrismCL.main(new String[]{"-help"});
    PrismCL.main(new String[]{
    "/opt/prism-4.5/prism-examples/dice/dice.pm",
    "-pf", "P>0.1 [ F s=7 & d=1 ]"});
  }

}
