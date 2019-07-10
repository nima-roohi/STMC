package edu.stmc;

public class STMCConfig {
  private STMCConfig() { }

  public static boolean enabled = false;

  public static Double alpha = null;
  public static Double beta  = null;
  public static Double gamma = null;
  public static Double delta = null;

  public static int[]   strataSizes = null;
  public static Integer minIters    = null;

  public static NameSmplMethod samplingMethod = null;
  public static NameHypTest    hypTestMethod  = null;
}
