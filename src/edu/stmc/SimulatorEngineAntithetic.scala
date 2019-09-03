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

import util.control.Breaks._
import parser.State
import prism.{ModelType, PrismComponent, PrismException, PrismNotSupportedException, PrismUtils}
import simulator.{Choice, SimulatorEngine}

import scala.collection.{JavaConverters, mutable}

/** Simulator engine for antithetic sampling */
class SimulatorEngineAntithetic(parent: PrismComponent) extends SimulatorEngine(parent) {

  require(STMCConfig.enabled)
  require(STMCConfig.samplingMethod == NameSmplMethod.ANTITHETIC)

  @throws[PrismException]
  protected override def doSampling(initialState: State, maxPathLength: Long): Unit = {

    var iters = 0
    var i = 0L

    // Flags
    var stoppedEarly = false
    val deadlocksFound = false
    var allDone = false
    var allKnown = false
    var someUnknownButBounded = false
    val shouldStopSampling = false

    // Path stats
    var avgPathLength = 0.0
    var minPathFound = 0L
    var maxPathFound = 0L

    // Progress info
    var lastPercentageDone = 0
    var percentageDone = 0

    // Timing info
    var start = 0L
    var stop = 0L
    var time_taken = .0

    // Start
    start = System.currentTimeMillis
    mainLog.print("\nSampling progress: [")
    mainLog.flush()

    val propertySamplers = JavaConverters.asScalaBuffer(this.propertySamplers)

    antiPhase = true

    // Main sampling loop
    iters = 0
    breakable {
      while (!shouldStopSampling) {
        setAntiPhase(!antiPhase)

        // See if all properties are done; if so, stop sampling
        if (!antiPhase) {
          allDone = true
          for (sampler <- propertySamplers)
            if (!sampler.getSimulationMethod.shouldStopNow(iters, sampler))
              allDone = false
          if (allDone)
            break
        }

        // Display progress (of slowest property)
        percentageDone = 100
        for (sampler <- propertySamplers)
          percentageDone = Math.min(percentageDone, sampler.getSimulationMethod.getProgress(iters, sampler))
        if (percentageDone > lastPercentageDone) {
          lastPercentageDone = percentageDone
          mainLog.print(" " + lastPercentageDone + "%")
          mainLog.flush()
        }

        iters += 1

        // Start the new path for this iteration (sample)
        initialisePath(initialState)

        // Generate a path
        allKnown = false
        someUnknownButBounded = false
        i = 0
        breakable {
          while ((!allKnown && i < maxPathLength) || someUnknownButBounded) {
            // Check status of samplers
            allKnown = true
            someUnknownButBounded = false
            for (sampler <- propertySamplers) {
              if (!sampler.isCurrentValueKnown) {
                allKnown = false
                if (sampler.needsBoundedNumSteps())
                  someUnknownButBounded = true
              }
            }
            // Stop when all answers are known or we have reached max path length
            // (but don't stop yet if there are "bounded" samplers with unknown values)
            if ((allKnown || i >= maxPathLength) && !someUnknownButBounded)
              break
            // Make a random transition
            automaticTransition()
            i += 1
          }
        }

        // TODO: Detect deadlocks so we can report a warning

        // Update path length statistics
        avgPathLength = (avgPathLength * (iters - 1) + i) / iters
        minPathFound = if (iters == 1) i else Math.min(minPathFound, i)
        maxPathFound = if (iters == 1) i else Math.max(maxPathFound, i)

        // If not all samplers could produce values, this an error
        if (!allKnown) {
          stoppedEarly = true
          break
        }

        // Update state of samplers based on last path
        for (sampler <- propertySamplers)
          sampler.updateStats()

      }
    }

    // Print details
    if (!stoppedEarly) {
      if (!shouldStopSampling) mainLog.print(" 100% ]")
      mainLog.println()
      stop = System.currentTimeMillis
      time_taken = (stop - start) / 1000.0
      mainLog.print("\nSampling complete: ")
      mainLog.print(iters + " iterations in " + time_taken + " seconds (average " + PrismUtils.formatDouble(2, time_taken / iters) + ")\n")
      mainLog.print("Path length statistics: average " + PrismUtils.formatDouble(2, avgPathLength) + ", min " + minPathFound + ", max " + maxPathFound + "\n")
    }
    else mainLog.print(" ...\n\nSampling terminated early after " + iters + " iterations.\n")
    // Print a warning if deadlocks occurred at any point
    if (deadlocksFound) mainLog.printWarning("Deadlocks were found during simulation: self-loops were added.")
    // Print a warning if simulation was stopped by the user
    if (shouldStopSampling) mainLog.printWarning("Simulation was terminated before completion.")
    // write to feedback file with true to indicate that we have finished sampling
    // Write_Feedback(iteration_counter, numIters, true);
    if (stoppedEarly) throw new PrismException("One or more of the properties being sampled could not be checked on a sample. Consider increasing the maximum path length")
  }

  private[this] var antiPhase = false
  private[this] val ints = new mutable.Queue[Double]()
  private[this] val unis = new mutable.Queue[Double]()
  private[this] val exps = new mutable.Queue[Double]()

  private[this] def setAntiPhase(value: Boolean): Unit = {
    antiPhase = value
    if (!value) {
      ints.clear()
      unis.clear()
      exps.clear()
    }
  }

  private[this] def nextUni(): Double =
    if (antiPhase)
      if (unis.isEmpty) rng.randomUnifDouble()
      else 1 - unis.dequeue
    else {
      val rnd = rng.randomUnifDouble()
      unis += rnd
      rnd
    }

  private[this] def nextInt(bound: Int): Int =
    if (antiPhase)
      if (ints.isEmpty) rng.randomUnifInt(bound)
      else ((1 - ints.dequeue) * bound).toInt
    else {
      val rnd = rng.randomUnifDouble()
      ints += rnd
      (rnd * bound).toInt
    }

  private[this] def nextExp(rate: Double): Double =
    if (antiPhase)
      if (exps.isEmpty) rng.randomExpDouble(rate)
      else (-Math.log(1 - exps.dequeue)) / rate
    else {
      val rnd = rng.randomUnifDouble()
      exps += rnd
      (-Math.log(rnd)) / rate
    }


  @throws[PrismException]
  protected override def automaticTransition: Boolean = {
    var choice: Choice = null
    var numChoices = 0
    var i = 0
    var j = 0
    var d = 0.0
    var r = 0.0
    val transitions = getTransitionList
    // Check for deadlock; if so, stop and return false
    numChoices = transitions.getNumChoices
    if (numChoices == 0) return false

    modelType match {
    case ModelType.DTMC =>
      // Pick a random number to determine choice/transition
      d = nextUni()
      val ref = new transitions.Ref
      transitions.getChoiceIndexByProbabilitySum(d, ref)
      // Execute
      executeTransition(ref.i, ref.offset, -1)
    case ModelType.MDP  =>
      // Pick a random choice
      i = nextInt(numChoices)
      choice = transitions.getChoice(i)
      // Pick a random transition from this choice
      d = nextUni()
      j = choice.getIndexByProbabilitySum(d)
      executeTransition(i, j, -1)
    case ModelType.CTMC =>
      // Get sum of all rates
      r = transitions.getProbabilitySum
      d = nextUni() * r
      val ref = new transitions.Ref
      transitions.getChoiceIndexByProbabilitySum(d, ref)
      executeTimedTransition(ref.i, ref.offset, nextExp(r), -1)
    case _              =>
      throw new PrismNotSupportedException(modelType + " not supported")
    }
    true
  }

}
