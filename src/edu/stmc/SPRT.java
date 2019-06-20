package edu.stmc;

public class SPRT {

  static final class Binary {
    Binary(final double threshold, final double lb, final double ub, final double alpha, final double beta) {
      assert 0 < lb : "Invalid lower-bound " + lb;
      assert ub < 1 : "Invalid upper-bound " + ub;
      assert lb < threshold : "Lower-bound " + lb + " must be smaller than " + threshold;
      assert threshold < ub : "Upper-bound " + ub + " must be larger than " + threshold;
      assert 0 < alpha && alpha < 0.5 : "Wrong type I error " + alpha;
      assert 0 < beta && beta < 0.5 : "Wrong type II error " + beta;
      this.threshold = threshold;
      this.lb = lb;
      this.ub = ub;
      this.alpha = alpha;
      this.beta = beta;
      logL = Math.log(beta / (1 - alpha));
      logU = Math.log((1 - beta) / alpha);
      assert logL < 0 : "Lower-bound log is not negative " + logL;
      assert logU > 0 : "Upper-bound log is not negative " + logU;
      q0 = Math.log((1 - ub) / (1 - lb));
      q1 = Math.log(ub / lb);
      assert q0 == q0 : "q0 is not a number " + q1;
      assert q1 == q1 : "q1 is not a number " + q1;
      assert Double.MIN_VALUE <= q0 && q0 <= Double.MAX_VALUE : "|q0| is too big" + Math.abs(q0);
      assert Double.MIN_VALUE <= q1 && q1 <= Double.MAX_VALUE : "|q1| is too big" + Math.abs(q1);
      assert logL + q0 > logL : "logL (" + logL + ") is too much smaller than q0 (" + q0 + ")";
      assert logU - q1 < logU : "logU (" + logU + ") is too much bigger than q1 (" + q1 + ")";
    }

    GTResult.Binary status() {
      return logT <= logL ? GTResult.Binary.NO :
             logT >= logU ? GTResult.Binary.YES :
             GTResult.Binary.UNDECIDED;
    }

    GTResult.Binary check(final boolean passed) {
      if (passed) logT += q1;
      else logT += q0;
      return status();
    }

    GTResult.Binary check(final int passed, final int failed) {
      assert passed >= 0 : "Invalid number of passed tests " + passed;
      assert failed >= 0 : "Invalid number of failed tests " + failed;
      logT += passed * q1 + failed * q0;
      return status();
    }

    final double threshold;
    final double lb;
    final double ub;
    final double alpha;
    final double beta;
    final double logL;
    final double logU;
    final double q0;
    final double q1;
    double logT = 0;
  }

  static final class Ternary {
    Ternary(double threshold, double lb, double ub, double alpha, double beta, double gamma) {
      assert 0 < lb : "Invalid lower-bound " + lb;
      assert ub < 1 : "Invalid upper-bound " + ub;
      assert lb < threshold : "Lower-bound " + lb + " must be smaller than " + threshold;
      assert threshold < ub : "Upper-bound " + ub + " must be larger than " + threshold;
      assert 0 < alpha && alpha < 0.5 : "Wrong type I error " + alpha;
      assert 0 < beta && beta < 0.5 : "Wrong type II error " + beta;
      assert 0 < gamma && gamma < 0.5 : "Wrong type III error " + gamma;
      this.threshold = threshold;
      this.lb = lb;
      this.ub = ub;
      this.alpha = alpha;
      this.beta = beta;
      this.gamma = gamma;
      this.lbBinary = new Binary((lb + threshold) / 2, lb, threshold, alpha, gamma);
      this.ubBinary = new Binary((ub + threshold) / 2, threshold, ub, gamma, beta);
    }

    GTResult.Ternary status(final GTResult.Binary lb, final GTResult.Binary ub) {
      return lb == GTResult.Binary.NO ? GTResult.Ternary.NO :
             ub == GTResult.Binary.YES ? GTResult.Ternary.YES :
             lb == GTResult.Binary.UNDECIDED || ub == GTResult.Binary.UNDECIDED ? GTResult.Ternary.UNDECIDED :
             GTResult.Ternary.UNKNOWN;
    }

    GTResult.Ternary status() { return status(lbBinary.status(), ubBinary.status()); }

    GTResult.Ternary check(final boolean passed) {
      return status(lbBinary.check(passed), ubBinary.check(passed));
    }

    GTResult.Ternary check(final int passed, final int failed) {
      return status(lbBinary.check(passed, failed), ubBinary.check(passed, failed));
    }

    final double threshold;
    final double lb;
    final double ub;
    final double alpha;
    final double beta;
    final double gamma;
    final Binary lbBinary;
    final Binary ubBinary;
  }
}
