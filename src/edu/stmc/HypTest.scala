package edu.stmc

/** Hypothesis Testing */
trait HypTest extends Cloneable {

  /** Reset the state (can be used to restart the test) */
  def reset(): Unit

  /** Update the test by adding an observation
    *
    * @param passed Whether or not the sample satisfied the test. */
  def update(passed: Boolean)

  /** Update the test by adding multiple observations
    *
    * @param passed Number of samples that are passed
    * @param failed Number of samples that are failed
    * @note Requires `passed >= 0` and `failed >= 0`*/
  def update(passed: Int, failed: Int)

  /** Whether or not the test is completed. */
  def completed: Boolean

  /** After [[completed]] returned true, this method is used to see if a decision is made (it is possible that a
    * test terminates without choosing between reject or not-reject).
    *
    * @note [[completed]] should return `true`*/
  def decided: Boolean

  /** After [[completed]] and [[decided]] both returned true, this is used to see if the null hypothesis is rejected.
    *
    * @note [[completed]] and [[decided]] methods should both return `true`. */
  def nullHypRejected: Boolean

  /** Get the parameters of the simulation (including the computed one) as a string. */
  def parametersStr: String

  /** Create a copy of this instance, so change in one does not affect change in the other. */
  def cloneCopy(): HypTest
}
