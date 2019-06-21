package edu.stmc;

import java.util.Arrays;

public enum HypTestName {
  SPRT,
  GSPRT;

  public static String valuesToString() {
    return Arrays.toString(values());
  }
}
