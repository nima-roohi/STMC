package edu.stmc;


public class STMCConfig {
  private STMCConfig() { }

  public static boolean enabled     = false;
  public static boolean multithread = false;
  public static int     repeat      = 1;

  public static Double alpha = null;
  public static Double beta  = null;
  public static Double gamma = null;
  public static Double delta = null;

  public static int[] strataSizes = null;
  public static Integer minIters = null;

  public static SmplMethodName samplingMethod = null;
  public static HypTestName    hypTestMethod  = null;
}
