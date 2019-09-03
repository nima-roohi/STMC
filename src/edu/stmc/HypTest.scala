/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 + STMC - Statistical Model Checker                                                               +
 +                                                                                                +
 + Copyright (C) 2019                                                                             +
 + Authors:                                                                                       +
 +   Nima Roohi <nroohi@ucsd.edu> (University of California San Diego)                            +
 +                                                                                                +
 + This program is free software: you can redistribute it and/or modify it under the terms        +
 + of the GNU General Public License as published by the Free Software Foundation, either         +
 + version 3 of the License, or (at your option) any later version.                               +
 +                                                                                                +
 + This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;      +
 + without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.      +
 + See the GNU General Public License for more details.                                           +
 +                                                                                                +
 + You should have received a copy of the GNU General Public License along with this program.     +
 + If not, see <https://www.gnu.org/licenses/>.                                                   +
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package edu.stmc

import parser.ast.Expression
import prism.PrismException
import simulator.method.SimulationMethod
import simulator.sampler.Sampler

/** Same as [[SimulationMethod]] from PRISM, with a few default implementations and a few more functions that help testing the class without using
  * [[Sampler]] or [[Expression]].
  *
  * @note Almost every method in [[SimulationMethod]] is declared here as well (even if it is not given a default implementation). So `ScalaDoc` will
  *       inherit the documentation in children of this class. */
abstract class HypTest extends SimulationMethod {

  /** Get the (short) name of this method. */
  override def getName: String

  /** Get the (full) name of this method. */
  override def getFullName: String

  /** Reset the status of this method (but not values of any parameters that have been set). Typically called if this instance is going to be re-used for
    * another run of approximate verification. */
  override def reset(): Unit

  /** Set the Expression (property) that simulation is going to be used to approximate. It will be either an [[parser.ast.ExpressionProb]] or
    * [[parser.ast.ExpressionReward]] object.
    * All constants should have already been defined and replaced.
    *
    * @throws PrismException if property is inappropriate somehow for this method. */
  @throws[PrismException]
  def setExpression(expr: Expression): Unit

  /** Compute the missing parameter (typically there are multiple parameters, one of which can be left free) ''before'' simulation. This is not always
    * possible - sometimes the parameter cannot be determined until afterwards. This method may get called multiple times.
    *
    * @throws PrismException if the parameters set so far are invalid.
    * @note Default implementation is added in STMC and it does nothing. */
  @throws[PrismException]
  override def computeMissingParameterBeforeSim(): Unit = Unit

  /** Compute the missing parameter (typically there are multiple parameters, one of which can be left free) ''after'' simulation (sometimes the parameter
    * cannot be determined before simulation). This method may get called multiple times.
    *
    * @note Default implementation is added in STMC and it does nothing. */
  override def computeMissingParameterAfterSim(): Unit = Unit

  /** Get the missing (computed) parameter. If it has not already been computed and this can be done ''before'' simulation, calling this method will trigger
    * its computation. If it can only be done ''after'', an exception is thrown.
    *
    * @return the computed missing parameter (as an Integer or Double object)
    * @throws PrismException if missing parameter is not and cannot be computed yet or if the parameters set so far are invalid. */
  @throws[PrismException]
  override def getMissingParameter: AnyRef

  /** Get the parameters of the simulation (including the computed one) as a string. */
  override def getParametersString: String

  /** Determine whether or not simulation should stop now, based on the stopping criteria of this method, and the current state of simulation (the number of
    * iterations so far and the corresponding [[Sampler]] object).
    *
    * @param iters   The number of iterations (samples) done so far
    * @param sampler The [[Sampler]] object for this simulation
    * @return `true` if the simulation should stop, `false` otherwise
    * @note This method may continue being called after `true` is returned, e.g. if multiple properties are being simulated simultaneously. */
  override def shouldStopNow(iters: Int, sampler: Sampler): Boolean

  /** Get an indication of progress so far for simulation, i.e. an approximate value for the percentage of work (samples) done. The value is a multiple of 10
    * in the range [0,100]. This estimate may not be linear (e.g. for CI/ACI where 'iterations' is computed). It is assumed that this method is called ''after''
    * the call to isCompleted(...).
    *
    * @param iters   The number of iterations (samples) done so far
    * @param sampler The [[Sampler]] object for this simulation
    * @note
    *   1. The iteration count may exceed that dictated by this method, e.g. if multiple properties are being simulated simultaneously.
    *   1. Default implementation is added in STMC and it returns 0. */
  override def getProgress(iters: Int, sampler: Sampler): Int = 0

  /** Get the (approximate) result for the property that simulation is being used to approximate. This should be a Boolean/Double for bounded/quantitative
    * properties, respectively.
    *
    * @param sampler The Sampler object for this simulation
    * @throws PrismException if we can't get a result for some reason. */
  @throws[PrismException]
  override def getResult(sampler: Sampler): AnyRef = Boolean.box(failed_to_reject)

  /** Get an explanation for the result of the simulation as a string.
    *
    * @param sampler The [[Sampler]] object for this simulation (e.g. to get mean)
    * @throws PrismException if we can't get a result for some reason. */
  @throws[PrismException]
  override def getResultExplanation(sampler: Sampler): String

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  /** Update the test by adding an observation.
    *
    * @param positive Whether or not the new sample is positive.
    * @note Implementations might add a constraint on the total number of samples. */
  def update(positive: Boolean)

  /** Update the test by adding multiple observations.
    *
    * @param positive Number of samples that are positive.
    * @param negative Number of samples that are negative.
    * @note
    *   1. Requires `positive >= 0` and `negative >= 0`.
    *   1. Implementations might add a constraint on the total number of samples. */
  def update(positive: Int, negative: Int)

  /** Whether or not the test is completed. */
  def completed: Boolean

  /** Whether or not the actual probability and the input threshold are too close to make a decision (sometimes, it is possible that a test terminates without
    * choosing between rejection of the null hypothesis and failure to do so).
    *
    * @note Requires [[completed]] to be `true`.
    * @see [[rejected]] and [[failed_to_reject]] */
  def too_close: Boolean

  /** Whether the test rejects the null hypothesis in favor of the alternative hypothesis.
    *
    * @note Requires [[completed]] to be `true`. */
  def rejected: Boolean

  /** Whether the test is failed to reject the null hypothesis in favor of the alternative hypothesis.
    *
    * @note Requires [[completed]] to be `true`. */
  def failed_to_reject: Boolean

}
