/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 + STMC - Statistical Model Checker                                                               +
 +                                                                                                +
 + Copyright (C) 2019                                                                             +
 + Authors:                                                                                       +
 +   Nima Roohi <nroohi@ucsd.edu> (University of California San Diego)                            +
 +                                                                                                +
 + This program is free software: you can redistribute it and/or modify it under the terms        +
 + of the GNU General Public License as published by the Free Software Foundation, either         +
 + version 3 of the License, or (at your option) any later version.                               +
 +                                                                                                +
 + This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;      +
 + without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.      +
 + See the GNU General Public License for more details.                                           +
 +                                                                                                +
 + You should have received a copy of the GNU General Public License along with this program.     +
 + If not, see <https://www.gnu.org/licenses/>.                                                   +
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package edu.stmc

import parser.ast.{Expression, ExpressionProb}
import prism.PrismException
import simulator.sampler.Sampler

/** Stratified Sequential Probability Ratio Test
  *
  * @note
  *   1. See ''Statistical verification of PCTL using antithetic and stratified samples''
  *   ([[https://doi.org/10.1007/s10703-019-00339-8 DOI: 10.1007/s10703-019-00339-8]]) for a reference to this method.
  *   1. Method [[init]] must be called before this test can be actually performed.
  *   1. Probabilistic guarantees in this class ignore numerical errors caused by floating point arithmetic. */
final class HypTestSPRTStratified extends HypTest {

  // Input parameters
  private[this] var threshold: Double = _
  private[this] var alpha: Double = _
  private[this] var beta: Double = _
  private[this] var delta: Double = _
  private[this] var LB: Boolean = _

  // Computed initially based on the input parameters
  private[this] var logL: Double = _
  private[this] var logU: Double = _

  // Test statistic
  // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
  private[this] var mean: Double = _ // mean accumulates the mean of the entire data set
  private[this] var M2: Double = _ // M2 aggregates the squared distance from the mean
  private[this] var iter: Int = _

  /** Initialize or reset this to a hypothesis test in which the null hypothesis is `p = θ - δ` and the alternative hypothesis is `p = θ + δ`, where `p` is
    * the actual probability, `θ` is the input threshold, and `δ` is the half of the size of indifference region.
    *
    * @param threshold Input Threshold
    * @param alpha     Type I  (aka `false positive`) error probability (the probability of incorrectly     rejecting the null hypothesis).
    * @param beta      Type II (aka `false negative`) error probability (the probability of incorrectly not rejecting the null hypothesis).
    * @param delta     Half of the size of indifference region.
    * @param LB        Whether or not the null and alternative hypotheses should be swapped (`true` means they should).
    * @note The following requirements must be met (`θ` refers to the input threshold):
    *   - 0 < θ < 1
    *   - 0 < α < 0.5
    *   - 0 < β < 0.5
    *   - 0 < δ < 0.5
    *   - δ < θ
    *   - δ < 1 - θ
    * @see [[status(logT*]], [[rejected]], [[failed_to_reject]]*/
  def init(threshold: Double, alpha: Double, beta: Double, delta: Double, LB: Boolean = true): HypTestSPRTStratified = {
    require(0 < threshold && threshold < 1, s"Invalid threshold $threshold")
    require(0 < alpha && alpha < 0.5, s"Invalid type I error $alpha")
    require(0 < beta && beta < 0.5, s"Invalid type II error $beta")
    require(0 < delta && delta < 0.5, s"Invalid indifference parameter $delta")
    require(0 < threshold - delta, s"Invalid threshold ($threshold) and/or indifference parameter ($delta)")
    require(1 > threshold + delta, s"Invalid threshold ($threshold) and/or indifference parameter ($delta)")
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
    logL = -Math.log((1 - this.alpha) / this.beta) / (2 * delta)
    logU = +Math.log((1 - this.beta) / this.alpha) / (2 * delta)
    assert(logL < 0, s"Lower-bound log is not negative " + logL)
    assert(logU > 0, s"Upper-bound log is not positive " + logU)

    iter = 0

    this
  }

  private def reset(threshold: Double,
                    alpha: Double, beta: Double, delta: Double,
                    LB: Boolean,
                    logL: Double, logU: Double,
                    mean: Double, M2: Double, iter: Int): HypTestSPRTStratified = {
    this.threshold = threshold
    this.alpha = alpha
    this.beta = beta
    this.delta = delta
    this.LB = LB
    this.logL = logL
    this.logU = logU
    this.mean = mean
    this.M2 = M2
    this.iter = iter
    this
  }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  // SimulationMethod Methods

  override def reset(): Unit = {
    mean = 0
    M2 = 0
    iter = 0
  }
  private[this] val name = if(STMCConfig.samplingMethod == NameSmplMethod.ANTITHETIC) "Antithetic" else "Stratified"
  override def getName: String = s"${name}SPRT"
  override def getFullName: String = s"$name Sequential Probability Ratio Test"
  override def getParametersString: String =
    s"threshold: $threshold, alpha: $alpha, beta: $beta, delta: $delta, LB: $LB, logL: $logL, logU: $logU, strata-sizes: ${STMCConfig.strataSizes.mkString("[", ",", "]")}, strata-size: ${STMCConfig.strataTotalSize}"

  override def getResultExplanation(sampler: Sampler): String = s"$getParametersString, mean: $mean, M2: $M2, iter: $iter"

  override def clone: HypTestSPRTStratified = new HypTestSPRTStratified().reset(threshold, alpha, beta, delta, LB, logL, logU, mean, M2, iter)

  override def setExpression(expr: Expression): Unit =
    if (!expr.isInstanceOf[ExpressionProb])
      throw new PrismException(s"Can only handle expressions of type ExpressionProp. However, type of '$expr' is ${expr.getClass.getName}")
    else {
      val expr2 = expr.asInstanceOf[ExpressionProb]
      val threshold = expr2.getBound.evaluateDouble
      val op = expr2.getRelOp
      init(threshold, STMCConfig.alpha, STMCConfig.beta, STMCConfig.delta, op.isLowerBound)
    }

  override def shouldStopNow(iters: Int, sampler: Sampler): Boolean = iters >= STMCConfig.minIters && completed

  override def getMissingParameter: java.lang.Integer =
  // `SimulationMethod` requires the return type to be either an Integer or a Double object.
    status match {
    case CompResult.Binary.SMALLER   => Int.box(-1)
    case CompResult.Binary.LARGER    => Int.box(+1)
    case CompResult.Binary.UNDECIDED => throw new PrismException("Missing parameter not computed yet")
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  // HypTest Methods

  /** @note No restriction on total number of samples. */
  override def update(positive: Boolean): Unit = update(1, 0)

  private[this] var cc = 0

  /** @note
    *   1. Requires `positive >= 0`.
    *   1. No restriction on total number of samples
    *   1. Value of `negative` is ignored (it is assumed to be [[STMCConfig.strataTotalSize]] - `positive`) */
  override def update(positive: Int, negative: Int): Unit = {
    // See https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
    iter += 1

    val m = positive / STMCConfig.strataTotalSize.toDouble
    val delta = m - mean
    mean += delta / iter
    val delta2 = m - mean
    M2 += delta * delta2

    cc += 1
    if(cc % 1000 == 0)
    println(f"$positive%2d, ${positive/4.0}%1.7f, $mean%1.7f, ${(M2/(iter-1))}")

    //    val Y = positive
//    val blockSize = STMCConfig.strataTotalSize.toDouble
//    v(Y) += 1
//    var mu = 0.0
//    var sig2 = 0.0
//    for (i <- v.indices) mu += i * v(i) / iter.toDouble / blockSize
//    for (i <- v.indices) sig2 += (i * i) / (blockSize * blockSize) * v(i) / iter
//    sig2 -= mu * mu
//    mean = mu
//    M2 = sig2 * (iter-1)
//    print(s"\n$iter: pos:$positive, v:${v.mkString("[", ",", "]")}, mean=$mean, var=$variance")
  }
//  val v = Array.ofDim[Int](STMCConfig.strataTotalSize + 1)

  @inline
  private[this] def variance = M2 / (iter-1) // sample variance (iter must be at least 2)


  /** @note The following probabilistic guarantees are made (if [[LB]] is `true` then swap `α` and `β`):
    *   1. If the actual probability is at most  `θ - δ` then the probability of returning [[CompResult.Binary.LARGER]]  is at most `α`.
    *   1. If the actual probability is at least `θ + δ` then the probability of returning [[CompResult.Binary.SMALLER]] is at most `β`.
    * @see [[init]] where all the parameters are set */
  def status(mean: Double, M2: Double, iter: Int): CompResult.Binary = {
    if (mean - threshold < variance * logL / iter) CompResult.Binary.SMALLER
    else if (mean - threshold > variance * logU / iter) CompResult.Binary.LARGER
    else CompResult.Binary.UNDECIDED
  }

  /** Same as [[status(logT*]], but input parameter is taken from the current instance */
  @inline
  def status: CompResult.Binary = status(mean, M2, iter)

  override def completed: Boolean = status ne CompResult.Binary.UNDECIDED

  /** @return `false` */
  override def too_close: Boolean = false

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
