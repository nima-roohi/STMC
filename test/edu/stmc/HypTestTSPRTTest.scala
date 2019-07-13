/**************************************************************************************************
 * STMC - Statistical Model Checker                                                               *
 *                                                                                                *
 * Copyright (C) 2019                                                                             *
 * Authors:                                                                                       *
 *   Nima Roohi <nroohi@ucsd.edu> (University of California San Diego)                            *
 *                                                                                                *
 * This program is free software: you can redistribute it and/or modify it under the terms        *
 * of the GNU General Public License as published by the Free Software Foundation, either         *
 * version 3 of the License, or (at your option) any later version.                               *
 *                                                                                                *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;      *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.      *
 * See the GNU General Public License for more details.                                           *
 *                                                                                                *
 * You should have received a copy of the GNU General Public License along with this program.     *
 * If not, see <https://www.gnu.org/licenses/>.                                                   *
 **************************************************************************************************/

package edu.stmc

import java.util.concurrent.ThreadLocalRandom

import org.scalatest._

class HypTestTSPRTTest extends FlatSpec {

  private def run(threshold: Double, alpha: Double, beta: Double, gamma: Double, delta: Double, LB: Boolean) = {
    val test = new HypTestTSPRT().init(threshold, alpha, beta, gamma, delta, LB)
    val rnd = ThreadLocalRandom.current()
    while (!test.completed)
      test.update(rnd.nextBoolean())
    test
  }

  private def test(threshold: Double, alpha: Double, beta: Double, gamma: Double, delta: Double, LB: Boolean) =
    run(threshold, alpha, beta, gamma, delta, LB).status

  private def testR(threshold: Double, alpha: Double, beta: Double, gamma: Double, delta: Double, LB: Boolean) =
    run(threshold, alpha, beta, gamma, delta, LB).rejected

  private def testF(threshold: Double, alpha: Double, beta: Double, gamma: Double, delta: Double, LB: Boolean) =
    run(threshold, alpha, beta, gamma, delta, LB).failed_to_reject

  private def testT(threshold: Double, alpha: Double, beta: Double, gamma: Double, delta: Double, LB: Boolean) =
    run(threshold, alpha, beta, gamma, delta, LB).too_close

  private val r1000 = (1 to 1000).par

  "Ternary SPRT" should "returns TOO_CLOSE very few times when threshold is not strictly within `δ`-neighborhood of the actual probability" in {
    assert(50 > r1000.count(_ => test(0.40, 0.1, 0.2, 0.01, 0.10, LB = true) == CompResult.Ternary.TOO_CLOSE))
    assert(100 > r1000.count(_ => test(0.49, 0.2, 0.4, 0.02, 0.01, LB = true) == CompResult.Ternary.TOO_CLOSE))
    assert(150 > r1000.count(_ => test(0.35, 0.3, 0.3, 0.03, 0.15, LB = true) == CompResult.Ternary.TOO_CLOSE))
    assert(150 > r1000.count(_ => test(0.65, 0.3, 0.3, 0.03, 0.15, LB = true) == CompResult.Ternary.TOO_CLOSE))

    assert(50 > r1000.count(_ => test(0.60, 0.1, 0.2, 0.01, 0.10, LB = false) == CompResult.Ternary.TOO_CLOSE))
    assert(100 > r1000.count(_ => test(0.51, 0.2, 0.1, 0.02, 0.01, LB = false) == CompResult.Ternary.TOO_CLOSE))
    assert(150 > r1000.count(_ => test(0.65, 0.3, 0.4, 0.03, 0.15, LB = false) == CompResult.Ternary.TOO_CLOSE))
    assert(150 > r1000.count(_ => test(0.35, 0.3, 0.4, 0.03, 0.15, LB = false) == CompResult.Ternary.TOO_CLOSE))

    assert(50 > r1000.count(_ => testT(0.40, 0.3, 0.4, 0.01, 0.10, LB = true)))
    assert(100 > r1000.count(_ => testT(0.49, 0.1, 0.3, 0.02, 0.01, LB = true)))
    assert(150 > r1000.count(_ => testT(0.35, 0.2, 0.2, 0.03, 0.15, LB = true)))
    assert(150 > r1000.count(_ => testT(0.65, 0.2, 0.2, 0.03, 0.15, LB = true)))

    assert(50 > r1000.count(_ => testT(0.60, 0.3, 0.2, 0.01, 0.10, LB = false)))
    assert(100 > r1000.count(_ => testT(0.51, 0.1, 0.3, 0.02, 0.01, LB = false)))
    assert(150 > r1000.count(_ => testT(0.65, 0.2, 0.4, 0.03, 0.15, LB = false)))
    assert(150 > r1000.count(_ => testT(0.35, 0.2, 0.4, 0.03, 0.15, LB = false)))
  }

  it should "returns SMALLER (ie rejects) very few times when lower-bound is correct" in {
    assert(50 > r1000.count(_ => test(0.50, 0.01, 0.01, 0.20, 0.1, LB = true) == CompResult.Ternary.SMALLER))
    assert(50 > r1000.count(_ => test(0.50, 0.01, 0.20, 0.30, 0.1, LB = true) == CompResult.Ternary.SMALLER))
    assert(50 > r1000.count(_ => test(0.50, 0.01, 0.45, 0.40, 0.1, LB = true) == CompResult.Ternary.SMALLER))

    assert(50 > r1000.count(_ => test(0.49, 0.01, 0.20, 0.30, 0.1, LB = true) == CompResult.Ternary.SMALLER))
    assert(50 > r1000.count(_ => test(0.41, 0.01, 0.45, 0.40, 0.1, LB = true) == CompResult.Ternary.SMALLER))
    assert(50 > r1000.count(_ => test(0.40, 0.01, 0.45, 0.40, 0.1, LB = true) == CompResult.Ternary.SMALLER))
    assert(50 > r1000.count(_ => test(0.39, 0.01, 0.01, 0.20, 0.1, LB = true) == CompResult.Ternary.SMALLER))

    assert(50 > r1000.count(_ => testR(0.50, 0.01, 0.01, 0.20, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testR(0.50, 0.01, 0.20, 0.30, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testR(0.50, 0.01, 0.45, 0.40, 0.1, LB = true)))

    assert(50 > r1000.count(_ => testR(0.49, 0.01, 0.20, 0.30, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testR(0.41, 0.01, 0.45, 0.40, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testR(0.40, 0.01, 0.45, 0.40, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testR(0.39, 0.01, 0.01, 0.20, 0.1, LB = true)))
  }

  it should "returns LARGER (ie rejects) very few times when upper-bound is correct" in {
    assert(50 > r1000.count(_ => test(0.50, 0.01, 0.01, 0.20, 0.1, LB = false) == CompResult.Ternary.LARGER))
    assert(50 > r1000.count(_ => test(0.50, 0.01, 0.20, 0.30, 0.1, LB = false) == CompResult.Ternary.LARGER))
    assert(50 > r1000.count(_ => test(0.50, 0.01, 0.45, 0.40, 0.1, LB = false) == CompResult.Ternary.LARGER))

    assert(50 > r1000.count(_ => test(0.50, 0.01, 0.01, 0.20, 0.1, LB = false) == CompResult.Ternary.LARGER))
    assert(50 > r1000.count(_ => test(0.51, 0.01, 0.20, 0.30, 0.1, LB = false) == CompResult.Ternary.LARGER))
    assert(50 > r1000.count(_ => test(0.60, 0.01, 0.45, 0.40, 0.1, LB = false) == CompResult.Ternary.LARGER))
    assert(50 > r1000.count(_ => test(0.61, 0.01, 0.45, 0.40, 0.1, LB = false) == CompResult.Ternary.LARGER))

    assert(50 > r1000.count(_ => testR(0.50, 0.01, 0.01, 0.20, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testR(0.50, 0.01, 0.20, 0.30, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testR(0.50, 0.01, 0.45, 0.40, 0.1, LB = false)))

    assert(50 > r1000.count(_ => testR(0.50, 0.01, 0.01, 0.20, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testR(0.51, 0.01, 0.20, 0.30, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testR(0.60, 0.01, 0.45, 0.40, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testR(0.61, 0.01, 0.45, 0.40, 0.1, LB = false)))
  }

  it should "returns LARGER (ie accepts) very few times when lower-bound is incorrect" in {
    assert(50 > r1000.count(_ => test(0.50, 0.01, 0.01, 0.20, 0.1, LB = true) == CompResult.Ternary.LARGER))
    assert(50 > r1000.count(_ => test(0.50, 0.20, 0.01, 0.30, 0.1, LB = true) == CompResult.Ternary.LARGER))
    assert(50 > r1000.count(_ => test(0.50, 0.45, 0.01, 0.40, 0.1, LB = true) == CompResult.Ternary.LARGER))

    assert(50 > r1000.count(_ => test(0.50, 0.01, 0.01, 0.20, 0.1, LB = true) == CompResult.Ternary.LARGER))
    assert(50 > r1000.count(_ => test(0.51, 0.20, 0.01, 0.30, 0.1, LB = true) == CompResult.Ternary.LARGER))
    assert(50 > r1000.count(_ => test(0.60, 0.45, 0.01, 0.40, 0.1, LB = true) == CompResult.Ternary.LARGER))
    assert(50 > r1000.count(_ => test(0.61, 0.45, 0.01, 0.40, 0.1, LB = true) == CompResult.Ternary.LARGER))

    assert(50 > r1000.count(_ => testF(0.50, 0.01, 0.01, 0.20, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testF(0.50, 0.20, 0.01, 0.30, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testF(0.50, 0.45, 0.01, 0.40, 0.1, LB = true)))

    assert(50 > r1000.count(_ => testF(0.50, 0.01, 0.01, 0.20, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testF(0.51, 0.20, 0.01, 0.30, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testF(0.60, 0.45, 0.01, 0.40, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testF(0.61, 0.45, 0.01, 0.40, 0.1, LB = true)))
  }

  it should "returns SMALLER (ie accepts) very few times when upper-bound is incorrect" in {
    assert(50 > r1000.count(_ => test(0.50, 0.01, 0.01, 0.20, 0.1, LB = false) == CompResult.Ternary.SMALLER))
    assert(50 > r1000.count(_ => test(0.50, 0.20, 0.01, 0.30, 0.1, LB = false) == CompResult.Ternary.SMALLER))
    assert(50 > r1000.count(_ => test(0.50, 0.45, 0.01, 0.40, 0.1, LB = false) == CompResult.Ternary.SMALLER))

    assert(50 > r1000.count(_ => test(0.49, 0.20, 0.01, 0.30, 0.1, LB = false) == CompResult.Ternary.SMALLER))
    assert(50 > r1000.count(_ => test(0.41, 0.45, 0.01, 0.40, 0.1, LB = false) == CompResult.Ternary.SMALLER))
    assert(50 > r1000.count(_ => test(0.40, 0.45, 0.01, 0.40, 0.1, LB = false) == CompResult.Ternary.SMALLER))
    assert(50 > r1000.count(_ => test(0.39, 0.01, 0.01, 0.20, 0.1, LB = false) == CompResult.Ternary.SMALLER))

    assert(50 > r1000.count(_ => testF(0.50, 0.01, 0.01, 0.20, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testF(0.50, 0.20, 0.01, 0.30, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testF(0.50, 0.45, 0.01, 0.40, 0.1, LB = false)))

    assert(50 > r1000.count(_ => testF(0.49, 0.20, 0.01, 0.30, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testF(0.41, 0.45, 0.01, 0.40, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testF(0.40, 0.45, 0.01, 0.40, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testF(0.39, 0.01, 0.01, 0.20, 0.1, LB = false)))
  }

  it should "often (ie probability 1-max(α,β)) returns TOO_CLOSE when threshold is the actual probability" in {
    assert(60 < r1000.count(_ => test(0.5, 0.01, 0.02, 0.01, 0.10, LB = true) == CompResult.Ternary.TOO_CLOSE))
    assert(120 < r1000.count(_ => test(0.5, 0.02, 0.04, 0.02, 0.01, LB = true) == CompResult.Ternary.TOO_CLOSE))
    assert(90 < r1000.count(_ => test(0.5, 0.03, 0.03, 0.03, 0.15, LB = true) == CompResult.Ternary.TOO_CLOSE))
    assert(90 < r1000.count(_ => test(0.5, 0.03, 0.03, 0.03, 0.15, LB = true) == CompResult.Ternary.TOO_CLOSE))

    assert(60 < r1000.count(_ => test(0.5, 0.01, 0.02, 0.1, 0.10, LB = false) == CompResult.Ternary.TOO_CLOSE))
    assert(60 < r1000.count(_ => test(0.5, 0.02, 0.01, 0.2, 0.01, LB = false) == CompResult.Ternary.TOO_CLOSE))
    assert(120 < r1000.count(_ => test(0.5, 0.03, 0.04, 0.3, 0.15, LB = false) == CompResult.Ternary.TOO_CLOSE))
    assert(120 < r1000.count(_ => test(0.5, 0.03, 0.04, 0.3, 0.15, LB = false) == CompResult.Ternary.TOO_CLOSE))

    assert(90 < r1000.count(_ => testT(0.5, 0.01, 0.03, 0.04, 0.01, LB = true)))
    assert(60 < r1000.count(_ => testT(0.5, 0.02, 0.02, 0.06, 0.15, LB = true)))
    assert(60 < r1000.count(_ => testT(0.5, 0.02, 0.02, 0.09, 0.15, LB = true)))
    assert(120 < r1000.count(_ => testT(0.5, 0.03, 0.04, 0.02, 0.10, LB = true)))

    assert(90 < r1000.count(_ => testT(0.5, 0.03, 0.02, 0.03, 0.10, LB = false)))
    assert(90 < r1000.count(_ => testT(0.5, 0.01, 0.03, 0.06, 0.01, LB = false)))
    assert(120 < r1000.count(_ => testT(0.5, 0.02, 0.04, 0.12, 0.15, LB = false)))
    assert(120 < r1000.count(_ => testT(0.5, 0.02, 0.04, 0.15, 0.15, LB = false)))
  }

}