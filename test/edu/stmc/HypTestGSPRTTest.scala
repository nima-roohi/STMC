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

import java.util.concurrent.ThreadLocalRandom

import org.scalatest.FlatSpec

class HypTestGSPRTTest extends FlatSpec {

  private def run(threshold: Double, alpha: Double, beta: Double, LB: Boolean, minSamples: Int = 200) = {
    val test = new HypTestGSPRT().init(threshold, alpha, beta, minSamples, LB)
    val rnd = ThreadLocalRandom.current()
    while (!test.completed)
      test.update(rnd.nextBoolean())
    test
  }

  private def test(threshold: Double, alpha: Double, beta: Double, LB: Boolean, minSamples: Int = 200) =
    run(threshold, alpha, beta, LB, minSamples).status

  private def testR(threshold: Double, alpha: Double, beta: Double, LB: Boolean, minSamples: Int = 200) =
    run(threshold, alpha, beta, LB, minSamples).rejected

  private def testF(threshold: Double, alpha: Double, beta: Double, LB: Boolean, minSamples: Int = 200) =
    run(threshold, alpha, beta, LB, minSamples).failed_to_reject

  private val r1000 = (1 to 1000).par

  "GSPRT" should "returns SMALLER (ie rejects) very few times when lower-bound (θ) is strictly smaller than the actual probability" in {
    assert(50 > r1000.count(_ => test(0.40, 0.01, 0.01, LB = true) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.40, 0.01, 0.20, LB = true) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.40, 0.01, 0.45, LB = true) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.39, 0.01, 0.45, LB = true) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.30, 0.01, 0.45, LB = true) == CompResult.Binary.SMALLER))

    assert(50 > r1000.count(_ => testF(0.49, 0.00001, 0.00005, LB = false, 1000)))

    assert(50 > r1000.count(_ => testR(0.40, 0.01, 0.01, LB = true)))
    assert(50 > r1000.count(_ => testR(0.40, 0.01, 0.20, LB = true)))
    assert(50 > r1000.count(_ => testR(0.40, 0.01, 0.45, LB = true)))
    assert(50 > r1000.count(_ => testR(0.39, 0.01, 0.45, LB = true)))
    assert(50 > r1000.count(_ => testR(0.30, 0.01, 0.45, LB = true)))
  }

  it should "returns LARGER (ie rejects) very few times when lower-bound (θ) is strictly larger than the actual probability" in {
    assert(50 > r1000.count(_ => test(0.60, 0.01, 0.01, LB = false) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.60, 0.01, 0.20, LB = false) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.60, 0.01, 0.45, LB = false) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.61, 0.01, 0.45, LB = false) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.70, 0.01, 0.45, LB = false) == CompResult.Binary.LARGER))

    assert(50 > r1000.count(_ => testR(0.60, 0.01, 0.01, LB = false)))
    assert(50 > r1000.count(_ => testR(0.60, 0.01, 0.20, LB = false)))
    assert(50 > r1000.count(_ => testR(0.60, 0.01, 0.45, LB = false)))
    assert(50 > r1000.count(_ => testR(0.61, 0.01, 0.45, LB = false)))
    assert(50 > r1000.count(_ => testR(0.70, 0.01, 0.45, LB = false)))
  }

  it should "returns LARGER (ie accepts) very few times when lower-bound (θ) is strictly larger than the actual probability" in {
    assert(50 > r1000.count(_ => test(0.60, 0.01, 0.01, LB = true) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.60, 0.20, 0.01, LB = true) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.60, 0.45, 0.01, LB = true) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.61, 0.47, 0.01, LB = true) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.70, 0.49, 0.01, LB = true) == CompResult.Binary.LARGER))

    assert(50 > r1000.count(_ => testF(0.60, 0.01, 0.01, LB = true)))
    assert(50 > r1000.count(_ => testF(0.60, 0.20, 0.01, LB = true)))
    assert(50 > r1000.count(_ => testF(0.60, 0.45, 0.01, LB = true)))
    assert(50 > r1000.count(_ => testF(0.61, 0.47, 0.01, LB = true)))
    assert(50 > r1000.count(_ => testF(0.70, 0.49, 0.01, LB = true)))
  }

  it should "returns SMALLER (ie accepts) very few times when lower-bound (θ) is strictly smaller than the actual probability" in {
    assert(50 > r1000.count(_ => test(0.40, 0.01, 0.01, LB = false) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.40, 0.20, 0.01, LB = false) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.40, 0.45, 0.01, LB = false) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.39, 0.47, 0.01, LB = false) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.30, 0.49, 0.01, LB = false) == CompResult.Binary.SMALLER))

    assert(50 > r1000.count(_ => testF(0.40, 0.01, 0.01, LB = false)))
    assert(50 > r1000.count(_ => testF(0.40, 0.20, 0.01, LB = false)))
    assert(50 > r1000.count(_ => testF(0.40, 0.45, 0.01, LB = false)))
    assert(50 > r1000.count(_ => testF(0.39, 0.47, 0.01, LB = false)))
    assert(50 > r1000.count(_ => testF(0.30, 0.49, 0.01, LB = false)))
  }

}