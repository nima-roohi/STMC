package edu.stmc;

import java.util.Arrays;

/** Supported hypothesis tests (not all of them might be supported in every scenario) */
public enum HypTestName {
  SPRT,
  GSPRT,
  TSPRT;

  public static String valuesToString() {
    return Arrays.toString(values());
  }
}
