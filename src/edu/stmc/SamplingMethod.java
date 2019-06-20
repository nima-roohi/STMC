package edu.stmc;

public enum SamplingMethod {
  INDEPENDENT,
  ANTISTHETIC,
  STRATIFIED;

  public static String valuesToString() {
    final StringBuilder      buff = new StringBuilder();
    final SamplingMethod[] vals = values();
    if (vals.length > 0) {
      int i = 0;
      buff.append(vals[0]);
      while (++i < vals.length)
        buff.append(", ").append(vals[i].toString().toLowerCase());
    }
    return buff.toString();
  }
}
