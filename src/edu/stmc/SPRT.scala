package edu.stmc


// @formatter:off
/** Sequential Probability Ratio Test
  *
  * @note Probabilistic guarantees in this class ignore numerical errors caused by floating point arithmetic.
  * @constructor Construct a hypothesis test in which the null hypothesis is `p = t-δ` and the alternative hypothesis
  *              is `p = t+δ`, where `p` is the actual probability, `t` is the input threshold, and `δ` is the half of
  *              the size of indifference region.
  * @param threshold Input Threshold
  * @param alpha     Type I  Error Probability (also known as 'false positive' probability).
  * @param beta      Type II Error Probability (also known as 'false negative' probability).
  * @param delta     Half of the size of indifference region.
  * <pre>
  * The following requirements must be met by the main constructor:
  *   - 0 < threshold < 1
  *   - 0 < alpha < 0.5
  *   - 0 < beta < 0.5
  *   - 0 < delta < 0.5
  *   - delta < threshold
  *   - delta < 1 - threshold
  * </pre> */
// @formatter:on
class SPRT(private[this] val threshold: Double,
           private[this] val alpha: Double,
           private[this] val beta: Double,
           private[this] val delta: Double)
  extends HypTest {

  require(0 < threshold && threshold < 1, s"Invalid threshold $threshold")
  require(0 < alpha && alpha < 0.5, s"Invalid type I error $alpha")
  require(0 < beta && beta < 0.5, s"Invalid type II error $beta")
  require(0 < delta && delta < 0.5, s"Invalid indifference parameter $delta")
  require(delta < threshold && threshold < 1 - delta, s"Invalid threshold ($threshold) and/or delta ($delta)")
  private[this] val lb = threshold - delta
  private[this] val ub = threshold + delta

  /** Lower and upper bounds after which the null hypothesis won't/will be rejected */
  private[this] val (logL, logU) = (Math.log(beta / (1 - alpha)), Math.log((1 - beta) / alpha))
  assert(logL < 0, s"Lower-bound log is not negative " + logL)
  assert(logU > 0, s"Upper-bound log is not positive " + logU)

  /** Step sizes for negative/positive samples */
  private[this] val (q0, q1) = (Math.log((1 - ub) / (1 - lb)), Math.log(ub / lb))
  assert(java.lang.Double.isFinite(q0), s"q0 ($q0) is not a finite number")
  assert(java.lang.Double.isFinite(q1), s"q1 ($q1) is not a finite number")
  assert(logL - q0 > logL, s"logL ($logL) is too much smaller than q0 ($q0)")
  assert(logU - q1 < logU, s"logU ($logU) is too much bigger than q1 ($q1)")

  /** Test statistic */
  private[this] var logT = 0.0

  override def reset(): Unit = logT = 0

  override def update(passed: Boolean): Unit = if (passed) logT += q1 else logT += q0
  override def update(passed: Int, failed: Int): Unit = logT += passed * q1 + failed * q0

  /** @note The following probabilistic guarantees are made:
    *   1. If the actual probability is at most `threshold-delta` then the probability of returning
    *       [[CompResult.Binary.LARGER]] is at most `alpha`.
    *   1. If the actual probability is at least `threshold+delta` then the probability of returning
    *       [[CompResult.Binary.SMALLER]] is at most `beta`. */
  def status(logT: Double): CompResult.Binary =
    if (logT <= logL) CompResult.Binary.SMALLER
    else if (logT >= logU) CompResult.Binary.LARGER
    else CompResult.Binary.UNDECIDED

  /** Same as [[status(logT*]], but input parameter is taken from the current instance */
  @inline
  final def status: CompResult.Binary = status(logT)

  override def completed: Boolean = status ne CompResult.Binary.UNDECIDED

  /** @note [[completed]] does not need to be `true` (as required by [[HypTest]]). */
  override def decided: Boolean = status ne CompResult.Binary.UNDECIDED

  /** @note Requires [[completed]] to be `true`.
    * @note If the actual probability is at most `threshold-delta` then the probability of returning `true` is at most
    *       `alpha`. */
  override def rejected: Boolean = status eq CompResult.Binary.LARGER

  /** @note Requires [[completed]] to be `true`.
    * @note If the actual probability is at least `threshold+delta` then the probability of returning `true` is at most
    *       `beta`. */
  override def failed_to_reject: Boolean = status eq CompResult.Binary.LARGER

  override def parametersStr: String =
    s"threshold: $threshold, alpha: $alpha, beta: $beta, delta: $delta, logL: $logL, logU: $logU, q0: $q0, q1: $q1"

  override def toString: String = s"$parametersStr, logT: $logT"

  override def copy(): SPRT = clone.asInstanceOf[SPRT]
}

object SPRT {
  def apply(threshold: Double, alpha: Double, beta: Double, delta: Double) =
    new SPRT(threshold, alpha, beta, delta)
}
