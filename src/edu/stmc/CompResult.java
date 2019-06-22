package edu.stmc;

/** Result of comparing a probability with a constant (often called threshold) */
public class CompResult {

  /** Used when test has two possible outputs */
  public enum Binary {
    UNDECIDED,
    SMALLER,
    LARGER
  }

  /** Used when test has three possible outputs */
  public enum Ternary {
    UNDECIDED,
    TOO_CLOSE,
    SMALLER,
    LARGER,
  }

}

