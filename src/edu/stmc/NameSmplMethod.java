package edu.stmc;

import java.util.Arrays;

/** Supported sampling methods (not all of them might be supported in every scenario) */
public enum NameSmplMethod {
  INDEPENDENT,
  ANTITHETIC,
  STRATIFIED;

  /** String representation of all possible values of this type (all lower-cased) */
  public static String valuesToString() {
    return Arrays.toString(values()).toLowerCase();
  }
}
