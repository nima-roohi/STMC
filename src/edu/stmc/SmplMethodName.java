package edu.stmc;

import java.util.Arrays;

public enum SmplMethodName {
  INDEPENDENT,
  ANTITHETIC,
  STRATIFIED;

  public static String valuesToString() {
    return Arrays.toString(values()).toLowerCase();
  }
}
