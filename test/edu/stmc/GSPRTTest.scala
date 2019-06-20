package edu.stmc

import java.util.concurrent.ThreadLocalRandom

import org.scalatest._


class GSPRTTest extends FlatSpec {

  "A binary GSPRT" should "decide coin flip correctly most of the time, when the right answer is YES" in {
    val passes = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new GSPRT(0.49, 0.01, 0.01, 10)
      var res = GTResult.Binary.UNDECIDED
      while (res == GTResult.Binary.UNDECIDED) {
        test.update(rnd.nextBoolean())
        res = test.status()
      }
      res == GTResult.Binary.YES
    })
    assert(passes > 900, s"Expected passes to be at least 990, but it is $passes")
  }

  it should "decide coin flip correctly most of the time, when the right answer is NO" in {
    val passes = (1 to 1000).par.count(_ => {
      val rnd = ThreadLocalRandom.current()
      val test = new GSPRT(0.51, 0.01, 0.01, 10)
      var res = GTResult.Binary.UNDECIDED
      while (res == GTResult.Binary.UNDECIDED) {
        test.update(rnd.nextBoolean())
        res = test.status()
      }
      res == GTResult.Binary.YES
    })
    assert(passes < 100, s"Expected passes to be at most 10, but it is $passes")
  }

}