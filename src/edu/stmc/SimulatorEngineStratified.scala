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

import parser.State
import parser.ast.{Expression, ModulesFile, PropertiesFile}
import prism.{ModelType, PrismComponent, PrismException, PrismNotSupportedException, PrismUtils}
import simulator.method.SimulationMethod
import simulator.sampler.Sampler
import simulator.{PathOnTheFly, SimulatorEngine, TransitionList}

import util.control.Breaks._
import scala.collection.JavaConverters

final class SimulatorEngineStratified(parent: PrismComponent) extends SimulatorEngine(parent) {

  private[this] class Stuff {
    private[stmc] var path: PathOnTheFly = _
    private[stmc] var currentState: State = _
    private[stmc] var transitionList: TransitionList = _
    private[stmc] var transitionListBuilt: Boolean = false
    private[stmc] var transitionListState: State = _
    private[stmc] var samplers: List[Sampler] = Nil
  }

  private[this] val stuff = Array.ofDim[Stuff](STMCConfig.strataTotalSize)
  for (i <- stuff.indices)
    stuff(i) = new Stuff()

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  private[this] val counter = Array.ofDim[Int](STMCConfig.strataSizes.length)
  private[this] def resetCounter(): Unit =
    for (i <- counter.indices)
      counter(i) = 0
  private[this] def stepCounter(): Boolean = {
    var i = 0
    while (i < counter.length && counter(i) + 1 == STMCConfig.strataSizes(i)) {
      counter(i) = 0
      i += 1
    }
    if (i == counter.length) true
    else {
      counter(i) += 1
      false
    }
  }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  private[this] val offsets = Array.ofDim[Array[Int]](STMCConfig.strataSizes.length)
  private[this] var offsets2: Array[Array[Int]] = _
  for (i <- STMCConfig.strataSizes.indices)
    offsets(i) = (0 until STMCConfig.strataSizes(i)).toArray

  private[this] def shuffleOffsets(): Unit = shuffleOffsets(offsets)
  private[this] def shuffleOffsets2(): Unit = shuffleOffsets(offsets2)
  private[this] def shuffleOffsets(offsets: Array[Array[Int]]): Unit =
    if (offsets != null)
      for (arr <- offsets)
        for (i <- arr.indices) {
          val j = i + rng.randomUnifInt(arr.length - i)
          val tmp = arr(i)
          arr(i) = arr(j)
          arr(j) = tmp
        }

  private[this] def shuffleStuff(): Unit =
    for (i <- stuff.indices) {
      val j = i + rng.randomUnifInt(stuff.length - i)
      val tmp = stuff(i)
      stuff(i) = stuff(j)
      stuff(j) = tmp
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  // Antithetic is similar to stratified and I don't want to spend time one another source file
  private[this] var antitheticCell1: Double = _
  private[this] var antitheticCell2: Double = _
  private[this] var anti = false

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  @throws[PrismException]
  override def modelCheckMultipleProperties(modulesFile: ModulesFile,
                                            propertiesFile: PropertiesFile,
                                            exprs: java.util.List[Expression],
                                            initialState: State,
                                            maxPathLength: Long,
                                            simMethod: SimulationMethod): Array[AnyRef] = {
//    loadModulesFile(modulesFile)
//    if (modelType != ModelType.DTMC)
//      offsets2 = offsets.clone()
//    initialize(exprs, modulesFile)

    super.modelCheckMultipleProperties(modulesFile, propertiesFile, exprs, initialState, maxPathLength, simMethod)
  }

  @throws[PrismException]
  override protected def doSampling(initialState: State, maxPathLength: Long): Unit = {
    if (modelType != ModelType.DTMC)
      offsets2 = offsets.clone()
    initialize(properties, modulesFile)


    mainLog.print("\nSampling progress: [")
    mainLog.flush()
    val start = System.currentTimeMillis()

    val ssprt = scalaPropertySamplers.head.getSimulationMethod.isInstanceOf[HypTestSPRTStratified]
    val positives = Array.ofDim[Int](scalaPropertySamplers.size)
    var maxPathLengthError = false

    var avgPathLength = 0.0
    var minPathFound = 0L
    var maxPathFound = 0L

    var iters = 0
    var finished = false

    breakable {
      while (!finished) {
        finished = true
        iters += 1

        // initialize all paths
        for (i <- stuff.indices) {
          initialisePath(i)
          for (sampler <- stuff(i).samplers)
            sampler.reset()
        }

        // simultaneously sample all paths
        var needMore = true
        var len = 0L
        while (needMore) {
          len += counter.length
          needMore = false
          shuffleStuff()
//          shuffleOffsets()
//          shuffleOffsets2()
          maxPathLengthError = len > maxPathLength
          if (maxPathLengthError)
            break
          for (i <- stuff.indices) {
            for (step <- counter.indices) {
              automaticTransition(i, step)
              for (sampler <- stuff(i).samplers) {
                sampler.update(stuff(i).path, getTransitionList(i))
                needMore |= !sampler.isCurrentValueKnown
              }
            }
            stepCounter()
          }
        }

        // update path length statistics
        avgPathLength = (avgPathLength * (iters - 1) + len) / iters
        minPathFound = if (iters == 1) len else Math.min(minPathFound, len)
        maxPathFound = if (iters == 1) len else Math.max(maxPathFound, len)

        for (i <- positives.indices)
          positives(i) = 0

        // find how many positive samples we just took
        for (stf <- stuff) {
          var sid = 0
          for (sampler <- stf.samplers) {
            if (sampler.getCurrentValue.asInstanceOf[Boolean])
              positives(sid) += 1
            sid += 1
          }
        }

        for ((sampler, positive) <- scalaPropertySamplers.zip(positives)) {
          val smp = sampler.getSimulationMethod
          // // shouldStopNow will update itself when ssprt is false
          val adj1 = if (ssprt || !sampler.getCurrentValue.asInstanceOf[Boolean]) 0 else 1
          val adj2 = if (ssprt) 0 else 1
          smp.asInstanceOf[HypTest].update(positive - adj1, STMCConfig.strataTotalSize - positive - adj2)
          finished &= smp.shouldStopNow(iters, sampler)
        }
      }
    }

    // Print details
    val samples = iters * STMCConfig.strataTotalSize
    val stop = System.currentTimeMillis()
    val time_taken = (stop - start) / 1000.0
    Main.updateTotal(time_taken, samples)
    if (!maxPathLengthError) {
      mainLog.print(" 100% ]")
      mainLog.println()
      mainLog.print("\nSampling complete: ")
      mainLog.print(s"$iters iterations ($samples samples) in $time_taken seconds (average ${PrismUtils.formatDouble(2, time_taken / samples)})\n")
      mainLog.print(s"Path length statistics: average ${PrismUtils.formatDouble(2, avgPathLength)}, min $minPathFound, max $maxPathFound\n")
    } else
        mainLog.print(s" ...\n\nSampling terminated early after $iters iterations ($samples samples).\n")

    if (maxPathLengthError)
      throw new PrismException("One or more of the properties being sampled could not be checked on a sample. Consider increasing the maximum path length")

  }

  @throws[PrismException]
  private[this] def initialize(exprs: java.util.List[Expression], mf: ModulesFile): Unit = {
    for (i <- stuff.indices) {
      stuff(i).path = new PathOnTheFly(modulesFile)
      stuff(i).currentState = new State(numVars)
      stuff(i).transitionList = new TransitionList()
      var samplers: List[Sampler] = Nil
      for (expr <- JavaConverters.asScalaBuffer(exprs)) {
        val sampler = Sampler.createSampler(expr, mf)
        samplers = sampler :: samplers
      }
      stuff(i).samplers = samplers
    }
  }

  @throws[PrismException]
  private[this] def initialisePath(id: Int): Unit = {
    if (modulesFile.getInitialStates == null)
      stuff(id).currentState.copy(modulesFile.getDefaultInitialState())
    else
      throw new PrismException("Random choice of multiple initial states not yet supported")
    // Initialise stored path
    updater.calculateStateRewards(stuff(id).currentState, tmpStateRewards)
    stuff(id).path.initialise(stuff(id).currentState, tmpStateRewards)
    // Reset transition list
    stuff(id).transitionListBuilt = false
    stuff(id).transitionListState = null
    for(sampler <- stuff(id).samplers) {
      sampler.reset()
      sampler.update(stuff(id).path, getTransitionList(id))
    }
  }


  @throws[PrismException]
  private[this] def getTransitionList(id: Int): TransitionList = {
    // Compute the current transition list, if required
    if (!stuff(id).transitionListBuilt) {
      updater.calculateTransitions(stuff(id).currentState, stuff(id).transitionList)
      stuff(id).transitionListBuilt = true
    }
    stuff(id).transitionList
  }

  @throws[PrismException]
  private[this] def automaticTransition(id: Int, step: Int): Boolean =
    if (STMCConfig.samplingMethod != NameSmplMethod.ANTITHETIC) {
      val d1 = rng.randomUnifDouble() / STMCConfig.strataSizes(step) + offsets(step)(counter(step)) / STMCConfig.strataSizes(step).toDouble
      if (modelType == ModelType.DTMC) automaticTransition(id, d1, 0)
      else {
        val d2 = rng.randomUnifDouble() / STMCConfig.strataSizes(step) + offsets2(step)(counter(step)) / STMCConfig.strataSizes(step).toDouble
        automaticTransition(id, d1, d2)
      }
    }
    else {
      anti = !anti
      if (anti) {
        antitheticCell1 = rng.randomUnifDouble()
        antitheticCell2 = if (modelType == ModelType.DTMC) 0 else rng.randomUnifDouble()
        automaticTransition(id, antitheticCell1, antitheticCell2)
      }
      else
        automaticTransition(id, 1 - antitheticCell1, 1 - antitheticCell2)
    }


  @throws[PrismException]
  private[this] def automaticTransition(id: Int, d1: Double, d2: Double): Boolean = {
    val transitions = getTransitionList(id)
    // Check for deadlock; if so, stop and return false
    val numChoices = transitions.getNumChoices
    if (numChoices == 0)
      return false

    modelType match {
    case ModelType.DTMC =>
      // Pick a random number to determine choice/transition
      val ref = new transitions.Ref()
      transitions.getChoiceIndexByProbabilitySum(d1, ref)
      executeTransition(id, ref.i, ref.offset, -1)
    case ModelType.MDP  =>
      // Pick a random choice
      // i = rng.randomUnifInt(numChoices)
      val i = (numChoices * d1).toInt
      val choice = transitions.getChoice(i)
      // Pick a random transition from this choice
      val j = choice.getIndexByProbabilitySum(d2)
      // Execute
      executeTransition(id, i, j, -1)
    case ModelType.CTMC =>
      // Get sum of all rates
      val r = transitions.getProbabilitySum
      // Pick a random number to determine choice/transition
      val d = r * d1
      val ref = new transitions.Ref()
      transitions.getChoiceIndexByProbabilitySum(d, ref)
      // Execute
      executeTimedTransition(id, ref.i, ref.offset, (-Math.log(d2)) / r, -1)
    case _              =>
      throw new PrismNotSupportedException(s"$modelType not supported");
    }

    true
  }

  @throws[PrismException]
  private[this] def executeTransition(id: Int, i: Int, offset: Int, index: Int): Unit = {
    val transitions = getTransitionList(id)
    // Get corresponding choice and, if required (for full paths), calculate transition index
    val choice = transitions.getChoice(i)
    val actualIndex = if (onTheFly || index != 1) index else transitions.getTotalIndexOfTransition(i, offset)
    // Get probability for transition
    val p = choice.getProbability(offset)
    // Compute next state. Note use of path.getCurrentState() because currentState
    // will be overwritten during the call to computeTarget().
    choice.computeTarget(offset, stuff(id).path.getCurrentState, stuff(id).currentState)
    // Update path
    stuff(id).path.addStep(actualIndex, choice.getModuleOrActionIndex, p, tmpTransitionRewards, stuff(id).currentState, tmpStateRewards, transitions)
    // Reset transition list
    stuff(id).transitionListBuilt = false
    stuff(id).transitionListState = null
  }

  @throws[PrismException]
  private[this] def executeTimedTransition(id: Int, i: Int, offset: Int, time: Double, index: Int): Unit = {
    val transitions = getTransitionList(id)
    // Get corresponding choice and, if required (for full paths), calculate transition index
    val choice = transitions.getChoice(i)
    val actualIndex = if (onTheFly || index != 1) index else transitions.getTotalIndexOfTransition(i, offset)
    // Get probability for transition
    val p = choice.getProbability(offset)
    // Compute next state. Note use of path.getCurrentState() because currentState
    // will be overwritten during the call to computeTarget().
    choice.computeTarget(offset, stuff(id).path.getCurrentState, stuff(id).currentState)
    // Update path
    stuff(id).path.addStep(time, actualIndex, choice.getModuleOrActionIndex, p, tmpTransitionRewards, stuff(id).currentState, tmpStateRewards, transitions)
    // Reset transition list
    stuff(id).transitionListBuilt = false
    stuff(id).transitionListState = null
  }

}
