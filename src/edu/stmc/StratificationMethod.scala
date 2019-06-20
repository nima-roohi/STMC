package edu.stmc

import parser.ast.{Expression, ExpressionProb, ExpressionProp}
import prism.PrismException
import simulator.method.SimulationMethod
import simulator.sampler.Sampler

class StratificationMethod(private[this] var expr: Expression,
                           private[this] val test: HypTest) extends SimulationMethod {

  /**
    * Get the (short) name of this method.
    */
  override def getName: String = "Stratification"

  /**
    * Get the (full) name of this method.
    */
  override def getFullName: String = getName

  /**
    * Reset the status of this method (but not values of any parameters that have
    * been set). Typically called if this instance is going to be re-used for
    * another run of approximate verification.
    */
  override def reset(): Unit = test.reset()

  /**
    * Compute the missing parameter (typically there are multiple parameters,
    * one of which can be left free) *before* simulation. This is not always
    * possible - sometimes the parameter cannot be determined until afterwards.
    * This method may get called multiple times.
    *
    * @throws PrismException if the parameters set so far are invalid.
    */
  override def computeMissingParameterBeforeSim(): Unit = Unit

  /**
    * Compute the missing parameter (typically there are multiple parameters,
    * one of which can be left free) *after* simulation. (Sometimes the parameter
    * cannot be determined before simulation.)
    * This method may get called multiple times.
    */
  override def computeMissingParameterAfterSim(): Unit = Unit

  /**
    * Get the missing (computed) parameter. If it has not already been computed and
    * this can be done *before* simulation, calling this method will trigger its computation.
    * If it can only be done *after*, an exception is thrown.
    *
    * @return the computed missing parameter (as an Integer or Double object)
    * @throws PrismException if missing parameter is not and cannot be computed yet
    *                        or if the parameters set so far are invalid.
    */
  override def getMissingParameter: AnyRef = None

  /**
    * Set the Expression (property) that simulation is going to be used to approximate.
    * It will be either an ExpressionProb or ExpressionReward object.
    * All constants should have already been defined and replaced.
    *
    * @throws PrismException if property is inappropriate somehow for this method.
    */
  override def setExpression(expr: Expression): Unit = {
    if (!expr.isInstanceOf[ExpressionProb])
      throw new PrismException("Can only handle expressions of type ExpressionProp. However, type of '" +
        expr.toString + "' is " + expr.getClass.getName)
    this.expr = expr
  }

  /**
    * Get the parameters of the simulation (including the computed one) as a string.
    */
  override def getParametersString: String = test.getParametersString

  /**
    * Determine whether or not simulation should stop now, based on the stopping
    * criteria of this method, and the current state of simulation (the number of
    * iterations so far and the corresponding Sampler object).
    * Note: This method may continue being called after 'true' is returned,
    * e.g. if multiple properties are being simulated simultaneously.
    *
    * @param iters   The number of iterations (samples) done so far
    * @param sampler The Sampler object for this simulation
    * @return true if the simulation should stop, false otherwise
    */
  override def shouldStopNow(iters: Int, sampler: Sampler): Boolean = {
    test.update(sampler.getCurrentValue.asInstanceOf[Boolean])
    test.shouldStopNow
  }

  /**
    * Get an indication of progress so far for simulation, i.e. an approximate value
    * for the percentage of work (samples) done. The value is a multiple of 10 in the range [0,100].
    * This estimate may not be linear (e.g. for CI/ACI where 'iterations' is computed).
    * It is assumed that this method is called *after* the call to shouldStopNow(...).
    * Note: The iteration count may exceed that dictated by this method,
    * e.g. if multiple properties are being simulated simultaneously.
    * TODO: check methods for this
    *
    * @param iters   The number of iterations (samples) done so far
    * @param sampler The Sampler object for this simulation
    */
  override def getProgress(iters: Int, sampler: Sampler): Int = 1000

  /**
    * Get the (approximate) result for the property that simulation is being used to approximate.
    * This should be a Boolean/Double for bounded/quantitative properties, respectively.
    *
    * @param sampler The Sampler object for this simulation
    * @throws PrismException if we can't get a result for some reason.
    */
  override def getResult(sampler: Sampler): AnyRef = Boolean.box(test.isNullRejected)

  /**
    * Get an explanation for the result of the simulation as a string.
    *
    * @param sampler The Sampler object for this simulation (e.g. to get mean)
    * @throws PrismException if we can't get a result for some reason.
    */
  override def getResultExplanation(sampler: Sampler): String = "No explanation"


  override def clone: SimulationMethod = {
    new StratificationMethod(expr,test.cloneCopy())
  }

}
