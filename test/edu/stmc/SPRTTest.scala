package edu.stmc

import java.util.concurrent.ThreadLocalRandom

import org.scalatest._

import scala.util.Random

class SPRTTest extends FlatSpec {

  "A binary SPRT" should "decide coin flip correctly most of the time, when the right answer is YES" in {
    val passes = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Binary(0.4,0.01,0.01, 0.1)
      var res = GTResult.Binary.UNDECIDED
      while(res == GTResult.Binary.UNDECIDED)
        res =  test.check(rnd.nextBoolean())
      res == GTResult.Binary.YES
    })
    assert(passes > 900, s"Expected passes to be at least 990, but it is $passes")
  }

  it should "decide coin flip correctly most of the time, when the right answer is NO" in {
    val passes = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Binary(0.6,0.01,0.01, 0.1)
      var res = GTResult.Binary.UNDECIDED
      while(res == GTResult.Binary.UNDECIDED)
        res =  test.check(rnd.nextBoolean())
      res == GTResult.Binary.YES
    })
    assert(passes < 100, s"Expected passes to be at most 10, but it is $passes")
  }

  "A ternary SPRT" should "decide coin flip correctly most of the time, when the right answer is YES" in {
    val passes = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Ternary(0.4,0.01,0.01, 0.01,0.1)
      var res = GTResult.Ternary.UNDECIDED
      while(res == GTResult.Ternary.UNDECIDED)
        res =  test.check(rnd.nextBoolean())
      res == GTResult.Ternary.YES
    })
    assert(passes > 900, s"Expected passes to be at least 990, but it is $passes")
  }

  it should "decide coin flip correctly most of the time, when the right answer is NO" in {
    val passes = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Ternary(0.6,0.01,0.01, 0.01,0.1)
      var res = GTResult.Ternary.UNDECIDED
      while(res == GTResult.Ternary.UNDECIDED)
        res =  test.check(rnd.nextBoolean())
      res == GTResult.Ternary.YES
    })
    assert(passes < 100, s"Expected passes to be at most 10, but it is $passes")
  }

  it should "not return too many wrong NO when the indifference region is not respected" in {
    val failures = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Ternary(0.49999999,0.01,0.01, 0.01,0.1)
      var res = GTResult.Ternary.UNDECIDED
      while(res == GTResult.Ternary.UNDECIDED)
        res =  test.check(rnd.nextBoolean())
      res == GTResult.Ternary.NO
    })
    assert(failures < 100, s"Expected failures to be at most 10, but it is $failures")
  }

  it should "not return too many wrong YES when the indifference region is not respected" in {
    val failures = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new SPRT.Ternary(0.50000001,0.01,0.01, 0.01,0.1)
      var res = GTResult.Ternary.UNDECIDED
      while(res == GTResult.Ternary.UNDECIDED)
        res =  test.check(rnd.nextBoolean())
      res == GTResult.Ternary.YES
    })
    assert(failures < 100, s"Expected failures to be at most 10, but it is $failures")
  }

}