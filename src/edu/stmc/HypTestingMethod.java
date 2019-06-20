package edu.stmc;

public enum HypTestingMethod {
  SPRT,
  GSPRT;

  public static String valuesToString() {
    final StringBuilder      buff = new StringBuilder();
    final HypTestingMethod[] vals = values();
    if (vals.length > 0) {
      int i = 0;
      buff.append(vals[0]);
      while (++i < vals.length)
        buff.append(", ").append(vals[i]);
    }
    return buff.toString();
  }
}
