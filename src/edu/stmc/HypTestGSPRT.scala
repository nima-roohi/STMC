package edu.stmc

import parser.ast.{Expression, ExpressionProb}
import prism.PrismException
import simulator.sampler.Sampler

import scala.math.log

/** Generalized Sequential Probability Ratio Test
  *
  * TODO: Please look at the "Markup" section in https://docs.scala-lang.org/overviews/scaladoc/for-library-authors.html to learn about a few useful markups.
  * TODO: What this method is and why do we need this when there is SPRT.
  * TODO: Please indicate that the probabilistic error guarantees in this class are asymptotic.
  * TODO: If a reference claims that being asymptotic is ok for some application, please include that.
  *
  * @note
  *   1. TODO: Add a Reference
  *   1. Method [[init]] must be called before this test can be actually performed.
  *   1. Probabilistic guarantees in this class ignore numerical errors caused by floating point arithmetic. */
final class HypTestGSPRT() extends HypTest {

  // Input parameters
  private[this] var threshold: Double = _
  private[this] var alpha: Double = _
  private[this] var beta: Double = _
  private[this] var minSamples: Int = _
  private[this] var LB: Boolean = _

  // Computed initially based on the input parameters
  private[this] var logL: Double = _
  private[this] var logU: Double = _
  private[this] var logP0: Double = _
  private[this] var logP1: Double = _

  // Test statistic
  private[this] var N: Int = 0 // Total number of samples
  private[this] var n: Int = 0 // Number of positive samples

  /** Initialize or reset this to a hypothesis test in which the null hypothesis is `p < θ` and the alternative hypothesis is `p > θ`, where `p` is
    * the actual probability, and `θ` is the input threshold.
    *
    * @param threshold  Input Threshold
    * @param alpha      Type I  (aka `false positive`) error probability (the probability of incorrectly     rejecting the null hypothesis).
    * @param beta       Type II (aka `false negative`) error probability (the probability of incorrectly not rejecting the null hypothesis).
    * @param minSamples Minimum number of samples required to make a decision.
    * @param LB         Whether or not the null and alternative hypotheses should be swapped (`true` means they should).
    * @note The following requirements must be met (`θ` refers to the input threshold):
    *   - 0 < θ < 1
    *   - 0 < α < 0.5
    *   - 0 < β < 0.5
    *   - minSamples > 1
    * @see [[status(n*]], [[rejected]], [[failed_to_reject]] */
  def init(threshold: Double, alpha: Double, beta: Double, minSamples: Int, LB: Boolean = true): HypTestGSPRT = {
    require(minSamples > 1, s"Invalid minimum number of samples $minSamples")
    require(0 < threshold && threshold < 1, s"Invalid threshold $threshold")
    require(0 < alpha && alpha < 0.5, s"Invalid type I error $alpha")
    require(0 < beta && beta < 0.5, s"Invalid type II error $beta")

    this.threshold = threshold
    this.minSamples = minSamples
    this.LB = LB
    if (LB) {
      this.alpha = beta
      this.beta = alpha
    } else {
      this.alpha = alpha
      this.beta = beta
    }

    // Lower and upper bounds after which the null hypothesis won't/will be rejected
    logL = log(this.beta / (1 - this.alpha))
    logU = log((1 - this.beta) / this.alpha)
    assert(logL < 0, s"Lower-bound log is not negative $logL")
    assert(logU > 0, s"Upper-bound log is not positive $logU")

    // Step size for negative and positive samples
    logP0 = log(threshold)
    logP1 = log(1 - threshold)
    assert(java.lang.Double.isFinite(logP0), s"logP0 ($logP0) is not a finite number")
    assert(java.lang.Double.isFinite(logP1), s"logP1 ($logP1) is not a finite number")

    this
  }

  private def reset(threshold: Double,
                    alpha: Double, beta: Double,
                    minSamples: Int, LB: Boolean,
                    logL: Double, logU: Double, logP0: Double, logP1: Double,
                    N: Int, n: Int): HypTestGSPRT = {
    this.threshold = threshold
    this.alpha = alpha
    this.beta = beta
    this.minSamples = minSamples
    this.LB = LB
    this.logL = logL
    this.logU = logU
    this.logP0 = logP0
    this.logP1 = logP1
    this.N = N
    this.n = n
    this
  }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  // SimulationMethod Methods

  override def reset(): Unit = {
    N = 0
    n = 0
  }

  override def getName: String = "GSPRT"
  override def getFullName: String = "Generalized Sequential Probability Ratio Test"
  override def getParametersString: String =
    s"threshold: $threshold, alpha: $alpha, beta: $beta, minSamples: $minSamples, LB: $LB, logL: $logL, logU: $logU, logP0: $logP0, logP1: $logP1"

  override def getResultExplanation(sampler: Sampler): String = s"$getParametersString, N: $N, n: $n"

  override def clone: HypTestGSPRT = new HypTestGSPRT().reset(threshold, alpha, beta, minSamples, LB, logL, logU, logP0, logP1, N, n)

  override def setExpression(expr: Expression): Unit =
    if (!expr.isInstanceOf[ExpressionProb])
      throw new PrismException(s"Can only handle expressions of type ExpressionProp. However, type of '$expr' is ${expr.getClass.getName}")
    else {
      val expr2 = expr.asInstanceOf[ExpressionProb]
      val threshold = expr2.getBound.evaluateDouble
      val op = expr2.getRelOp
      init(threshold, STMCConfig.alpha, STMCConfig.beta, STMCConfig.minIters, op.isLowerBound)
    }

  override def shouldStopNow(iters: Int, sampler: Sampler): Boolean = {
    update(sampler.getCurrentValue.asInstanceOf[Boolean])
    completed
  }

  override def getMissingParameter: java.lang.Integer =
  // `SimulationMethod` requires the return type to be either an Integer or a Double object.
    status match {
    case CompResult.Binary.SMALLER   => Int.box(-1)
    case CompResult.Binary.LARGER    => Int.box(+1)
    case CompResult.Binary.UNDECIDED => throw new PrismException("Missing parameter not computed yet")
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  // HypTest Methods

  /** @note At the time of entering this function, total number of samples must be at most 2^^31^^-2. */
  override def update(positive: Boolean): Unit = {
    N += 1
    if (positive)
      n += 1
  }

  /** @note
    *   1. Requires `positive >= 0` and `negative >= 0`.
    *   1. At the time of entering this function, total number of samples plus `positive` plus `negative` must be at most 2^^31^^-1. */
  override def update(positive: Int, negative: Int): Unit = {
    N += positive + negative
    n += positive
  }

  // TODO: Please check the following probabilistic guarantees. Note that α and β should not be swapped (the implementation should be fixed instead).

  /** @note
    *   1. Requires `n ≤ N`
    *   1. Asymptotic guarantees: as total number of samples goes to infinity, the following probabilistic guarantees are made (if [[LB]] is `true` then
    * swap `α` and `β`):
    *      a. if the actual probability is strictly smaller than `θ` then the probability of returning [[CompResult.Binary.LARGER]]  would be at most `α`.
    *      a. if the actual probability is strictly larger  than `θ` then the probability of returning [[CompResult.Binary.SMALLER]] would be at most `β`.
    * @see [[init]] where all the parameters are set */
  def status(N: Int, n: Int): CompResult.Binary = {
    if (N < minSamples) return CompResult.Binary.UNDECIDED
    val mu = n / N.toDouble
    if (mu <= 0 || 1 <= mu) return CompResult.Binary.UNDECIDED
    val logMu0 = log(mu)
    val logMu1 = log(1 - mu)
    assert(java.lang.Double.isFinite(logMu0), s"logMu0 ($logMu0) is not a finite number")
    assert(java.lang.Double.isFinite(logMu1), s"logMu1 ($logMu1) is not a finite number")
    val `N-n` = N - n
    val exp = n * logP0 + `N-n` * logP1
    val logT = if (mu >= threshold) n * logMu0 + `N-n` * logMu1 - exp
               else exp - n * logMu0 + `N-n` * logMu1
    if (logT >= logU) CompResult.Binary.LARGER
    else if (logT <= logL) CompResult.Binary.SMALLER
    else CompResult.Binary.UNDECIDED
  }

  /** Same as [[status(n*]], but input parameter is taken from the current instance */
  @inline
  def status: CompResult.Binary = status(N, n)

  override def completed: Boolean = status ne CompResult.Binary.UNDECIDED

  /** @return `false`*/
  override def too_close: Boolean = false

  /** @note
    *   1. [[completed]] should be `true`.
    *   1. In the current implementation, the maximum number of samples is 2^^31^^-1.
    *   1. Asymptotic guarantee: as total number of samples goes to infinity,
    *      a. When [[LB]] is `true`:  if the actual probability is strictly larger  than θ then the probability of returning `true` would be at most `α`.
    *      a. When [[LB]] is `false`: if the actual probability is strictly smaller than θ then the probability of returning `true` would be at most `α`. */
  override def rejected: Boolean =
    if (LB) status eq CompResult.Binary.SMALLER
    else status eq CompResult.Binary.LARGER

  /** @note
    *   1. [[completed]] should be `true`.
    *   1. In the current implementation, the maximum number of samples is 2^^31^^-1.
    *   1. Asymptotic guarantee: as total number of samples goes to infinity,
    *      a. When [[LB]] is `true`:  if the actual probability is strictly smaller than θ then the probability of returning `true` would be at most `β`.
    *      a. When [[LB]] is `false`: if the actual probability is strictly larger  than θ then the probability of returning `true` would be at most `β`. */
  override def failed_to_reject: Boolean = !rejected

}
