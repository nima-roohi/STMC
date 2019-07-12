/******************************************************************************
 * STMC - Statistical Model Checker                                           *
 *                                                                            *
 * Copyright (C) 2019                                                         *
 * Authors:                                                                   *
 *     Nima Roohi <nroohi@ucsd.edu> (University of California San Diego)      *
 *                                                                            *
 * This file is part of STMC.                                                 *
 *                                                                            *
 * STMC is free software: you can redistribute it and/or modify               *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * Foobar is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with Foobar.  If not, see <https://www.gnu.org/licenses/>.           *
 ******************************************************************************/

package edu.stmc

import parser.ast.{Expression, ExpressionProb}
import prism.PrismException
import simulator.sampler.Sampler

/** Ternary Sequential Probability Ratio Test
  *
  * ''Ternary'' means if the algorithm terminates, there are three possibilities for its answer:
  *   1. ''reject'': null hypothesis is rejected,
  *   1. ''failed_to_reject'': null hypothesis is not rejected, and
  *   1. ''too_close'': threshold and actual probability are too close to make a call.
  *
  * @note
  *   1. Transforming binary error guarantee in SPRT to ternary error guarantee as specified in [[init]] is first introduced in
  *   [[https://www.doi.org/10.1007/11609773_10 2006 - Error Control for Probabilistic Model Checking, by Håkan L. S. Younes]]
  *   (the paper does not coin any new name).
  *   1. Method [[init]] must be called before this test can be actually performed.
  *   1. Probabilistic guarantees in this class ignore numerical errors caused by floating point arithmetic.
  *   1. See [[HypTestSPRT]] for a comment on actual error probabilities. */
final class HypTestTSPRT private(private[this] var lb: HypTestSPRT,
                                 private[this] var ub: HypTestSPRT,
                                 private[this] var LB: Boolean) extends HypTest {

  /** Create an uninitialized instance of this method. */
  def this() = this(new HypTestSPRT, new HypTestSPRT, false)

  /** Initialize or reset this to a hypothesis test in which the null hypothesis is `p = θ - δ` and the alternative hypothesis is `p = θ + δ`, where `p` is
    * the actual probability, `θ` is the input threshold, and `δ` is the half of the size of indifference region.
    *
    * @param threshold Input Threshold
    * @param alpha     Type I   (aka `false positive`)  error probability (the probability of incorrectly     rejecting the null hypothesis).
    * @param beta      Type II  (aka `false negative`)  error probability (the probability of incorrectly not rejecting the null hypothesis).
    * @param gamma     Type III (aka `false too_close`) error probability (the probability of incorrectly call it too close).
    * @param delta     Half of the size of indifference region.
    * @param LB        Whether or not the null and alternative hypotheses should be swapped (`true` means they should).
    * @note The following requirements must be met (`θ` refers to the input threshold):
    *   - 0 < θ < 1
    *   - 0 < α < 0.5
    *   - 0 < β < 0.5
    *   - 0 < γ < 0.5
    *   - 0 < δ < 0.5
    *   - δ < θ
    *   - δ < 1 - θ
    * @see [[status(lb*]], [[too_close]], [[rejected]], [[failed_to_reject]] */
  def init(threshold: Double, alpha: Double, beta: Double, gamma: Double, delta: Double, LB: Boolean = true): HypTestTSPRT = {
    val half_delta = delta / 2
    this.LB = LB
    if (LB) {
      lb.init(threshold - half_delta, gamma, alpha, half_delta, LB = false)
      ub.init(threshold + half_delta, beta, gamma, half_delta, LB = false)
    } else {
      lb.init(threshold - half_delta, gamma, beta, half_delta, LB = false)
      ub.init(threshold + half_delta, alpha, gamma, half_delta, LB = false)
    }
    this
  }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  // SimulationMethod Methods

  override def reset(): Unit = {
    lb.reset()
    ub.reset()
  }

  override def getName: String = "TSPRT"
  override def getFullName: String = "Ternary Sequential Probability Ratio Test"
  override def getParametersString: String = s"lower-bound (${lb.getParametersString}), upper-bound (${ub.getParametersString})"

  override def getResultExplanation(sampler: Sampler): String =
    s"lower-bound (${lb.getResultExplanation(sampler)}), upper-bound (${ub.getResultExplanation(sampler)})"

  override def clone: HypTestTSPRT = new HypTestTSPRT(lb.clone, ub.clone, LB)

  override def setExpression(expr: Expression): Unit =
    if (!expr.isInstanceOf[ExpressionProb])
      throw new PrismException(s"Can only handle expressions of type ExpressionProp. However, type of '$expr' is ${expr.getClass.getName}")
    else {
      val expr2 = expr.asInstanceOf[ExpressionProb]
      val threshold = expr2.getBound.evaluateDouble
      val op = expr2.getRelOp
      init(threshold, STMCConfig.alpha, STMCConfig.beta, STMCConfig.gamma, STMCConfig.delta, op.isLowerBound)
    }

  override def shouldStopNow(iters: Int, sampler: Sampler): Boolean = {
    update(sampler.getCurrentValue.asInstanceOf[Boolean])
    completed
  }

  override def getMissingParameter: java.lang.Integer =
  // `SimulationMethod` requires the return type to be either an Integer or a Double object.
    status match {
    case CompResult.Ternary.SMALLER   => Int.box(-1)
    case CompResult.Ternary.TOO_CLOSE => Int.box(0)
    case CompResult.Ternary.LARGER    => Int.box(+1)
    case CompResult.Ternary.UNDECIDED => throw new PrismException("Missing parameter not computed yet")
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  // HypTest Methods

  /** @note No restriction on total number of samples. */
  override def update(positive: Boolean): Unit = {
    lb.update(positive)
    ub.update(positive)
  }

  /** @note
    *   1. Requires `positive >= 0` and `negative >= 0`.
    *   1. No restriction on total number of samples */
  override def update(positive: Int, negative: Int): Unit = {
    lb.update(positive, negative)
    ub.update(positive, negative)
  }

  /** @note The following probabilistic guarantees are made (if `LB` is `true` then swap `α` and `β`):
    *   1. If the actual probability is at most  `θ` then the probability of returning [[CompResult.Ternary.LARGER]]  is at most `α`.
    *   1. If the actual probability is at least `θ` then the probability of returning [[CompResult.Ternary.SMALLER]] is at most `β`.
    *   1. If `θ` is not strictly within `δ`-neighborhood of the actual probability then the probability of returning [[CompResult.Ternary.TOO_CLOSE]]
    *       is at most `γ`.
    * @see [[init]] where all the parameters are set */
  def status(lb: CompResult.Binary, ub: CompResult.Binary): CompResult.Ternary =
    if ((lb eq CompResult.Binary.UNDECIDED) || (ub eq CompResult.Binary.UNDECIDED)) CompResult.Ternary.UNDECIDED
    else if (lb != ub) CompResult.Ternary.TOO_CLOSE
    else if (lb eq CompResult.Binary.SMALLER) CompResult.Ternary.SMALLER
    else CompResult.Ternary.LARGER

  /** Same as [[status(lb*]], but input parameters are taken from the current instance */
  @inline
  def status: CompResult.Ternary = status(lb.status, ub.status)

  override def completed: Boolean = status ne CompResult.Ternary.UNDECIDED

  /** @note
    *   1. Requires [[completed]] to be `true`.
    *   1. If `θ` is ''not'' strictly within `δ`-neighborhood of the actual probability then the probability of returning `true` is at most `γ`. */
  override def too_close: Boolean = status eq CompResult.Ternary.TOO_CLOSE

  /** @note
    *   1. Requires [[completed]] to be `true`.
    *   1. The following probabilistic guarantees are made:
    *      a. When `LB` is `true`:  if the actual probability is at least `θ` then the probability of returning `true` is at most `α`.
    *      a. When `LB` is `false`: if the actual probability is at most  `θ` then the probability of returning `true` is at most `α`. */
  override def rejected: Boolean =
    if (LB) status eq CompResult.Ternary.SMALLER
    else status eq CompResult.Ternary.LARGER

  /** @note
    *   1. Requires [[completed]] to be `true`.
    *   1. The following probabilistic guarantees are made:
    *      a. When `LB` is `false`: if the actual probability is at least `θ` then the probability of returning `true` is at most `β`.
    *      a. When `LB` is `true`:  if the actual probability is at most  `θ` then the probability of returning `true` is at most `β`. */
  override def failed_to_reject: Boolean =
    if (LB) status eq CompResult.Ternary.LARGER
    else status eq CompResult.Ternary.SMALLER

}
