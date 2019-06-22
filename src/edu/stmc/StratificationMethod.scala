package edu.stmc

import java.lang

import parser.ast.{Expression, ExpressionProb}
import prism.PrismException
import simulator.method.SimulationMethod
import simulator.sampler.Sampler

class StratificationMethod(private[this] var expr: ExpressionProb,
                           private[this] val test: HypTest) extends SimulationMethod {

  /** Get the (short) name of this method. */
  override def getName: String = "Stratification"

  /** Get the (full) name of this method. */
  override def getFullName: String = "Hypothesis Testing using Stratified Sampling"

  /** Reset the status of this method (but not values of any parameters that have
    * been set). Typically called if this instance is going to be re-used for
    * another run of approximate verification. */
  override def reset(): Unit = test.reset()

  /** Does Nothing. */
  override def computeMissingParameterBeforeSim(): Unit = Unit

  /** Does Nothing. */
  override def computeMissingParameterAfterSim(): Unit = Unit

  /** Returns [[None]] */
  override def getMissingParameter: AnyRef = None

  /** Returns 0 */
  override def getProgress(iters: Int, sampler: Sampler): Int = 0

  /** Set the Expression (property) that simulation is going to be used to approximate.
    * Input expression must be of type [[ExpressionProb]].
    * All constants should have already been defined and replaced.
    *
    * @throws PrismException if property is inappropriate somehow for this method. */
  override def setExpression(expr: Expression): Unit = {
    if (!expr.isInstanceOf[ExpressionProb])
      throw new PrismException(
        s"Can only handle expressions of type ExpressionProp. However, type of '$expr' is ${expr.getClass.getName}")
    this.expr = expr.asInstanceOf[ExpressionProb]
  }

  /** Get the parameters of the simulation (including the computed one) as a string. */
  override def getParametersString: String = test.parametersStr

  /** Determine whether or not simulation should stop now, based on the stopping
    * criteria of this method, and the current state of simulation (the number of
    * iterations so far and the corresponding Sampler object).
    * Note: This method may continue being called after 'true' is returned,
    * e.g. if multiple properties are being simulated simultaneously.
    *
    * @param iters   The number of iterations (samples) done so far
    * @param sampler The Sampler object for this simulation
    * @return true if the simulation should stop, false otherwise */
  override def shouldStopNow(iters: Int, sampler: Sampler): Boolean = {
    test.update(sampler.getCurrentValue.asInstanceOf[Boolean])
    test.completed
  }

  /** Get the (approximate) result for the property that simulation is being used to approximate.
    *
    * @param sampler The Sampler object for this simulation
    * @throws PrismException if we can't get a result for some reason. */
  override def getResult(sampler: Sampler): lang.Boolean =
    Boolean.box(if (expr.getRelOp.isLowerBound) test.rejected else !test.rejected)

  /** Get an explanation for the result of the simulation as a string.
    *
    * @param sampler The Sampler object for this simulation (e.g. to get mean) */
  override def getResultExplanation(sampler: Sampler): String =
    test.toString


  override def clone: SimulationMethod = new StratificationMethod(expr, test.copy())

}
