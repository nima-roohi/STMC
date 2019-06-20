package edu.stmc;

/** Sequential Probability Ratio Test */
public class SPRT {
  private SPRT() { }

  /**
   * SPRT for binary simple hypothesis testing.
   * <ul>
   * <li><em>Binary</em> means if the algorithm terminates, there are two possibilities for its answer.
   * <li><em>Simple</em> means null and alternative hypotheses must be in the form of {@literal H0: p=p0} and
   * {@literal H1: p=p1}, respectively (for some values of {@code 0<p0<p1<1}). Note that {@literal H0: p<p0} is
   * not a simple hypothesis.</li>
   * </ul>
   */
  public static final class Binary {

    /**
     * Construct an instance of the binary version of SPRT
     * @param threshold Input threshold
     * @param alpha     Type I error probability (also known as <em>false positive</em> probability) is the probability
     *                  of incorrectly rejecting the null hypothesis.
     * @param beta      Type II error probability (also known as <em>false negative</em> probability) is the probability
     *                  of incorrectly not rejecting the null hypothesis.
     * @param delta     Half of the size of indifference region.
     * @requires {@code 0 < lb < threshold < ub < ub < 1}
     * @requires {@code 0 < alpha < 0.5}
     * @requires {@code 0 < beta < 0.5}
     */
    public Binary(final double threshold, final double alpha, final double beta, final double delta) {
      assert 0 < threshold && threshold < 1 : "Wrong threshold " + threshold;
      assert 0 < alpha && alpha < 0.5 : "Wrong type I error " + alpha;
      assert 0 < beta && beta < 0.5 : "Wrong type II error " + beta;
      assert delta < threshold && threshold < 1 - delta : "Invalid threshold (" + threshold + ") and/or delta (" + delta + ")";
      assert 0 < alpha && alpha < 0.5 : "Wrong type I error " + alpha;
      assert 0 < beta && beta < 0.5 : "Wrong type II error " + beta;
      final double lb = threshold - delta;
      final double ub = threshold + delta;
      this.threshold = threshold;
      this.alpha = alpha;
      this.beta = beta;
      this.delta = delta;
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

    /**
     * @return {@link GTResult.Binary#YES}, {@link GTResult.Binary#NO}, or {@link GTResult.Binary#UNDECIDED}.
     * <ul>
     * <li>{@link GTResult.Binary#YES} means that the test rejected the null hypothesis (the actual probability is not
     * smaller than the threshold in H0).</li>
     * <li>{@link GTResult.Binary#NO} means that the test did not reject the null hypothesis.</li>
     * <li>{@link GTResult.Binary#UNDECIDED} means that the test is not done yet.</li>
     * </ul>
     * @ensures Let {@literal p} be the actual probability. There are two two probabilistic guarantees:
     * <ul>
     * <li>If {@code p <= threshold - delta} then the probability of returning {@link GTResult.Binary#YES} is at most {@link #alpha}.</li>
     * <li>If {@code p >= threshold + delta} then the probability of returning {@link GTResult.Binary#NO} is at most {@link #beta}.</li>
     * </ul>
     */
    public GTResult.Binary status() {
      return logT <= logL ? GTResult.Binary.NO :
             logT >= logU ? GTResult.Binary.YES :
             GTResult.Binary.UNDECIDED;
    }

    /**
     * Update the test by adding an observation
     * @param passed Whether or not the sample satisfied the test or not.
     * @return {@link #status()}
     */
    public GTResult.Binary check(final boolean passed) {
      if (passed) logT += q1;
      else logT += q0;
      return status();
    }

    /**
     * Update the test by adding the input number of observations
     * @param passed Number of samples that are passed
     * @param failed Number of samples that are failed
     * @return {@link #status()}
     * @requires {@code passed >= 0}
     * @requires {@code failed >= 0}
     */
    public GTResult.Binary check(final int passed, final int failed) {
      assert passed >= 0 : "Invalid number of passed tests " + passed;
      assert failed >= 0 : "Invalid number of failed tests " + failed;
      logT += passed * q1 + failed * q0;
      return status();
    }

    /** Input Threshold */
    public final double threshold;

    /** Type I Error Probability */
    public final double alpha;

    /** Type II Error Probability */
    public final double beta;

    /** Half of the size of indifference region */
    public final double delta;

    /** Lower bound after which H0 won't be rejected */
    public final double logL;

    /** Upper bound after which H0 will be rejected */
    public final double logU;

    /** Step size for negative samples */
    public final double q0;

    /** Step size for positive samples */
    public final double q1;

    /** Test statistic */
    private double logT = 0;
  }

  /**
   * SPRT for ternary simple hypothesis testing.
   * <ul>
   * <li><em>Ternary</em> means if the algorithm terminates, there are three possibilities for its answer.
   * <li><em>Simple</em> means null and alternative hypotheses must be in the form of {@literal H0: p=p0} and
   * {@literal H1: p=p1}, respectively (for some values of {@code 0<p0<p1<1}). Note that {@literal H0: p<p0} is
   * not a simple hypothesis.</li>
   * </ul>
   */
  public static final class Ternary {

    /**
     * Construct an instance of the ternary version of SPRT
     * @param threshold Input threshold
     * @param alpha     Type I error probability (also known as <em>false positive</em> probability) is the probability
     *                  of incorrectly rejecting the null hypothesis.
     * @param beta      Type II error probability (also known as <em>false negative</em> probability) is the probability
     *                  of incorrectly not rejecting the null hypothesis.
     * @param gamma     The probability of returning {@literal unknown} when the actual probability is at least
     *                  {@link #delta} away from the input threshold.
     * @param delta     Half of the size of indifference region.
     * @requires {@code 0 < lb < threshold < ub < ub < 1}
     * @requires {@code 0 < alpha < 0.5}
     * @requires {@code 0 < beta < 0.5}
     * @requires {@code 0 < gamma < 0.5}
     */
    public Ternary(double threshold, double alpha, double beta, double gamma, double delta) {
      assert 0 < threshold && threshold < 1 : "Wrong threshold " + threshold;
      assert 0 < alpha && alpha < 0.5 : "Wrong type I error " + alpha;
      assert 0 < beta && beta < 0.5 : "Wrong type II error " + beta;
      assert delta < threshold && threshold < 1 - delta : "Invalid threshold (" + threshold + ") and/or delta (" + delta + ")";
      assert 0 < gamma && gamma < 0.5 : "Wrong type III error " + gamma;
      this.threshold = threshold;
      this.alpha = alpha;
      this.beta = beta;
      this.delta = delta;
      this.gamma = gamma;
      final double halfDelta = delta / 2;
      this.lbBinary = new Binary(threshold - halfDelta, alpha, gamma, halfDelta);
      this.ubBinary = new Binary(threshold + halfDelta, gamma, beta, halfDelta);
    }

    /**
     * @param lb Result of lower-bound binary test
     * @param ub Result of upper-bound binary test
     * @return {@link GTResult.Ternary#YES}, {@link GTResult.Ternary#NO}, {@link GTResult.Ternary#UNKNOWN}, or {@link GTResult.Ternary#UNDECIDED}.
     * <ul>
     * <li>{@link GTResult.Ternary#YES} means that the test rejected the null hypothesis (the actual probability is not
     * smaller than the threshold in H0).</li>
     * <li>{@link GTResult.Ternary#NO} means that the test did not reject the null hypothesis.</li>
     * <li>{@link GTResult.Ternary#UNKNOWN} means that the test is finished, but could not determine an answer.</li>
     * <li>{@link GTResult.Ternary#UNDECIDED} means that the test is not done yet.</li>
     * </ul>
     * @ensures Let {@literal p} be the actual probability. There are two two probabilistic guarantees:
     * <ul>
     * <li>If {@code p <= threshold - delta} then the probability of returning {@link GTResult.Ternary#YES} is at most {@link #alpha}.</li>
     * <li>If {@code p >= threshold + delta} then the probability of returning {@link GTResult.Ternary#NO} is at most {@link #beta}.</li>
     * <li>If {@code threshold - delta < p < threshold + delta} then the probability of returning {@link GTResult.Ternary#UNKNOWN} is at
     * most {@link #beta}.</li>
     * </ul>
     */
    public static GTResult.Ternary status(final GTResult.Binary lb, final GTResult.Binary ub) {
      return lb == GTResult.Binary.NO ? GTResult.Ternary.NO :
             ub == GTResult.Binary.YES ? GTResult.Ternary.YES :
             lb == GTResult.Binary.UNDECIDED || ub == GTResult.Binary.UNDECIDED ? GTResult.Ternary.UNDECIDED :
             GTResult.Ternary.UNKNOWN;
    }

    /** Same as {@link #status(GTResult.Binary, GTResult.Binary)}, but input parameters are taken from the current state
     * @return Current status of the test */
    public GTResult.Ternary status() { return status(lbBinary.status(), ubBinary.status()); }

    /**
     * Update the test by adding an observation
     * @param passed Whether or not the sample satisfied the test or not.
     * @return {@link #status()}
     */
    public GTResult.Ternary check(final boolean passed) {
      return status(lbBinary.check(passed), ubBinary.check(passed));
    }

    /**
     * Update the test by adding an observation
     * @param passed Number of samples that are passed
     * @param failed Number of samples that are failed
     * @return {@link #status()}
     */
    public GTResult.Ternary check(final int passed, final int failed) {
      return status(lbBinary.check(passed, failed), ubBinary.check(passed, failed));
    }

    /** Input Threshold */
    public final double threshold;

    /** Type I Error Probability */
    public final double alpha;

    /** Type II Error Probability */
    public final double beta;

    /** The probability of incorrectly returning {@link GTResult.Ternary#UNKNOWN} */
    public final double gamma;

    /** Half of the size of indifference region */
    public final double delta;

    /** Lower bound binary test */
    private final Binary lbBinary;

    /** Upper bound binary test */
    private final Binary ubBinary;
  }
}
