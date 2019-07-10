package edu.stmc;

import java.util.Arrays;

/** Supported hypothesis tests (not all of them might be supported in every scenario) */
public enum NameHypTest {
  SPRT,
  GSPRT,
  TSPRT;

  /** String representation of all possible values of this type */
  public static String valuesToString() {
    return Arrays.toString(values());
  }
}
