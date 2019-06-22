package edu.stmc;

import prism.PrismCL;
import prism.PrismException;

public class Main {

  public static void main(String[] args) throws PrismException {
    args = new String[]{
    // "/opt/prism-4.5/prism-examples/dice/dice.pm",
    // "-pf", "P>0.1 [ F s=7 & d=1 ]",
    "/Users/nima/Dropbox/Research/Yu/codes/prism-4.4/examples/egl/egl.pm",
    "-const", "L=8",
    "-const", "N=12",
    "-pf", "P<0.51[F<100!kA&kB]",
    "-mt",
    "-repeat", "1",
    "-sim",
    "-stmc",
    "-htm", "SPRT",
    "-alpha", "0.01",
    "-beta", "0.01",
    "-delta", "0.05",
    // "-gamma", "0.1",
    // "-miniter", "10",
    };

    boolean multithread = false;
    int     repeat      = 1;
    for (int i = 0; i < args.length; i++)
      if (args[i].length() > 0 && args[i].charAt(0) == '-') {
        // Remove "-"
        String sw = args[i].substring(1);
        if (sw.length() == 0)
          throw new PrismException("Invalid empty switch");
        // Remove optional second "-" (i.e. we allow switches of the form --sw too)
        if (sw.charAt(0) == '-')
          sw = sw.substring(1);
        if ("multithread".equals(sw) || "mt".equals(sw)) multithread = true;
        else if ("repeat".equals(sw)) {
          i++;
          if (i == args.length)
            throw new PrismException("Missing value for -repeat switch");
          try {
            repeat = Integer.parseInt(args[i]);
          } catch (NumberFormatException e) {
            throw new PrismException("Could not parse value of -repeat switch ('" + args[i] + "') as an integer");
          }
          if (repeat < 1)
            throw new PrismException("Value for -repeat switch ('" + args[i] + "') is too small (must be at least one)");
        }
      }
    System.out.println(multithread + "   " + repeat);

    PrismCL.main(args);
  }

}
