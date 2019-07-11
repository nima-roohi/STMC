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

  "A ternary SPRT" should "decide SMALLER very few times when lower-bound is correct" in {
    val passes = (1 to 1000).par.count(_ => test(0.5, 0.01, 0.01, 0.01, 0.1, LB = true) == CompResult.Ternary.SMALLER)
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "decide LARGER very few times when lower-bound is incorrect" in {
    val passes = (1 to 1000).par.count(_ => test(0.5, 0.01, 0.01, 0.01, 0.1, LB = true) == CompResult.Ternary.LARGER)
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "decide LARGER very few times when upper-bound is correct" in {
    val passes = (1 to 1000).par.count(_ => test(0.5, 0.01, 0.01, 0.01, 0.1, LB = false) == CompResult.Ternary.LARGER)
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "decide SMALLER very few times when upper-bound is incorrect" in {
    val passes = (1 to 1000).par.count(_ => test(0.5, 0.01, 0.01, 0.01, 0.1, LB = false) == CompResult.Ternary.SMALLER)
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "decide TOO_CLOSE very few times when lower-bound is correct and not too close" in {
    val passes = (1 to 1000).par.count(_ => test(0.49, 0.01, 0.01, 0.01, 0.01, LB = true) == CompResult.Ternary.TOO_CLOSE)
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "decide TOO_CLOSE very few times when upper-bound is correct and not too close" in {
    val passes = (1 to 1000).par.count(_ => test(0.51, 0.01, 0.01, 0.01, 0.01, LB = false) == CompResult.Ternary.TOO_CLOSE)
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "decide TOO_CLOSE very few times when lower-bound is incorrect but not too close" in {
    val passes = (1 to 1000).par.count(_ => test(0.51, 0.01, 0.01, 0.01, 0.01, LB = true) == CompResult.Ternary.TOO_CLOSE)
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "decide TOO_CLOSE very few times when upper-bound is incorrect but not too close" in {
    val passes = (1 to 1000).par.count(_ => test(0.49, 0.01, 0.01, 0.01, 0.01, LB = false) == CompResult.Ternary.TOO_CLOSE)
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "reject very few times when lower-bound is correct" in {
    val passes = (1 to 1000).par.count(_ => testR(0.5, 0.01, 0.01, 0.01, 0.1, LB = true))
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "reject very few times when upper-bound is correct" in {
    val passes = (1 to 1000).par.count(_ => testR(0.5, 0.01, 0.01, 0.01, 0.1, LB = false))
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "fail to reject very few times when lower-bound is incorrect" in {
    val passes = (1 to 1000).par.count(_ => testF(0.5, 0.01, 0.01, 0.01, 0.1, LB = true))
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  it should "fail to reject very few times when upper-bound is incorrect" in {
    val passes = (1 to 1000).par.count(_ => testF(0.5, 0.01, 0.01, 0.01, 0.1, LB = false))
    assert(passes < 100, s"Expected value is about 10, but the actual value is $passes")
  }

  "A ternary SPRT" should "have its actual Type I error probability close to α=γ when lower-bound is the actual probability" in {
    val passes = (1 to 10000).par.count(_ => testR(0.5, 0.03, 0.06, 0.03, 0.1, LB = true))
    assert(100 < passes && passes < 500, s"Expected value is about 300, but the actual value is $passes")
  }

  it should "have its actual Type I error probability close to α=γ when upper-bound is the actual probability" in {
    val passes = (1 to 10000).par.count(_ => testR(0.5, 0.03, 0.06, 0.03, 0.1, LB = false))
    assert(100 < passes && passes < 500, s"Expected value is about 300, but the actual value is $passes")
  }

  it should "have its actual Type II error probability close to β=γ when lower-bound is the actual probability" in {
    val passes = (1 to 10000).par.count(_ => testF(0.5, 0.03, 0.06, 0.06, 0.1, LB = true))
    assert(400 < passes && passes < 800, s"Expected value is about 600, but the actual value is $passes")
  }

  it should "have its actual Type II error probability close to β=γ when upper-bound is the actual probability" in {
    val passes = (1 to 10000).par.count(_ => testF(0.5, 0.03, 0.06, 0.06, 0.1, LB = false))
    assert(400 < passes && passes < 800, s"Expected value is about 600, but the actual value is $passes")
  }

  it should "have its actual Type III error probability close to γ when lower-bound is the actual probability + δ" in {
    val passes = (1 to 10000).par.count(_ => testT(0.51, 0.1, 0.06, 0.03, 0.01, LB = true))
    assert(100 < passes && passes < 500, s"Expected value is about 300, but the actual value is $passes")
  }

  it should "have its actual Type III error probability close to γ when upper-bound is the actual probability - δ" in {
    val passes = (1 to 10000).par.count(_ => testT(0.49, 0.1, 0.06, 0.03, 0.01, LB = false))
    assert(100 < passes && passes < 500, s"Expected value is about 300, but the actual value is $passes")
  }

}