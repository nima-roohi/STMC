package edu.stmc

import java.util.concurrent.ThreadLocalRandom

import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.matchers.{MatchResult, Matcher}

class HypTestSPRTTest extends FlatSpec with Matchers {

  private def run(threshold: Double, alpha: Double, beta: Double, delta: Double, LB: Boolean) = {
    val test = new HypTestSPRT().init(threshold, alpha, beta, delta, LB)
    val rnd = ThreadLocalRandom.current()
    while (!test.completed)
      test.update(rnd.nextBoolean())
    test
  }

  private def test(threshold: Double, alpha: Double, beta: Double, delta: Double, LB: Boolean) =
    run(threshold, alpha, beta, delta, LB).status

  private def testR(threshold: Double, alpha: Double, beta: Double, delta: Double, LB: Boolean) =
    run(threshold, alpha, beta, delta, LB).rejected

  private def testF(threshold: Double, alpha: Double, beta: Double, delta: Double, LB: Boolean) =
    run(threshold, alpha, beta, delta, LB).failed_to_reject

  private val r1000 = (1 to 1000).par
  private val r10000 = (1 to 10000).par

  "SPRT" should "returns SMALLER (ie rejects) very few times when lower-bound (θ) is at least δ smaller than the actual probability" in {
    assert(50 > r1000.count(_ => test(0.40, 0.01, 0.01, 0.1, LB = true) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.40, 0.01, 0.20, 0.1, LB = true) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.40, 0.01, 0.45, 0.1, LB = true) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.39, 0.01, 0.45, 0.1, LB = true) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.30, 0.01, 0.45, 0.1, LB = true) == CompResult.Binary.SMALLER))

    assert(50 > r1000.count(_ => testR(0.40, 0.01, 0.01, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testR(0.40, 0.01, 0.20, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testR(0.40, 0.01, 0.45, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testR(0.39, 0.01, 0.45, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testR(0.30, 0.01, 0.45, 0.1, LB = true)))
  }

  it should "returns LARGER (ie rejects) very few times when lower-bound (θ) is at least δ larger than the actual probability" in {
    assert(50 > r1000.count(_ => test(0.60, 0.01, 0.01, 0.1, LB = false) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.60, 0.01, 0.20, 0.1, LB = false) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.60, 0.01, 0.45, 0.1, LB = false) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.61, 0.01, 0.45, 0.1, LB = false) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.70, 0.01, 0.45, 0.1, LB = false) == CompResult.Binary.LARGER))

    assert(50 > r1000.count(_ => testR(0.60, 0.01, 0.01, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testR(0.60, 0.01, 0.20, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testR(0.60, 0.01, 0.45, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testR(0.61, 0.01, 0.45, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testR(0.70, 0.01, 0.45, 0.1, LB = false)))
  }

  it should "returns LARGER (ie accepts) very few times when lower-bound (θ) is at least δ larger than the actual probability" in {
    assert(50 > r1000.count(_ => test(0.60, 0.01, 0.01, 0.1, LB = true) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.60, 0.20, 0.01, 0.1, LB = true) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.60, 0.45, 0.01, 0.1, LB = true) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.61, 0.47, 0.01, 0.1, LB = true) == CompResult.Binary.LARGER))
    assert(50 > r1000.count(_ => test(0.70, 0.49, 0.01, 0.1, LB = true) == CompResult.Binary.LARGER))

    assert(50 > r1000.count(_ => testF(0.60, 0.01, 0.01, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testF(0.60, 0.20, 0.01, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testF(0.60, 0.45, 0.01, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testF(0.61, 0.47, 0.01, 0.1, LB = true)))
    assert(50 > r1000.count(_ => testF(0.70, 0.49, 0.01, 0.1, LB = true)))
  }

  it should "returns SMALLER (ie accepts) very few times when lower-bound (θ) is at least δ smaller than the actual probability" in {
    assert(50 > r1000.count(_ => test(0.40, 0.01, 0.01, 0.1, LB = false) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.40, 0.20, 0.01, 0.1, LB = false) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.40, 0.45, 0.01, 0.1, LB = false) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.39, 0.47, 0.01, 0.1, LB = false) == CompResult.Binary.SMALLER))
    assert(50 > r1000.count(_ => test(0.30, 0.49, 0.01, 0.1, LB = false) == CompResult.Binary.SMALLER))

    assert(50 > r1000.count(_ => testF(0.40, 0.01, 0.01, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testF(0.40, 0.20, 0.01, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testF(0.40, 0.45, 0.01, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testF(0.39, 0.47, 0.01, 0.1, LB = false)))
    assert(50 > r1000.count(_ => testF(0.30, 0.49, 0.01, 0.1, LB = false)))
  }

  def beInRange(lb: Int, ub: Int) = new BeInRange(lb, ub)
  class BeInRange(lb: Int, ub: Int) extends Matcher[Int] {
    def apply(v: Int) =
      MatchResult(lb <= v && v <= ub, s"$v is not in [$lb,$ub]", s"$v is in [$lb,$ub]")
  }

  "Actual Type-I error probability in SPRT" should "be close to α when lower-bound (θ) is exactly δ smaller than the actual probability" in {
    r10000.count(_ => testR(0.4, 0.05, 0.01, 0.1, LB = true)) should beInRange(300, 700)
    r10000.count(_ => testR(0.4, 0.10, 0.01, 0.1, LB = true)) should beInRange(800, 1200)
    r10000.count(_ => testR(0.4, 0.05, 0.10, 0.1, LB = true)) should beInRange(300, 700)
    r10000.count(_ => testR(0.4, 0.05, 0.40, 0.1, LB = true)) should beInRange(300, 700)
    r10000.count(_ => testR(0.4, 0.05, 0.05, 0.1, LB = true)) should beInRange(300, 700)
  }

  it should "be close to α when upper-bound (θ) is exactly δ larger than the actual probability" in {
    r10000.count(_ => testR(0.6, 0.05, 0.01, 0.1, LB = false)) should beInRange(300, 700)
    r10000.count(_ => testR(0.6, 0.10, 0.01, 0.1, LB = false)) should beInRange(800, 1200)
    r10000.count(_ => testR(0.6, 0.05, 0.10, 0.1, LB = false)) should beInRange(300, 700)
    r10000.count(_ => testR(0.6, 0.05, 0.40, 0.1, LB = false)) should beInRange(300, 700)
    r10000.count(_ => testR(0.6, 0.05, 0.05, 0.1, LB = false)) should beInRange(300, 700)
  }

  "Actual Type-II error probability in SPRT" should "be close to β when lower-bound (θ) is exactly δ larger than the actual probability" in {
    r10000.count(_ => testF(0.6, 0.01, 0.05, 0.1, LB = true)) should beInRange(300, 700)
    r10000.count(_ => testF(0.6, 0.01, 0.10, 0.1, LB = true)) should beInRange(800, 1200)
    r10000.count(_ => testF(0.6, 0.10, 0.05, 0.1, LB = true)) should beInRange(300, 700)
    r10000.count(_ => testF(0.6, 0.40, 0.05, 0.1, LB = true)) should beInRange(300, 700)
    r10000.count(_ => testF(0.6, 0.05, 0.05, 0.1, LB = true)) should beInRange(300, 700)
  }

  it should "be close to β when upper-bound (θ) is exactly δ smaller than the actual probability" in {
    r10000.count(_ => testF(0.4, 0.01, 0.05, 0.1, LB = false)) should beInRange(300, 700)
    r10000.count(_ => testF(0.4, 0.01, 0.10, 0.1, LB = false)) should beInRange(800, 1200)
    r10000.count(_ => testF(0.4, 0.10, 0.05, 0.1, LB = false)) should beInRange(300, 700)
    r10000.count(_ => testF(0.4, 0.40, 0.05, 0.1, LB = false)) should beInRange(300, 700)
    r10000.count(_ => testF(0.4, 0.05, 0.05, 0.1, LB = false)) should beInRange(300, 700)
  }

}