package edu.stmc

/** Hypothesis Testing */
trait HypTest extends Cloneable {

  /** Reset the test (can be used to restart the test). */
  def reset(): Unit

  /** Update the test by adding an observation.
    *
    * @param positive Whether or not the new sample is positive.
    * @note Implementations might add a constraint on the total number of samples. */
  def update(positive: Boolean)

  /** Update the test by adding multiple observations.
    *
    * @param positive Number of samples that are positive.
    * @param negative Number of samples that are negative.
    * @note Requires `positive >= 0` and `negative >= 0`.
    * @note Implementations might add a constraint on the total number of samples. */
  def update(positive: Int, negative: Int)

  /** Whether or not the test is completed. */
  def completed: Boolean

  /** Whether or not a decision is made (it is possible that a test terminates without choosing between the null and
    * alternative hypotheses).
    *
    * @note Requires [[completed]] to be `true`. */
  def decided: Boolean

  /** Whether the test rejects the null hypothesis in favor of the alternative hypothesis.
    *
    * @note Requires [[completed]] to be `true`. */
  def rejected: Boolean

  /** Whether the test is failed to reject the null hypothesis in favor of the alternative hypothesis.
    *
    * @note Requires [[completed]] to be `true`. */
  def failed_to_reject: Boolean

  /** Get parameters of the test (including the computed ones) as a string. */
  def parametersStr: String

  /** Create a copy of this instance, so changes in one does not affect changes in the other. */
  def copy(): HypTest
}
