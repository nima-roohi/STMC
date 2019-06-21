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
  public static final class Binary implements HypTest {

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
     * @return {@link CompResult.Binary#LARGER}, {@link CompResult.Binary#SMALLER}, or {@link CompResult.Binary#UNDECIDED}.
     * <ul>
     * <li>{@link CompResult.Binary#LARGER} means that the test rejected the null hypothesis (the actual probability is not
     * smaller than the threshold in H0).</li>
     * <li>{@link CompResult.Binary#SMALLER} means that the test did not reject the null hypothesis.</li>
     * <li>{@link CompResult.Binary#UNDECIDED} means that the test is not done yet.</li>
     * </ul>
     * @ensures Let {@literal p} be the actual probability. There are two two probabilistic guarantees:
     * <ul>
     * <li>If {@code p <= threshold - delta} then the probability of returning {@link CompResult.Binary#LARGER} is at most {@link #alpha}.</li>
     * <li>If {@code p >= threshold + delta} then the probability of returning {@link CompResult.Binary#SMALLER} is at most {@link #beta}.</li>
     * </ul>
     */
    public CompResult.Binary status() {
      return logT <= logL ? CompResult.Binary.SMALLER :
             logT >= logU ? CompResult.Binary.LARGER :
             CompResult.Binary.UNDECIDED;
    }

    /**
     * Update the test by adding an observation
     * @param passed Whether or not the sample satisfied the test or not.
     */
    @Override
    public void update(final boolean passed) {
      if (passed) logT += q1;
      else logT += q0;
    }

    /**
     * Update the test by adding the input number of observations
     * @param passed Number of samples that are passed
     * @param failed Number of samples that are failed
     * @requires {@code passed >= 0}
     * @requires {@code failed >= 0}
     */
    @Override
    public void update(final int passed, final int failed) {
      assert passed >= 0 : "Invalid number of passed tests " + passed;
      assert failed >= 0 : "Invalid number of failed tests " + failed;
      logT += passed * q1 + failed * q0;
    }

    /** Reset the state (can be used to restart the test) */
    @Override
    public void reset() { logT = 0; }

    @Override
    public boolean completed() { return status() != CompResult.Binary.UNDECIDED; }

    @Override
    public boolean decided() { return true; }

    @Override
    public boolean nullHypRejected() { return status() == CompResult.Binary.LARGER; }

    @Override
    public String parametersStr() {
      return "threshold: " + threshold + ", " +
             "alpha: " + alpha + ", " +
             "beta: " + beta + ", " +
             "delta: " + delta +
             "logL: " + logL + ", " +
             "logU: " + logU + ", " +
             "q0: " + q0 + ", " +
             "q1: " + q1;
    }

    @Override
    public Binary cloneCopy() {
      try {
        return (Binary) super.clone();
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
  public static final class Ternary implements HypTest {

    /** Constructor used to cloning */
    private Ternary(Binary lbBinary, Binary ubBinary, double threshold, double alpha, double beta, double gamma, double delta) {
      this.lbBinary = lbBinary.cloneCopy();
      this.ubBinary = ubBinary.cloneCopy();
      this.threshold = threshold;
      this.alpha = alpha;
      this.beta = beta;
      this.gamma = gamma;
      this.delta = delta;
    }

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
     * @return {@link CompResult.Ternary#LARGER}, {@link CompResult.Ternary#SMALLER}, {@link CompResult.Ternary#TOO_CLOSE}, or {@link CompResult.Ternary#UNDECIDED}.
     * <ul>
     * <li>{@link CompResult.Ternary#LARGER} means that the test rejected the null hypothesis (the actual probability is not
     * smaller than the threshold in H0).</li>
     * <li>{@link CompResult.Ternary#SMALLER} means that the test did not reject the null hypothesis.</li>
     * <li>{@link CompResult.Ternary#TOO_CLOSE} means that the test is finished, but could not determine an answer.</li>
     * <li>{@link CompResult.Ternary#UNDECIDED} means that the test is not done yet.</li>
     * </ul>
     * @ensures Let {@literal p} be the actual probability. There are two two probabilistic guarantees:
     * <ul>
     * <li>If {@code p <= threshold - delta} then the probability of returning {@link CompResult.Ternary#LARGER} is at most {@link #alpha}.</li>
     * <li>If {@code p >= threshold + delta} then the probability of returning {@link CompResult.Ternary#SMALLER} is at most {@link #beta}.</li>
     * <li>If {@code threshold - delta < p < threshold + delta} then the probability of returning {@link CompResult.Ternary#TOO_CLOSE} is at
     * most {@link #beta}.</li>
     * </ul>
     */
    public static CompResult.Ternary status(final CompResult.Binary lb, final CompResult.Binary ub) {
      return lb == CompResult.Binary.SMALLER ? CompResult.Ternary.SMALLER :
             ub == CompResult.Binary.LARGER ? CompResult.Ternary.LARGER :
             lb == CompResult.Binary.UNDECIDED || ub == CompResult.Binary.UNDECIDED ? CompResult.Ternary.UNDECIDED :
             CompResult.Ternary.TOO_CLOSE;
    }

    /**
     * Same as {@link #status(CompResult.Binary, CompResult.Binary)}, but input parameters are taken from the current state
     * @return Current status of the test
     */
    public CompResult.Ternary status() { return status(lbBinary.status(), ubBinary.status()); }

    /**
     * Update the test by adding an observation
     * @param passed Whether or not the sample satisfied the test or not.
     */
    @Override
    public void update(final boolean passed) {
      lbBinary.update(passed);
      ubBinary.update(passed);
    }

    /**
     * Update the test by adding an observation
     * @param passed Number of samples that are passed
     * @param failed Number of samples that are failed
     */
    @Override
    public void update(final int passed, final int failed) {
      lbBinary.update(passed, failed);
      ubBinary.update(passed, failed);
    }

    /** Reset the state (can be used to restart the test) */
    @Override
    public void reset() {
      lbBinary.reset();
      ubBinary.reset();
    }

    @Override
    public boolean completed() { return status() != CompResult.Ternary.UNDECIDED; }

    @Override
    public boolean decided() { return status() != CompResult.Ternary.TOO_CLOSE; }

    @Override
    public boolean nullHypRejected() { return status() == CompResult.Ternary.LARGER; }

    @Override
    public String parametersStr() {
      return "threshold: " + threshold + ", " +
             "alpha: " + alpha + ", " +
             "beta: " + beta + ", " +
             "gamma: " + gamma + ", " +
             "delta: " + delta + ", " +
             "lbBinary: " + lbBinary.parametersStr() + ", " +
             "ubBinary: " + ubBinary.parametersStr();
    }

    @Override
    public Ternary cloneCopy() { return new Ternary(lbBinary, ubBinary, threshold, alpha, beta, gamma, delta); }

    /** Input Threshold */
    public final double threshold;

    /** Type I Error Probability */
    public final double alpha;

    /** Type II Error Probability */
    public final double beta;

    /** The probability of incorrectly returning {@link CompResult.Ternary#TOO_CLOSE} */
    public final double gamma;

    /** Half of the size of indifference region */
    public final double delta;

    /** Lower bound binary test */
    private final Binary lbBinary;

    /** Upper bound binary test */
    private final Binary ubBinary;
  }
}
