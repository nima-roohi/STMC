package edu.stmc;

import prism.PrismCL;

public class Main {

  public static void main(String[] args) {
    // PrismCL.main(new String[]{"-help"});
    PrismCL.main(new String[]{
    "/opt/prism-4.5/prism-examples/dice/dice.pm",
    "-pf", "P>0.1 [ F s=7 & d=1 ]",
    "-sim", "-stmc",
    "-alpha", "0.1",
    "-beta", "0.1",
    "-gamma", "0.1",
    "-delta", "0.1",
    "-miniter", "10",
    });
  }

}
