package edu.stmc

import java.util.concurrent.ThreadLocalRandom

import org.scalatest._

import scala.util.Random

class SPRTTest extends FlatSpec {

  "A binary SPRT" should "decide coin flip correctly most of the time, when the right answer is LARGER" in {
    val passes = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Binary(0.4, 0.01, 0.01, 0.1)
      var res = CompResult.Binary.UNDECIDED
      while (res == CompResult.Binary.UNDECIDED) {
        test.update(rnd.nextBoolean())
        res = test.status()
      }
      res == CompResult.Binary.LARGER
    })
    assert(passes > 900, s"Expected passes to be at least 990, but it is $passes")
  }

  it should "decide coin flip correctly most of the time, when the right answer is SMALLER" in {
    val passes = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Binary(0.6, 0.01, 0.01, 0.1)
      var res = CompResult.Binary.UNDECIDED
      while (res == CompResult.Binary.UNDECIDED) {
        test.update(rnd.nextBoolean())
        res = test.status()
      }
      res == CompResult.Binary.LARGER
    })
    assert(passes < 100, s"Expected passes to be at most 10, but it is $passes")
  }

  "A ternary SPRT" should "decide coin flip correctly most of the time, when the right answer is LARGER" in {
    val passes = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Ternary(0.4, 0.01, 0.01, 0.01, 0.1)
      var res = CompResult.Ternary.UNDECIDED
      while (res == CompResult.Ternary.UNDECIDED) {
        test.update(rnd.nextBoolean())
        res = test.status()
      }
      res == CompResult.Ternary.LARGER
    })
    assert(passes > 900, s"Expected passes to be at least 990, but it is $passes")
  }

  it should "decide coin flip correctly most of the time, when the right answer is SMALLER" in {
    val passes = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Ternary(0.6, 0.01, 0.01, 0.01, 0.1)
      var res = CompResult.Ternary.UNDECIDED
      while (res == CompResult.Ternary.UNDECIDED) {
        test.update(rnd.nextBoolean())
        res = test.status()
      }
      res == CompResult.Ternary.LARGER
    })
    assert(passes < 100, s"Expected passes to be at most 10, but it is $passes")
  }

  it should "not return too many wrong SMALLER when the indifference region is not respected" in {
    val failures = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Ternary(0.49999999, 0.01, 0.01, 0.01, 0.1)
      var res = CompResult.Ternary.UNDECIDED
      while (res == CompResult.Ternary.UNDECIDED) {
        test.update(rnd.nextBoolean())
        res = test.status()
      }
      res == CompResult.Ternary.SMALLER
    })
    assert(failures < 100, s"Expected failures to be at most 10, but it is $failures")
  }

  it should "not return too many wrong LARGER when the indifference region is not respected" in {
    val failures = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Ternary(0.50000001, 0.01, 0.01, 0.01, 0.1)
      var res = CompResult.Ternary.UNDECIDED
      while (res == CompResult.Ternary.UNDECIDED) {
        test.update(rnd.nextBoolean())
        res = test.status()
      }
      res == CompResult.Ternary.LARGER
    })
    assert(failures < 100, s"Expected failures to be at most 10, but it is $failures")
  }

}