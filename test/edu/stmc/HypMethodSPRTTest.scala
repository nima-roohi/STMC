package edu.stmc

import java.util.concurrent.ThreadLocalRandom

import org.scalatest._

class HypMethodSPRTTest extends FlatSpec {

  def binaryTest(threshold: Double, alpha: Double, beta: Double, delta: Double, LB: Boolean): Boolean = {
    val test = new HypMethodSPRT().init(threshold, alpha, beta, delta, LB)
    val rnd = ThreadLocalRandom.current()
    while (!test.completed)
      test.update(rnd.nextBoolean())
    test.failed_to_reject
  }

  def binaryTestCmp(threshold: Double, alpha: Double, beta: Double, delta: Double, LB: Boolean): CompResult.Binary = {
    val test = new HypMethodSPRT().init(threshold, alpha, beta, delta, LB)
    val rnd = ThreadLocalRandom.current()
    var res = CompResult.Binary.UNDECIDED
    while (res == CompResult.Binary.UNDECIDED) {
      test.update(rnd.nextBoolean())
      res = test.status
    }
    res
  }

  "A binary SPRT" should "decide coin flip correctly most of the time, when lower-bound is correct" in {
    val passes = (1 to 1000).par.count(_ => binaryTest(0.4, 0.01, 0.01, 0.1, LB = true))
    assert(passes > 900, s"Expected value is about 990, but the actual value is $passes")
  }

  it should "decide coin flip correctly most of the time, when lower-bound is incorrect" in {
    val passes = (1 to 1000).par.count(_ => binaryTest(0.6, 0.01, 0.01, 0.1, LB = true))
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "decide coin flip correctly most of the time, when upper-bound is correct" in {
    val passes = (1 to 1000).par.count(_ => binaryTest(0.6, 0.01, 0.01, 0.1, LB = false))
    assert(passes > 900, s"Expected value is about 990, but the actual value is $passes")
  }

  it should "decide coin flip correctly most of the time, when upper-bound is incorrect" in {
    val passes = (1 to 1000).par.count(_ => binaryTest(0.4, 0.01, 0.01, 0.1, LB = false))
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  "A binary SPRT" should "compare threshold and actual probability correctly most of the time, when lower-bound is correct" in {
    val passes = (1 to 1000).par.count(_ => binaryTestCmp(0.4, 0.01, 0.01, 0.1, LB = true) == CompResult.Binary.LARGER)
    assert(passes > 900, s"Expected value is about 990, but the actual value is $passes")
  }

  it should "compare threshold and actual probability correctly most of the time, when lower-bound is incorrect" in {
    val passes = (1 to 1000).par.count(_ => binaryTestCmp(0.6, 0.01, 0.01, 0.1, LB = true) == CompResult.Binary.LARGER)
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "compare threshold and actual probability correctly most of the time, when upper-bound is correct" in {
    val passes = (1 to 1000).par.count(_ => binaryTestCmp(0.6, 0.01, 0.01, 0.1, LB = false) == CompResult.Binary.SMALLER)
    assert(passes > 900, s"Expected value is about 990, but the actual value is $passes")
  }

  it should "compare threshold and actual probability correctly most of the time, when upper-bound is incorrect" in {
    val passes = (1 to 1000).par.count(_ => binaryTestCmp(0.4, 0.01, 0.01, 0.1, LB = false) == CompResult.Binary.SMALLER)
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  "Actual error probability" should "be close to alpha when lower-bound should not be rejected" in {
    var passes: Int = 0
    passes = (1 to 10000).par.count(_ => binaryTest(0.4, 0.05, 0.01, 0.1, LB = true))
    assert(9350 < passes && passes < 9650, s"Expected value is about 9500, but the actual value is $passes")
  }

  it should "be close to alpha when upper should not be rejected" in {
    var passes: Int = 0
    passes = (1 to 10000).par.count(_ => binaryTest(0.6, 0.05, 0.01, 0.1, LB = false))
    assert(9350 < passes && passes < 9650, s"Expected value is about 9500, but the actual value is $passes")
  }

  it should "be close to beta when lower-bound should be rejected" in {
    var passes: Int = 0
    passes = (1 to 10000).par.count(_ => binaryTest(0.6, 0.05, 0.01, 0.1, LB = true))
    assert(passes < 250, s"Expected value is about 100, but the actual value is $passes")
  }

  it should "be close to beta when upper-bound should be rejected" in {
    var passes: Int = 0
    passes = (1 to 10000).par.count(_ => binaryTest(0.4, 0.05, 0.01, 0.1, LB = false))
    assert(passes < 250, s"Expected value is about 100, but the actual value is $passes")
  }

}