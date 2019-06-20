package edu.stmc;

/** Generalized Sequential Probability Ratio Test */
public class GSPRT implements HypTest {

  public GSPRT(final double threshold, final double alpha, final double beta, final int minSamples) {
    assert 0 < threshold && threshold < 1 : "Wrong threshold " + threshold;
    assert 0 < alpha && alpha < 0.5 : "Wrong type I error " + alpha;
    assert 0 < beta && beta < 0.5 : "Wrong type II error " + beta;
    assert 0 < alpha && alpha < 0.5 : "Wrong type I error " + alpha;
    assert 0 < beta && beta < 0.5 : "Wrong type II error " + beta;
    this.threshold = threshold;
    this.alpha = alpha;
    this.beta = beta;
    logL = Math.log(beta / (1 - alpha));
    logU = Math.log((1 - beta) / alpha);
    assert logL < 0 : "Lower-bound log is not negative " + logL;
    assert logU > 0 : "Upper-bound log is not negative " + logU;
    logP0 = Math.log(threshold);
    logP1 = Math.log(1 - threshold);
    assert logP0 == logP0 : "logP0 is not a number " + logP0;
    assert logP1 == logP1 : "logP1 is not a number " + logP1;
    assert Double.MIN_VALUE <= logP0 && logP0 <= Double.MAX_VALUE : "|logP0| is too big" + Math.abs(logP0);
    assert Double.MIN_VALUE <= logP1 && logP1 <= Double.MAX_VALUE : "|logP1| is too big" + Math.abs(logP1);
    assert minSamples > 1 : "Invalid minimum number of samples " + minSamples;
    this.minSamples = minSamples;
  }

  public GTResult.Binary status() {
    if (N < STMCConfig.minIters)
      return GTResult.Binary.UNDECIDED;
    final double mu = n / (double) N;
    if (1 <= mu || mu <= 0)
      return GTResult.Binary.UNDECIDED;
    final double logMu0 = Math.log(mu);
    final double logMu1 = Math.log(1 - mu);
    assert logMu0 == logMu0 : "logMu0 is not a number " + logMu0;
    assert logMu1 == logMu1 : "logMu1 is not a number " + logMu1;
    assert Double.MIN_VALUE <= logMu0 && logMu0 <= Double.MAX_VALUE : "|logMu0| is too big" + Math.abs(logMu0);
    assert Double.MIN_VALUE <= logMu1 && logMu1 <= Double.MAX_VALUE : "|logMu1| is too big" + Math.abs(logMu1);
    final double logT = mu >= threshold ?
                        (n * logMu0 + (N - n) * logMu1) - (n * logP0 + (N - n) * logP1) :
                        (n * logP0 + (N - n) * logP1) - (n * logMu0 + (N - n) * logMu1);
    return logT >= logU ? GTResult.Binary.YES :
           logT <= logL ? GTResult.Binary.NO :
           GTResult.Binary.UNDECIDED;
  }

  @Override
  public void update(final boolean passed) {
    N++;
    if (passed) n++;
  }

  @Override
  public void update(final int passed, final int failed) {
    N += passed + failed;
    n += passed;
  }

  /** Reset the state (can be used to restart the test) */
  @Override
  public void reset() {
    N = 0;
    n = 0;
  }

  @Override
  public boolean shouldStopNow() { return status() != GTResult.Binary.UNDECIDED; }

  @Override
  public boolean hasAnswer() { return true; }

  @Override
  public boolean isNullRejected() { return status() == GTResult.Binary.YES; }

  @Override
  public String getParametersString() {
    return "threshold: " + threshold + ", " +
           "alpha: " + alpha + ", " +
           "beta: " + beta + ", " +
           "logL: " + logL + ", " +
           "logU: " + logU + ", " +
           "logP0: " + logP0 + ", " +
           "logP1: " + logP1 + ", " +
           "minSamples: " + minSamples;
  }

  @Override
  public GSPRT cloneCopy() {
    try {
      return (GSPRT) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }

  /** Input Threshold */
  public final double threshold;

  /** Type I Error Probability */
  public final double alpha;

  /** Type II Error Probability */
  public final double beta;

  /** Lower bound after which H0 won't be rejected */
  public final double logL;

  /** Upper bound after which H0 will be rejected */
  public final double logU;

  /** Step size for negative samples */
  public final double logP0;

  /** Step size for positive samples */
  public final double logP1;

  /** Minimum number of samples before making any decision */
  public final int minSamples;

  /** Total number of samples */
  private long N = 0;

  /** Number of positive samples */
  private long n = 0;
}
