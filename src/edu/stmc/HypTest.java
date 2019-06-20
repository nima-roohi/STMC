package edu.stmc;

public interface HypTest extends  Cloneable {

  /** Reset the state (can be used to restart the test) */
  void reset();

  /**
   * Update the test by adding an observation
   * @param passed Whether or not the sample satisfied the test or not.
   */
  void update(boolean passed);

  /**
   * Update the test by adding an observation
   * @param passed Number of samples that are passed
   * @param failed Number of samples that are failed
   * @requires {@code passed >= 0}
   * @requires {@code failed >= 0}
   */
  void update(int passed, int failed);

  /** Whether or not an answer has been already found */
  boolean shouldStopNow();

  /**
   * After {@link #shouldStopNow} returns true, this method is used to see if the result is known
   * @requires {@link #shouldStopNow} should return {@literal true}
   */
  boolean hasAnswer();

  /**
   * After {@link #shouldStopNow} and {@link #hasAnswer} both returned true, this is used to see if the null
   * hypothesis is rejected.
   * @requires {@link #shouldStopNow} should return {@literal true}
   * @requires {@link #hasAnswer} should return {@literal true}
   */
  boolean isNullRejected();

  /** Get the parameters of the simulation (including the computed one) as a string. */
  String getParametersString();

  HypTest cloneCopy();
}
