package edu.stmc;

import java.util.Arrays;

/** Supported sampling methods (not all of them might be supported in every scenario) */
public enum SmplMethodName {
  INDEPENDENT,
  ANTITHETIC,
  STRATIFIED;

  public static String valuesToString() {
    return Arrays.toString(values()).toLowerCase();
  }
}
