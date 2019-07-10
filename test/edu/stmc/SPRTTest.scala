package edu.stmc

import java.util.concurrent.ThreadLocalRandom

import org.scalatest._

class SPRTTest extends FlatSpec {

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

  it should "compare threshold and actual probabilities correctly most of the time, when lower-bound is correct" in {
    val passes = (1 to 1000).par.count(_ => binaryTestCmp(0.4, 0.01, 0.01, 0.1, LB = true) == CompResult.Binary.LARGER)
    assert(passes > 900, s"Expected value is about 990, but the actual value is $passes")
  }

  it should "compare threshold and actual probabilities correctly most of the time, when lower-bound is incorrect" in {
    val passes = (1 to 1000).par.count(_ => binaryTestCmp(0.6, 0.01, 0.01, 0.1, LB = true) == CompResult.Binary.SMALLER)
    assert(passes > 900, s"Expected value is about 990, but the actual value is $passes")
  }

  it should "compare threshold and actual probabilities correctly most of the time, when upper-bound is correct" in {
    val passes = (1 to 1000).par.count(_ => binaryTestCmp(0.6, 0.01, 0.01, 0.1, LB = false) == CompResult.Binary.SMALLER)
    assert(passes > 900, s"Expected value is about 990, but the actual value is $passes")
  }

  it should "compare threshold and actual probabilities correctly most of the time, when upper-bound is incorrect" in {
    val passes = (1 to 1000).par.count(_ => binaryTestCmp(0.4, 0.01, 0.01, 0.1, LB = false) == CompResult.Binary.LARGER)
    assert(passes > 900, s"Expected value is about 990, but the actual value is $passes")
  }

  "Actual error probability in SPRT" should "be close to alpha when lower-bound should not be rejected" in {
    var passes: Int = 0
    passes = (1 to 10000).par.count(_ => binaryTest(0.4, 0.05, 0.01, 0.1, LB = true))
    assert(9200 < passes && passes < 9800, s"Expected value is about 9500, but the actual value is $passes")
  }

  it should "be close to alpha when upper should not be rejected" in {
    var passes: Int = 0
    passes = (1 to 10000).par.count(_ => binaryTest(0.6, 0.05, 0.01, 0.1, LB = false))
    assert(9200 < passes && passes < 9800, s"Expected value is about 9500, but the actual value is $passes")
  }

  it should "be close to beta when lower-bound should be rejected" in {
    var passes: Int = 0
    passes = (1 to 10000).par.count(_ => binaryTest(0.6, 0.05, 0.01, 0.1, LB = true))
    assert(passes < 300, s"Expected value is about 100, but the actual value is $passes")
  }

  it should "be close to beta when upper-bound should be rejected" in {
    var passes: Int = 0
    passes = (1 to 10000).par.count(_ => binaryTest(0.4, 0.05, 0.01, 0.1, LB = false))
    assert(passes < 300, s"Expected value is about 100, but the actual value is $passes")
  }

  //  "A ternary SPRT" should "decide coin flip correctly most of the time, when the right answer is LARGER" in {
  //    val passes = (1 to 1000).par.count(_ => {
  //      val rnd = ThreadLocalRandom.current()
  //      val test = new TSPRT(0.4, 0.01, 0.01, 0.01, 0.1)
  //      var res = CompResult.Ternary.UNDECIDED
  //      while (res == CompResult.Ternary.UNDECIDED) {
  //        test.update(rnd.nextBoolean())
  //        res = test.status
  //      }
  //      res == CompResult.Ternary.LARGER
  //    })
  //    assert(passes > 900, s"Expected passes to be at least 990, but it is $passes")
  //  }
  //
  //  it should "decide coin flip correctly most of the time, when the right answer is SMALLER" in {
  //    val passes = (1 to 1000).par.count(_ => {
  //      val rnd = ThreadLocalRandom.current()
  //      val test = new TSPRT(0.6, 0.01, 0.01, 0.01, 0.1)
  //      var res = CompResult.Ternary.UNDECIDED
  //      while (res == CompResult.Ternary.UNDECIDED) {
  //        test.update(rnd.nextBoolean())
  //        res = test.status
  //      }
  //      res == CompResult.Ternary.LARGER
  //    })
  //    assert(passes < 100, s"Expected passes to be at most 10, but it is $passes")
  //  }
  //
  //  it should "not return too many wrong SMALLER when the indifference region is not respected" in {
  //    val failures = (1 to 1000).par.count(_ => {
  //      val rnd = ThreadLocalRandom.current()
  //      val test = new TSPRT(0.49999999, 0.01, 0.01, 0.01, 0.1)
  //      var res = CompResult.Ternary.UNDECIDED
  //      while (res == CompResult.Ternary.UNDECIDED) {
  //        test.update(rnd.nextBoolean())
  //        res = test.status
  //      }
  //      res == CompResult.Ternary.SMALLER
  //    })
  //    assert(failures < 100, s"Expected failures to be at most 10, but it is $failures")
  //  }
  //
  //  it should "not return too many wrong LARGER when the indifference region is not respected" in {
  //    val failures = (1 to 1000).par.count(_ => {
  //      val rnd = ThreadLocalRandom.current()
  //      val test = new TSPRT(0.50000001, 0.01, 0.01, 0.01, 0.1)
  //      var res = CompResult.Ternary.UNDECIDED
  //      while (res == CompResult.Ternary.UNDECIDED) {
  //        test.update(rnd.nextBoolean())
  //        res = test.status
  //      }
  //      res == CompResult.Ternary.LARGER
  //    })
  //    assert(failures < 100, s"Expected failures to be at most 10, but it is $failures")
  //  }

}