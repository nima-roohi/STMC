package edu.stmc

import parser.ast.{Expression, ExpressionProb}
import prism.PrismException
import simulator.sampler.Sampler

/** Sequential Probability Ratio Test
  *
  * @note
  *   1. Probabilistic guarantees in this class ignore numerical errors caused by floating point arithmetic.
  *   1. Method [[init]] must be called before this test can be actually performed.
  * @constructor Create an uninitialized instance of this method. */
final class HypTestSPRT() extends HypTest {

  // Input parameters
  private[this] var threshold: Double = _
  private[this] var alpha: Double = _
  private[this] var beta: Double = _
  private[this] var delta: Double = _
  private[this] var LB: Boolean = _

  // Computed initially based on the input parameters
  private[this] var q0: Double = _
  private[this] var q1: Double = _
  private[this] var logL: Double = _
  private[this] var logU: Double = _

  // Test statistic
  private[this] var logT = 0.0

  /** Initialize or reset this to a hypothesis test in which the null hypothesis is `p = θ - δ` and the alternative hypothesis is `p = θ + δ`, where `p` is
    * the actual probability, `θ` is the input threshold, and `δ` is the half of the size of indifference region.
    *
    * @param threshold Input Threshold
    * @param alpha     Type I  (aka `false positive`) error probability (the probability of incorrectly     rejecting the null hypothesis).
    * @param beta      Type II (aka `false negative`) error probability (the probability of incorrectly not rejecting the null hypothesis).
    * @param delta     Half of the size of indifference region.
    * @param LB        Whether or not the null and alternative hypotheses should be swapped (`true` means they should).
    * @note The following requirements must be met by the main constructor (`θ` refers for threshold):
    *   - 0 < θ < 1
    *   - 0 < α < 0.5
    *   - 0 < β < 0.5
    *   - 0 < δ < 0.5
    *   - δ < θ
    *   - δ < 1 - θ */
  def init(threshold: Double, alpha: Double, beta: Double, delta: Double, LB: Boolean = true): HypTestSPRT = {
    require(0 < threshold && threshold < 1, s"Invalid threshold $threshold")
    require(0 < alpha && alpha < 0.5, s"Invalid type I error $alpha")
    require(0 < beta && beta < 0.5, s"Invalid type II error $beta")
    require(0 < delta && delta < 0.5, s"Invalid indifference parameter $delta")
    val lb = threshold - delta
    val ub = threshold + delta
    require(0 < lb, s"Invalid threshold ($threshold) and/or indifference parameter ($delta)")
    require(ub < 1, s"Invalid threshold ($threshold) and/or indifference parameter ($delta)")
    this.threshold = threshold
    this.delta = delta

    this.LB = LB
    if (LB) {
      this.alpha = beta
      this.beta = alpha
    } else {
      this.alpha = alpha
      this.beta = beta
    }

    // Lower and upper bounds after which the null hypothesis won't/will be rejected
    logL = Math.log(this.beta / (1 - this.alpha))
    logU = Math.log((1 - this.beta) / this.alpha)
    assert(logL < 0, s"Lower-bound log is not negative " + logL)
    assert(logU > 0, s"Upper-bound log is not positive " + logU)

    // Step sizes for negative/positive samples
    q0 = Math.log((1 - ub) / (1 - lb))
    q1 = Math.log(ub / lb)
    assert(java.lang.Double.isFinite(q0), s"q0 ($q0) is not a finite number")
    assert(java.lang.Double.isFinite(q1), s"q1 ($q1) is not a finite number")
    assert(logL - q0 > logL, s"logL ($logL) is too much smaller than q0 ($q0)")
    assert(logU - q1 < logU, s"logU ($logU) is too much bigger than q1 ($q1)")

    this
  }

  private def reset(threshold: Double,
                    alpha: Double, beta: Double, delta: Double,
                    LB: Boolean,
                    q0: Double, q1: Double,
                    logL: Double, logU: Double, logT: Double): Unit = {
    this.threshold = threshold
    this.alpha = alpha
    this.beta = beta
    this.delta = delta
    this.LB = LB
    this.q0 = q0
    this.q1 = q1
    this.logL = logL
    this.logU = logU
    this.logT = logT
  }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  // SimulationMethod Methods

  override def reset(): Unit = logT = 0
  override def getName: String = "SPRT"
  override def getFullName: String = "Sequential Probability Ratio Test"
  override def getParametersString: String =
    s"threshold: $threshold, alpha: $alpha, beta: $beta, delta: $delta, LB: $LB, q0: $q0, q1: $q1, logL: $logL, logU: $logU"

  def getResultExplanation(sampler: Sampler): String =
    s"$getParametersString, logT: $logT"

  override def clone: HypTestSPRT = {
    val res = new HypTestSPRT()
    res.reset(threshold, alpha, beta, delta, LB, q0, q1, logL, logU, logT)
    res
  }

  override def setExpression(expr: Expression): Unit =
    if (!expr.isInstanceOf[ExpressionProb])
      throw new PrismException(s"Can only handle expressions of type ExpressionProp. However, type of '$expr' is ${expr.getClass.getName}")
    else {
      val expr2 = expr.asInstanceOf[ExpressionProb]
      val threshold = expr2.getBound.evaluateDouble
      val op = expr2.getRelOp
      init(threshold, STMCConfig.alpha, STMCConfig.beta, STMCConfig.delta, op.isLowerBound)
    }

  override def shouldStopNow(iters: Int, sampler: Sampler): Boolean = {
    update(sampler.getCurrentValue.asInstanceOf[Boolean])
    completed
  }

  override def getMissingParameter: AnyRef =
    if (!completed) throw new PrismException("Missing parameter not computed yet")
    else status

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  // HypMethod Methods

  /** @note No restriction on total number of samples. */
  override def update(positive: Boolean): Unit = if (positive) logT += q1 else logT += q0

  /** @note
    *   1. Requires `positive >= 0` and `negative >= 0`.
    *   1. No restriction on total number of samples */
  override def update(positive: Int, negative: Int): Unit = logT += positive * q1 + negative * q0

  /** @note The following probabilistic guarantees are made (if [[LB]] is `true` then swap `α` and `β`):
    *   1. If the actual probability is at most  `θ - δ` then the probability of returning [[CompResult.Binary.LARGER]]  is at most `α`.
    *   1. If the actual probability is at least `θ + δ` then the probability of returning [[CompResult.Binary.SMALLER]] is at most `β`. */
  def status(logT: Double): CompResult.Binary =
    if (logT <= logL) CompResult.Binary.SMALLER
    else if (logT >= logU) CompResult.Binary.LARGER
    else CompResult.Binary.UNDECIDED

  /** Same as [[status(logT*]], but input parameter is taken from the current instance */
  @inline
  def status: CompResult.Binary = status(logT)

  override def completed: Boolean = status ne CompResult.Binary.UNDECIDED

  /** @note [[completed]] does not need to be `true` (as required by [[HypTest]]). */
  override def too_close: Boolean = status ne CompResult.Binary.UNDECIDED

  /** @note
    *   1. Requires [[completed]] to be `true`.
    *   1. The following probabilistic guarantees are made:
    *      a. When [[LB]] is `true`:  if the actual probability is at least `θ + δ` then the probability of returning `true` is at most `α`.
    *      a. When [[LB]] is `false`: if the actual probability is at most  `θ - δ` then the probability of returning `true` is at most `α`. */
  override def rejected: Boolean =
    if (LB) status eq CompResult.Binary.SMALLER
    else status eq CompResult.Binary.LARGER

  /** @note
    *   1. Requires [[completed]] to be `true`.
    *   1. The following probabilistic guarantees are made:
    *      a. When [[LB]] is `false`: if the actual probability is at least `θ + δ` then the probability of returning `true` is at most `β`.
    *      a. When [[LB]] is `true`:  if the actual probability is at most  `θ - δ` then the probability of returning `true` is at most `β`. */
  override def failed_to_reject: Boolean = !rejected

}
