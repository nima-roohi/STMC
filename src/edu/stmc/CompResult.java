package edu.stmc;

/** Result of comparing a probability with a constant (often called threshold) */
public class CompResult {

  public enum Binary {
    UNDECIDED,
    SMALLER,
    LARGER
  }

  public enum Ternary {
    UNDECIDED,
    TOO_CLOSE,
    SMALLER,
    LARGER,
  }

}

