package edu.stmc

/** Ternary Sequential Probability Ratio Test
  *
  * ''Ternary'' means if the algorithm terminates, there are three possibilities for its answer:
  * ''reject'', ''failed_to_reject'', and ''undecided''.
  *
  * [[HypTestName]]
  *
  * @note Probabilistic guarantees in this class ignore numerical errors caused by floating point arithmetic.
  * @constructor
  * @param lb Lower-bound [[SPRT]] test
  * @param ub Upper-bound [[SPRT]] test */
class TSPRT private(private[this] val lb: SPRT,
                    private[this] val ub: SPRT)
  extends HypTest {

  /** Construct a hypothesis test in which the null hypothesis is `p = t-δ` and the alternative hypothesis
    * is `p = t+δ`, where `p` is the actual probability, `t` is the input threshold, and `δ` is the input
    * indifference region.
    *
    * @param threshold Threshold
    * @param alpha     Type I error probability (also known as 'false positive' probability) is the probability of
    *                  incorrectly rejecting the null hypothesis.
    * @param beta      Type II error probability (also known as 'false negative' probability) is the probability of
    *                  incorrectly not rejecting the null hypothesis.
    * @param gamma     Type III error probability (aka 'false undecided' probability) is the probability of
    *                  incorrectly remain undecided when actual probability is delta away from the input threshold.
    * @param delta     Half of the size of indifference region
    * @note
    * <pre>
    * The following requirements must be met:
    * 0 < threshold < 1
    * 0 < alpha < 0.5
    * 0 < beta < 0.5
    * 0 < gamma < 0.5
    * 0 < delta < 0.5
    * delta < threshold
    * delta < 1 - threshold
    * </pre> */
  def this(threshold: Double, alpha: Double, beta: Double, gamma: Double, delta: Double) =
    this(new SPRT(threshold - delta / 2, alpha, gamma, delta / 2),
      new SPRT(threshold + delta / 2, gamma, beta, delta / 2))

  override def reset(): Unit = {
    lb.reset()
    ub.reset()
  }

  override def update(passed: Boolean): Unit = {
    lb.update(passed)
    ub.update(passed)
  }

  override def update(passed: Int, failed: Int): Unit = {
    lb.update(passed, failed)
    ub.update(passed, failed)
  }

  /** @note The following probabilistic gurantees are made:
    *   1. If the actual probability is `threshold-delta` then the probability of returning
    *       [[CompResult.Ternary.LARGER]] is at most `alpha`.
    *   1. If the actual probability is `threshold+delta` then the probability of returning
    *       [[CompResult.Ternary.SMALLER]] is at most `beta`.
    *   1. If the actual probability is within `threshold-delta` and `threshold+delta` then the probability of
    *       returning [[CompResult.Ternary.TOO_CLOSE]] is at most `beta`. */
  def status(lb: CompResult.Binary, ub: CompResult.Binary): CompResult.Ternary =
    if (lb eq CompResult.Binary.SMALLER) CompResult.Ternary.SMALLER
    else if (ub eq CompResult.Binary.LARGER) CompResult.Ternary.LARGER
    else if ((lb eq CompResult.Binary.UNDECIDED) || (ub eq CompResult.Binary.UNDECIDED)) CompResult.Ternary.UNDECIDED
    else CompResult.Ternary.TOO_CLOSE

  /** Same as [[status]], but input parameters are taken from the current instance */
  def status: CompResult.Ternary = status(lb.status, ub.status)

  override def completed: Boolean = status ne CompResult.Ternary.UNDECIDED

  /** @note [[completed]] should be `true`
    * @note If the actual probability is between `threshold - delta` and `threshold + delta` then the probability of
    *       returning `true` would be at most `alpha`. */
  override def decided: Boolean = status ne CompResult.Ternary.TOO_CLOSE

  /** @note [[completed]] should be `true`
    * @note If the actual probability is less than `threshold - delta` then the probability of returning `true` would
    *       be at most `alpha`. */
  override def rejected: Boolean = status eq CompResult.Ternary.LARGER

  /** @note [[completed]] should be `true`
    * @note If the actual probability is larger than `threshold + delta` then the probability of returning `true` would
    *       be at most `beta`. */
  override def failed_to_reject: Boolean = status eq CompResult.Ternary.LARGER

  override def parametersStr: String = s"lower-bound(${lb.parametersStr}), upper-bound(${ub.parametersStr})"
  override def copy() = new TSPRT(lb.copy(), ub.copy())
}

object TSPRT {
  def apply(threshold: Double, alpha: Double, beta: Double, gamma: Double, delta: Double) =
    new TSPRT(threshold, alpha, beta, gamma, delta)
}
