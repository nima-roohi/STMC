package edu.stmc

import scala.math.log

// @formatter:off
/** Generalized Sequential Probability Ratio Test
  *
  * @note Probabilistic guarantees in this class ignore numerical errors caused by floating point arithmetic.
  * @constructor Construct a hypothesis test in which the null hypothesis is `p < t` and the alternative hypothesis is
  *              `p > t`, where `p` is the actual probability and `t` is the input threshold.
  * @param threshold  Input Threshold
  * @param alpha      Type I  Error Probability (also known as 'false positive' probability).
  * @param beta       Type II Error Probability (also known as 'false negative' probability).
  * @param minSamples Minimum number of samples before making any decision
  * <pre>
  * The following requirements must be met by the main constructor:
  *   0 < threshold < 1
  *   0 < alpha < 0.5
  *   0 < beta < 0.5
  *   1 < minSamples
  * </pre> */
// @formatter:on
class GSPRT(private[this] val threshold: Double,
            private[this] val alpha: Double,
            private[this] val beta: Double,
            private[this] val minSamples: Int)
  extends HypTest {

  require(minSamples > 1, s"Invalid minimum number of samples $minSamples")
  require(0 < threshold && threshold < 1, s"Invalid threshold $threshold")
  require(0 < alpha && alpha < 0.5, s"Invalid type I error $alpha")
  require(0 < beta && beta < 0.5, s"Invalid type II error $beta")

  /** Lower and upper bounds after which the null hypothesis won't/will be rejected */
  private[this] val (logL, logU) = (log(beta / (1 - alpha)), log((1 - beta) / alpha))
  assert(logL < 0, s"Lower-bound log is not negative $logL")
  assert(logU > 0, s"Upper-bound log is not positive $logU")

  /** Step size for negative and positive samples */
  private[this] val (logP0, logP1) = (log(threshold), log(1 - threshold))
  assert(java.lang.Double.isFinite(logP0), s"logP0 ($logP0) is not a finite number")
  assert(java.lang.Double.isFinite(logP1), s"logP1 ($logP1) is not a finite number")

  /** Total number of samples */
  private[this] var N: Int = 0

  /** Number of positive samples */
  private[this] var n: Int = 0

  override def reset(): Unit = {
    N = 0
    n = 0
  }

  /** @note At the time of entering this function, total number of samples must be at most 2^^31^^-2. */
  override def update(passed: Boolean): Unit = {
    N += 1
    if (passed)
      n += 1
  }

  /** @note Requires `positive >= 0` and `negative >= 0`.
    * @note At the time of entering this function, total number of samples plus `positive` plus `negative` must be
    *       at most 2^^31^^-1. */
  override def update(positive: Int, negative: Int): Unit = {
    N += positive + negative
    n += positive
  }

  /** @note Requires `n <= N`
    * @note Asymptotic guarantees: As total number of samples goes to infinity,
    *       1. if the actual probability is strictly smaller than the input threshold then the probability of
    *       returning [[CompResult.Binary.LARGER]] would be at most `alpha`.
    *       1. if the actual probability is strictly larger than the input threshold then the probability of
    *       returning [[CompResult.Binary.SMALLER]] would be at most `beta`. */
  def status(n: Int, N: Int): CompResult.Binary = {
    if (N < STMCConfig.minIters) return CompResult.Binary.UNDECIDED
    val mu = n / N.toDouble
    if (1 <= mu || mu <= 0) return CompResult.Binary.UNDECIDED
    val logMu0 = log(mu)
    val logMu1 = log(1 - mu)
    assert(java.lang.Double.isFinite(logMu0), s"logMu0 ($logMu0) is not a finite number")
    assert(java.lang.Double.isFinite(logMu1), s"logMu1 ($logMu1) is not a finite number")
    val exp = n * logP0 + (N - n) * logP1
    val logT = if (mu >= threshold) n * logMu0 + (N - n) * logMu1 - exp
               else exp - n * logMu0 + (N - n) * logMu1
    if (logT >= logU) CompResult.Binary.LARGER
    else if (logT <= logL) CompResult.Binary.SMALLER
    else CompResult.Binary.UNDECIDED
  }

  /** Same as [[status(n*]], but input parameters are taken from the current instance */
  @inline
  final def status: CompResult.Binary = status(n, N)

  override def completed: Boolean = status ne CompResult.Binary.UNDECIDED

  /** @note [[completed]] does not need to be `true` (as required by [[HypTest]]). */
  override def decided: Boolean = status ne CompResult.Binary.UNDECIDED

  /** @note [[completed]] should be `true`
    * @note Asymptotic guarantee: as total number of samples goes to infinity, if the actual probability is strictly
    *       smaller than the input threshold then the probability of returning `true` would be at most `alpha`.
    * @note In the current implementation, the maximum number of samples is 2^^31^^-1. */
  override def rejected: Boolean = status eq CompResult.Binary.LARGER

  /** @note [[completed]] should be `true`
    * @note Asymptotic guarantee: as total number of samples goes to infinity, if the actual probability is strictly
    *       larger than the input threshold then the probability of returning `true` would be at most `beta`.
    * @note In the current implementation, the maximum number of samples is 2^^31^^-1. */
  override def failed_to_reject: Boolean = status eq CompResult.Binary.SMALLER

  override def parametersStr: String =
    s"threshold: $threshold, alpha: $alpha, beta: $beta, logL: $logL, logU: $logU, logP0: $logP0, logP1: $logP1, minSamples: $minSamples"

  override def toString: String = s"$parametersStr, N: $N, n: $n"

  override def copy(): GSPRT = clone().asInstanceOf[GSPRT]
}

object GSPRT {
  def apply(threshold: Double, alpha: Double, beta: Double, minSamples: Int) =
    new GSPRT(threshold, alpha, beta, minSamples)
}