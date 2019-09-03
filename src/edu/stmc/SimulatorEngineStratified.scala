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

import java.io.File

import util.control.Breaks._
import java.util
import java.util.{Collections, List}
import java.util.concurrent.ThreadLocalRandom

import parser.State
import parser.`type`.Type
import parser.ast.{Expression, ModulesFile, PropertiesFile}
import prism.{ModelType, PrismComponent, PrismException, PrismFileLog, PrismLangException, PrismLog, PrismNotSupportedException, PrismUtils, ResultsCollection, UndefinedConstants}
import simulator.{Choice, Path, PathFull, PathFullInfo, PathFullPrefix, PathOnTheFly, SimulatorEngine, TransitionList, Updater}
import simulator.method.SimulationMethod
import simulator.sampler.Sampler
import strat.Strategy

import scala.collection.{JavaConverters, mutable}

/** Simulator engine for stratified sampling */
class SimulatorEngineStratified(parent: PrismComponent) extends SimulatorEngine(parent) {

  require(STMCConfig.enabled)
  require(STMCConfig.samplingMethod == NameSmplMethod.STRATIFIED)

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  private[this] var propertySamplers: mutable.Buffer[Sampler] = _

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
    if (offsets != null) {
      val rnd = ThreadLocalRandom.current()
      for (arr <- offsets)
        for (i <- arr.indices) {
          val j = rnd.nextInt(i, arr.length)
          val tmp = arr(i)
          arr(i) = arr(j)
          arr(j) = tmp
        }
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  @throws[PrismException]
  override def modelCheckMultipleProperties(modulesFile: ModulesFile,
                                            propertiesFile: PropertiesFile,
                                            exprs: util.List[Expression],
                                            initialState: State,
                                            maxPathLength: Long,
                                            simMethod: SimulationMethod): Array[AnyRef] = {
    if (modulesFile.getModelType == ModelType.DTMC)
      offsets2 = null
    else
      offsets2 = offsets.clone()
    return super.modelCheckMultipleProperties(modulesFile, propertiesFile, exprs, initialState, maxPathLength, simMethod)
  }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------

  private[this] class StratifiedPath {
    var path: PathOnTheFly = _
    var currentState: State = _
    var transitionList: TransitionList = _
    var transitionListBuilt: Boolean = _
    var transitionListState: State = _
    var updater: Updater = _
  }
  private[this] val strataSize = STMCConfig.strataSizes.product
  private[this] val stratifiedPaths = (1 to strataSize).map(_ => new StratifiedPath).toArray
  private[this] var idx: Int = _
  private[this] var idx_step:Int = _


  @throws[PrismException]
  override protected def doSampling(initialState: State, maxPathLength: Long): Unit = {
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
    start = System.currentTimeMillis()
    mainLog.print("\nSampling progress: [")
    mainLog.flush()

    // Main sampling loop
    iters = 0
    breakable {
      while (!shouldStopSampling) {

        // See if all properties are done; if so, stop sampling
        val allDone = propertySamplers.exists(sampler => sampler.getSimulationMethod.shouldStopNow(iters, sampler))
        if (allDone)
          break

        // Display progress (of slowest property)
        percentageDone = 100
        for (sampler <- propertySamplers)
          percentageDone = Math.min(percentageDone, sampler.getSimulationMethod.getProgress(iters, sampler))
        if (percentageDone > lastPercentageDone) {
          lastPercentageDone = percentageDone
          mainLog.print(" " + lastPercentageDone + "%")
          mainLog.flush()
        }

        iters += STMCConfig.strataSizes.length

        idx = 0
        while (idx < strataSize) {
          initialisePath(initialState)
          idx += 1
        }

        // Generate a path
        allKnown = false
        someUnknownButBounded = false
        i = 0

        breakable {
          while ((!allKnown && i < maxPathLength) || someUnknownButBounded) {
            // Check status of samplers
            val unknowns = propertySamplers.filterNot(_.isCurrentValueKnown)
            allKnown = unknowns.isEmpty
            someUnknownButBounded = unknowns.exists(_.needsBoundedNumSteps)

            // Stop when all answers are known or we have reached max path length
            // (but don't stop yet if there are "bounded" samplers with unkown values)
            if ((allKnown || i >= maxPathLength) && !someUnknownButBounded)
              break
            shuffleOffsets()
            shuffleOffsets2()
            idx = 0
            while (idx != strataSize) {
              idx_step = 0
              while(idx_step < counter.length) {
                automaticTransition()
                idx_step += 1
              }
              stepCounter()
              idx += 1
            }
            // Make a random transition
            i += counter.length
          }
        }

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

    if (stoppedEarly)
      throw new PrismException("One or more of the properties being sampled could not be checked on a sample. Consider increasing the maximum path length")

  }

  @throws[PrismException]
  override def automaticTransition(): Boolean = {
    var choice: Choice = null
    var numChoices = 0
    var i = 0
    var j = 0
    var d = 0.0
    var r = 0.0
    val transitions = getTransitionList

    // Check for deadlock; if so, stop and return false
    numChoices = transitions.getNumChoices()
    if (numChoices == 0)
      return false

    modelType match {
    case ModelType.DTMC =>
      // Pick a random number to determine choice/transition
      d = rng.randomUnifDouble() / STMCConfig.strataSizes(idx_step) + offsets(idx)(counter(idx_step)) / STMCConfig.strataSizes(idx_step).toDouble
      val ref = new transitions.Ref()
      transitions.getChoiceIndexByProbabilitySum(d, ref)
      // Execute
      executeTransition(ref.i, ref.offset, -1)
    case ModelType.MDP  =>
      // Pick a random choice
      // i = rng.randomUnifInt(numChoices)
      i = (numChoices * (rng.randomUnifDouble() / STMCConfig.strataSizes(idx_step) + offsets2(idx)(counter(idx_step)) / STMCConfig.strataSizes(idx_step).toDouble)).toInt
      choice = transitions.getChoice(i)
      // Pick a random transition from this choice
      d = rng.randomUnifDouble() / STMCConfig.strataSizes(idx_step) + offsets(idx)(counter(idx_step)) / STMCConfig.strataSizes(idx_step).toDouble
      j = choice.getIndexByProbabilitySum(d)
      // Execute
      executeTransition(i, j, -1)
    case ModelType.CTMC =>
      // Get sum of all rates
      r = transitions.getProbabilitySum()
      // Pick a random number to determine choice/transition
      d = r * (rng.randomUnifDouble() / STMCConfig.strataSizes(idx_step) + offsets(idx)(counter(idx_step)) / STMCConfig.strataSizes(idx_step).toDouble)
      val ref = new transitions.Ref()
      transitions.getChoiceIndexByProbabilitySum(d, ref)
      // Execute
      val rnd = rng.randomUnifDouble() / STMCConfig.strataSizes(idx_step) + offsets(idx)(counter(idx_step)) / STMCConfig.strataSizes(idx_step).toDouble
      executeTimedTransition(ref.i, ref.offset, /*rng.randomExpDouble(r)*/(-Math.log(rnd)) / r, -1)
    case _              =>
      throw new PrismNotSupportedException(modelType + " not supported");
    }

    true
  }


  //------------------------------------------------------------------------------------------------------------------------------------------------------------
  //------------------------------------------------------------------------------------------------------------------------------------------------------------
  //------------------------------------------------------------------------------------------------------------------------------------------------------------


  @throws[PrismException]
  override def checkModelForSimulation(modulesFile: ModulesFile): Unit = super.checkModelForSimulation(modulesFile)

  @throws[PrismException]
  override def createNewPath(modulesFile: ModulesFile): Unit = throw new UnsupportedOperationException

  @throws[PrismException]
  override def createNewOnTheFlyPath(modulesFile: ModulesFile) {
    // Store model
    loadModulesFile(modulesFile)
    // Create empty (on-the-fly_ path object associated with this model
    for (i <- 0 until strataSize)
      stratifiedPaths(i).path = new PathOnTheFly(modulesFile)
    onTheFly = true
  }

  @throws[PrismException]
  override def initialisePath(initialState: State) {
    // Store passed in state as current state
    if (initialState != null)
      stratifiedPaths(idx).currentState.copy(initialState)
    // Or pick default/random one
    else if (modulesFile.getInitialStates() == null)
           stratifiedPaths(idx).currentState.copy(modulesFile.getDefaultInitialState())
    else
      throw new PrismNotSupportedException("Random choice of multiple initial states not yet supported")

    stratifiedPaths(idx).updater.calculateStateRewards(stratifiedPaths(idx).currentState, tmpStateRewards)
    // Initialise stored path
    stratifiedPaths(idx).path.initialise(stratifiedPaths(idx).currentState, tmpStateRewards)
    // Reset transition list
    stratifiedPaths(idx).transitionListBuilt = false
    stratifiedPaths(idx).transitionListState = null
    // Reset and then update samplers for any loaded properties
    resetSamplers()
    updateSamplers()
    // Initialise the strategy (if loaded)
    initialiseStrategy()
  }

  @throws[PrismException]
  override def manualTransition(index: Int): Unit = super.manualTransition(index)

  @throws[PrismException]
  override def manualTransition(index: Int, time: Double): Unit = super.manualTransition(index, time)

  @throws[PrismException]
  override def automaticTransitions(n: Int, stopOnLoops: Boolean): Int = super.automaticTransitions(n, stopOnLoops)

  @throws[PrismException]
  override def automaticTransitions(time: Double, stopOnLoops: Boolean): Int = super.automaticTransitions(time, stopOnLoops)

  @throws[PrismException]
  override def backtrackTo(step: Int): Unit = {
    // Check step is valid
    if (step < 0)
      throw new PrismException("Cannot backtrack to a negative step index")
    if (step > stratifiedPaths(idx).path.size())
      throw new PrismException("There is no step " + step + " to backtrack to")
    // Back track in path
    stratifiedPaths(idx).path.asInstanceOf[PathFull].backtrack(step)
    // Update current state
    stratifiedPaths(idx).currentState.copy(stratifiedPaths(idx).path.getCurrentState())
    // Reset transition list
    stratifiedPaths(idx).transitionListBuilt = false
    stratifiedPaths(idx).transitionListState = null
    // Recompute samplers for any loaded properties
    recomputeSamplers()
  }

  @throws[PrismException]
  override def backtrackTo(time: Double): Unit = {
    // Check step is valid
    if (time < 0)
      throw new PrismException("Cannot backtrack to a negative time point")
    if (time > stratifiedPaths(idx).path.getTotalTime())
      throw new PrismException("There is no time point " + time + " to backtrack to")
    val pathFull = stratifiedPaths(idx).path.asInstanceOf[PathFull]
    // Get length (non-on-the-fly paths will never exceed length Integer.MAX_VALUE)
    val nLong = stratifiedPaths(idx).path.size()
    if (nLong > Integer.MAX_VALUE)
      throw new PrismException("PathFull cannot deal with paths over length " + Integer.MAX_VALUE)
    val n = nLong.toInt
    // Find the index of the step we are in at that point
    // i.e. the first state whose cumulative time on entering exceeds 'time'
    var step = 0
    while (step <= n && pathFull.getCumulativeTime(step) < time)
      step += 1
    // Then backtrack to this step
    backtrackTo(step)
  }

  @throws[PrismException]
  override def removePrecedingStates(step: Int): Unit = {
    // Check step is valid
    if (step < 0)
      throw new PrismException("Cannot remove states before a negative step index")
    if (step > stratifiedPaths(idx).path.size())
      throw new PrismException("There is no step " + step + " in the path")
    // Modify path
    stratifiedPaths(idx).path.asInstanceOf[PathFull].removePrecedingStates(step)
    // (No need to update currentState or re-generate transitions)
    // Recompute samplers for any loaded properties
    recomputeSamplers()
  }

  @throws[PrismException]
  override def computeTransitionsForStep(step: Int): Unit =
    computeTransitionsForState(stratifiedPaths(idx).path.asInstanceOf[PathFull].getState(step))

  /**
    * Re-compute the transition table for the current state.
    */
  @throws[PrismException]
  override def computeTransitionsForCurrentState(): Unit = {
    computeTransitionsForState(stratifiedPaths(idx).path.getCurrentState())
  }

  /**
    * Re-compute the transition table for a particular state.
    */
  @throws[PrismException]
  private[this] def computeTransitionsForState(state: State): Unit = {
    stratifiedPaths(idx).updater.calculateTransitions(state, stratifiedPaths(idx).transitionList)
    stratifiedPaths(idx).transitionListBuilt = true
    stratifiedPaths(idx).transitionListState = state
  }

  // ------------------------------------------------------------------------------
  // Methods for loading objects from model checking: paths, strategies, etc.
  // ------------------------------------------------------------------------------

  @throws[PrismException]
  override def loadReachableStates(reachableStates: java.util.List[State]): Unit = this.reachableStates = reachableStates

  @throws[PrismException]
  override def loadStrategy(strategy: Strategy) = this.strategy = strategy

  @throws[PrismException]
  override def loadPath(modulesFile: ModulesFile, newPath: PathFullInfo): Unit = super.loadPath(modulesFile, newPath)

  // ------------------------------------------------------------------------------
  // Methods for adding/querying labels and properties
  // ------------------------------------------------------------------------------

  @throws[PrismException]
  override def addLabel(label: Expression): Int = addLabel(label, null)

  @throws[PrismException]
  override def addLabel(label: Expression, pf: PropertiesFile): Int = super.addLabel(label, pf)

  @throws[PrismException]
  override def addProperty(prop: Expression): Int = addProperty(prop, null)

  @throws[PrismException]
  override def addProperty(prop: Expression, pf: PropertiesFile): Int = super.addProperty(prop, pf)

  @throws[PrismException]
  override def queryLabel(index: Int): Boolean = labels.get(index).evaluateBoolean(stratifiedPaths(idx).path.getCurrentState())

  @throws[PrismException]
  override def queryLabel(index: Int, step: Int): Boolean =
    labels.get(index).evaluateBoolean(stratifiedPaths(idx).path.asInstanceOf[PathFull].getState(step))


  @throws[PrismException]
  override def queryIsInitial(): Boolean =
  // Currently init...endinit is not supported so this is easy to check
    stratifiedPaths(idx).path.getCurrentState().equals(modulesFile.getDefaultInitialState());

  @throws[PrismException]
  override def queryIsInitial(step: Int): Boolean =
  // Currently init...endinit is not supported so this is easy to check
    stratifiedPaths(idx).path.asInstanceOf[PathFull].getState(step).equals(modulesFile.getDefaultInitialState());

  @throws[PrismException]
  override def queryIsDeadlock(): Boolean = getTransitionList().isDeadlock()

  @throws[PrismException]
  override def queryIsDeadlock(step: Int): Boolean =
  // By definition, earlier states in the path cannot be deadlocks
    if (step == stratifiedPaths(idx).path.size()) getTransitionList().isDeadlock() else false

  @throws[PrismException]
  override def queryProperty(index: Int): AnyRef = {
    if (index < 0 || index >= propertySamplers.size) {
      mainLog.printWarning("Can't query property " + index + ".")
      return null
    }
    val sampler = propertySamplers(index)
    if (sampler.isCurrentValueKnown) sampler.getCurrentValue else null
  }

  // ------------------------------------------------------------------------------
  // Private methods for path creation and modification
  // ------------------------------------------------------------------------------

  @throws[PrismException]
  private[this] def loadModulesFile(modulesFile: ModulesFile): Unit = {
    // Store model, some info and constants
    this.modulesFile = modulesFile
    modelType = modulesFile.getModelType()
    this.mfConstants = modulesFile.getConstantValues()

    // Check model is simulate-able
    checkModelForSimulation(modulesFile)

    // Get variable list (symbol table) for model
    varList = modulesFile.createVarList()
    numVars = varList.getNumVars()

    // Evaluate constants and optimise (a copy of) modules file for simulation
    val modulesFileCpy = modulesFile.deepCopy().replaceConstants(mfConstants).simplify().asInstanceOf[ModulesFile]

    // Create state/transition/rewards storage
    for (i <- 0 until strataSize) {
      stratifiedPaths(i).currentState = new State(numVars)
      stratifiedPaths(i).transitionList = new TransitionList()
    }
    tmpStateRewards = Array.ofDim[Double](modulesFileCpy.getNumRewardStructs)
    tmpTransitionRewards = Array.ofDim[Double](modulesFileCpy.getNumRewardStructs)

    // Create updater for model
    for (i <- 0 until strataSize)
      stratifiedPaths(i).updater = new Updater(modulesFileCpy, varList, this)

    // Clear storage for strategy
    strategy = null

    // Create storage for labels/properties
    labels = new util.ArrayList[Expression]()
    properties = new util.ArrayList[Expression]()
    this.asInstanceOf[SimulatorEngine].propertySamplers = new util.ArrayList[Sampler]()
    propertySamplers = JavaConverters.asScalaBuffer(this.asInstanceOf[SimulatorEngine].propertySamplers)
  }

  @throws[PrismException]
  override protected def executeTransition(i: Int, offset: Int, index: Int): Unit = {
    val transitions = getTransitionList()
    // Get corresponding choice and, if required (for full paths), calculate transition index
    val choice = transitions.getChoice(i)
    val index2 = if (!onTheFly && index == -1) transitions.getTotalIndexOfTransition(i, offset) else index
    // Get probability for transition
    val p = choice.getProbability(offset)
    // Compute its transition rewards
    stratifiedPaths(idx).updater.calculateTransitionRewards(stratifiedPaths(idx).path.getCurrentState, choice, tmpTransitionRewards)
    // Compute next state. Note use of path.getCurrentState() because currentState
    // will be overwritten during the call to computeTarget().
    choice.computeTarget(offset, stratifiedPaths(idx).path.getCurrentState, stratifiedPaths(idx).currentState)
    // Compute state rewards for new state
    stratifiedPaths(idx).updater.calculateStateRewards(stratifiedPaths(idx).currentState, tmpStateRewards);
    // Update path
    stratifiedPaths(idx).path.addStep(index2, choice.getModuleOrActionIndex, p, tmpTransitionRewards, stratifiedPaths(idx).currentState, tmpStateRewards, transitions)
    // Reset transition list
    stratifiedPaths(idx).transitionListBuilt = false
    stratifiedPaths(idx).transitionListState = null
    // Update samplers for any loaded properties
    updateSamplers()
    // Update strategy (if loaded)
    updateStrategy()
  }

  @throws[PrismException]
  override protected def executeTimedTransition(i: Int, offset: Int, time: Double, index: Int): Unit = {
    val transitions = getTransitionList()
    // Get corresponding choice and, if required (for full paths), calculate transition index
    val choice = transitions.getChoice(i);
    val index2 = if (!onTheFly && index == -1) transitions.getTotalIndexOfTransition(i, offset) else index
    // Get probability for transition
    val p = choice.getProbability(offset)
    // Compute its transition rewards
    stratifiedPaths(idx).updater.calculateTransitionRewards(stratifiedPaths(idx).path.getCurrentState, choice, tmpTransitionRewards)
    // Compute next state. Note use of path.getCurrentState() because currentState
    // will be overwritten during the call to computeTarget().
    choice.computeTarget(offset, stratifiedPaths(idx).path.getCurrentState, stratifiedPaths(idx).currentState)
    // Compute state rewards for new state
    stratifiedPaths(idx).updater.calculateStateRewards(stratifiedPaths(idx).currentState, tmpStateRewards)
    // Update path
    stratifiedPaths(idx).path.addStep(time, index, choice.getModuleOrActionIndex, p, tmpTransitionRewards, stratifiedPaths(idx).currentState, tmpStateRewards, transitions)
    // Reset transition list
    stratifiedPaths(idx).transitionListBuilt = false
    stratifiedPaths(idx).transitionListState = null
    // Update samplers for any loaded properties
    updateSamplers()
    // Update strategy (if loaded)
    updateStrategy()
  }

  @throws[PrismException]
  private[this] def resetSamplers(): Unit =
    for (sampler <- propertySamplers)
      sampler.reset()

  @throws[PrismException]
  private[this] def updateSamplers(): Unit =
    for (sampler <- propertySamplers)
      sampler.update(stratifiedPaths(idx).path, getTransitionList())

  @throws[PrismException]
  private[this] def recomputeSamplers(): Unit = {
    resetSamplers()
    // Get length (non-on-the-fly paths will never exceed length Integer.MAX_VALUE)
    val nLong = path.size();
    if (nLong > Integer.MAX_VALUE)
      throw new PrismLangException("PathFull cannot deal with paths over length " + Integer.MAX_VALUE)
    val n = nLong.toInt
    // Loop
    val prefix = new PathFullPrefix(stratifiedPaths(idx).path.asInstanceOf[PathFull], 0)
    for (i <- 0 to n) {
      prefix.setPrefixLength(i)
      for (sampler <- propertySamplers)
        sampler.update(prefix, null)
    }
  }

  private[this] def initialiseStrategy(): Unit =
    if (strategy != null) {
      val state = getCurrentState()
      val s = reachableStates.indexOf(state)
      strategy.initialise(s);
    }

  /**
    * Update the state of the loaded strategy, if present, based on the last step that occurred.
    */
  private[this] def updateStrategy(): Unit =
    if (strategy != null) {
      val state = getCurrentState()
      val s = reachableStates.indexOf(state)
      val action = stratifiedPaths(idx).path.getPreviousModuleOrAction()
      strategy.update(action, s)
    }

  // ------------------------------------------------------------------------------
  // Queries regarding model
  // ------------------------------------------------------------------------------

  override def getNumVariables(): Int = super.getNumVariables

  override def getVariableName(i: Int): String = getVariableName(i)

  override def getVariableType(i: Int): Type = super.getVariableType(i)

  @throws[PrismException]
  override def getIndexOfVar(name: String): Int = varList.getIndex(name)

  // ------------------------------------------------------------------------------
  // Querying of current state and its available choices/transitions
  // ------------------------------------------------------------------------------

  @throws[PrismException]
  override def getTransitionList: TransitionList = {
    // Compute the current transition list, if required
    if (!stratifiedPaths(idx).transitionListBuilt)
      computeTransitionsForCurrentState()
    stratifiedPaths(idx).transitionList
  }

  override def getTransitionListState(): State =
    if (stratifiedPaths(idx).transitionListState == null) stratifiedPaths(idx).path.getCurrentState() else stratifiedPaths(idx).transitionListState

  @throws[PrismException]
  override def getNumChoices(): Int = getTransitionList().getNumChoices()

  @throws[PrismException]
  override def getNumTransitions(): Int = getTransitionList().getNumTransitions()

  @throws[PrismException]
  override def getNumTransitions(i: Int): Int = getTransitionList().getChoice(i).size()

  @throws[PrismException]
  override def getChoiceIndexOfTransition(index: Int): Int = getTransitionList().getChoiceIndexOfTransition(index)

  @throws[PrismException]
  override def getTransitionModuleOrAction(i: Int, offset: Int): String = {
    val transitions = getTransitionList()
    transitions.getTransitionModuleOrAction(transitions.getTotalIndexOfTransition(i, offset))
  }

  @throws[PrismException]
  override def getTransitionModuleOrAction(index: Int): String = getTransitionList().getTransitionModuleOrAction(index)


  @throws[PrismException]
  override def getTransitionModuleOrActionIndex(i: Int, offset: Int): Int = {
    val transitions = getTransitionList()
    transitions.getTransitionModuleOrActionIndex(transitions.getTotalIndexOfTransition(i, offset))
  }

  @throws[PrismException]
  override def getTransitionModuleOrActionIndex(index: Int): Int =
    getTransitionList().getTransitionModuleOrActionIndex(index)

  @throws[PrismException]
  override def getTransitionAction(i: Int, offset: Int): String = {
    val transitions = getTransitionList()
    val a = transitions.getTransitionModuleOrActionIndex(transitions.getTotalIndexOfTransition(i, offset))
    if (a < 0) null else modulesFile.getSynch(a - 1)
  }

  @throws[PrismException]
  override def getTransitionAction(index: Int): String = {
    val a = getTransitionList().getTransitionModuleOrActionIndex(index)
    if (a < 0) null else modulesFile.getSynch(a - 1)
  }

  @throws[PrismException]
  override def getTransitionProbability(i: Int, offset: Int): Double = {
    val transitions = getTransitionList()
    transitions.getChoice(i).getProbability(offset)
  }

  @throws[PrismException]
  override def getTransitionProbability(index: Int): Double = {
    val transitions = getTransitionList()
    transitions.getTransitionProbability(index)
  }

  @throws[PrismException]
  override def getTransitionUpdateString(index: Int): String =
    getTransitionList().getTransitionUpdateString(index, getTransitionListState())

  @throws[PrismException]
  override def getTransitionUpdateStringFull(index: Int): String =
    getTransitionList().getTransitionUpdateStringFull(index)

  @throws[PrismException]
  override def computeTransitionTarget(i: Int, offset: Int): State =
    getTransitionList().getChoice(i).computeTarget(offset, getTransitionListState())

  @throws[PrismException]
  override def computeTransitionTarget(index: Int): State =
    getTransitionList().computeTransitionTarget(index, getTransitionListState())

  // ------------------------------------------------------------------------------
  // Querying of current path (full or on-the-fly)
  // ------------------------------------------------------------------------------

  override def getPath: Path = stratifiedPaths(idx).path.asInstanceOf[Path]

  override def getPathSize: Long = stratifiedPaths(idx).path.size()

  override def getCurrentState: State = stratifiedPaths(idx).path.getCurrentState

  override def getPreviousState: State = stratifiedPaths(idx).path.getPreviousState

  override def getTotalTimeForPath: Double = stratifiedPaths(idx).path.getTotalTime

  override def getTotalCumulativeRewardForPath(rsi: Int): Double = stratifiedPaths(idx).path.getTotalCumulativeReward(rsi)

  // ------------------------------------------------------------------------------
  // Querying of current path (full paths only)
  // ------------------------------------------------------------------------------

  override def getPathFull: PathFull = stratifiedPaths(idx).path.asInstanceOf[PathFull]

  override def getVariableValueOfPathStep(step: Int, varIndex: Int): AnyRef = stratifiedPaths(idx).path.asInstanceOf[PathFull].getState(step).varValues(varIndex)

  override def getStateOfPathStep(step: Int): State = stratifiedPaths(idx).path.asInstanceOf[PathFull].getState(step)

  override def getStateRewardOfPathStep(step: Int, rsi: Int): Double = stratifiedPaths(idx).path.asInstanceOf[PathFull].getStateReward(step, rsi)

  override def getCumulativeTimeUpToPathStep(step: Int): Double = stratifiedPaths(idx).path.asInstanceOf[PathFull].getCumulativeTime(step)

  override def getCumulativeRewardUpToPathStep(step: Int, rsi: Int): Double = stratifiedPaths(idx).path.asInstanceOf[PathFull].getCumulativeReward(step, rsi)

  override def getTimeSpentInPathStep(step: Int): Double = stratifiedPaths(idx).path.asInstanceOf[PathFull].getTime(step)

  override def getChoiceOfPathStep(step: Int): Int = stratifiedPaths(idx).path.asInstanceOf[PathFull].getChoice(step)

  override def getModuleOrActionIndexOfPathStep(step: Int): Int = stratifiedPaths(idx).path.asInstanceOf[PathFull].getModuleOrActionIndex(step)

  override def getModuleOrActionOfPathStep(step: Int): String = stratifiedPaths(idx).path.asInstanceOf[PathFull].getModuleOrAction(step)

  override def getTransitionRewardOfPathStep(step: Int, rsi: Int): Double = stratifiedPaths(idx).path.asInstanceOf[PathFull].getTransitionReward(step, rsi)

  override def isPathLooping: Boolean = stratifiedPaths(idx).path.isLooping

  override def loopStart(): Long = stratifiedPaths(idx).path.loopStart()

  override def loopEnd(): Long = stratifiedPaths(idx).path.loopEnd()

  @throws[PrismException]
  override def exportPath(file: File): Unit = exportPath(file, showRewards = false)

  @throws[PrismException]
  override def exportPath(file: File, showRewards: Boolean): Unit = exportPath(file, timeCumul = false, showRewards = showRewards, " ", null)


  @throws[PrismException]
  override def exportPath(file: File, timeCumul: Boolean, colSep: String, vars: util.ArrayList[Integer]): Unit =
    exportPath(file, timeCumul, showRewards = false, colSep, vars)

  @throws[PrismException]
  override def exportPath(file: File, timeCumul: Boolean, showRewards: Boolean, colSep: String, vars: util.ArrayList[Integer]) {
    var log: PrismLog = null
    try {
      if (stratifiedPaths(idx).path == null)
        throw new PrismException("There is no path to export")
      // create new file log or use main log
      if (file != null) {
        log = new PrismFileLog(file.getPath)
        if (!log.ready())
          throw new PrismException("Could not open file \"" + file + "\" for output")
        mainLog.println("\nExporting path to file \"" + file + "\"...")
      } else {
        log = mainLog
        log.println()
      }
      stratifiedPaths(idx).path.asInstanceOf[PathFull].exportToLog(log, timeCumul, showRewards, colSep, vars)
      if (file != null)
        log.close()
    } finally {
      if (file != null && log != null)
        log.close()
    }
  }

  @throws[PrismException]
  override def plotPath(graphModel: userinterface.graph.Graph): Unit =
    stratifiedPaths(idx).path.asInstanceOf[PathFull].plotOnGraph(graphModel)

  // ------------------------------------------------------------------------------
  // Model checking (approximate)
  // ------------------------------------------------------------------------------

  override def isPropertyOKForSimulation(expr: Expression): Boolean = isPropertyOKForSimulationString(expr) == null

  @throws[PrismException]
  override def checkPropertyForSimulation(expr: Expression): Unit = {
    val errMsg = isPropertyOKForSimulationString(expr)
    if (errMsg != null)
      throw new PrismNotSupportedException(errMsg)
  }

  override protected def isPropertyOKForSimulationString(expr: Expression): String = super.isPropertyOKForSimulationString(expr)

  @throws[PrismException]
  override def modelCheckSingleProperty(modulesFile: ModulesFile,
                                        propertiesFile: PropertiesFile,
                                        expr: Expression,
                                        initialState: State,
                                        maxPathLength: Long,
                                        simMethod: SimulationMethod): AnyRef =
    super.modelCheckSingleProperty(modulesFile, propertiesFile, expr, initialState, maxPathLength, simMethod)

  @throws[PrismException]
  @throws[InterruptedException]
  override def modelCheckExperiment(modulesFile: ModulesFile,
                                    propertiesFile: PropertiesFile,
                                    undefinedConstants: UndefinedConstants,
                                    resultsCollection: ResultsCollection,
                                    expr: Expression,
                                    initialState: State,
                                    maxPathLength: Long,
                                    simMethod: SimulationMethod): Unit =
    modelCheckExperiment(modulesFile, propertiesFile, undefinedConstants, resultsCollection, expr, initialState, maxPathLength, simMethod)

  @throws[PrismException]
  override def stopSampling(): Unit = Unit
}
