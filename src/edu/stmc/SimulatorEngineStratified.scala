/** ************************************************************************************************
  * STMC - Statistical Model Checker                                                               *
  * *
  * Copyright (C) 2019                                                                             *
  * Authors:                                                                                       *
  * Nima Roohi <nroohi@ucsd.edu> (University of California San Diego)                            *
  * *
  * This program is free software: you can redistribute it and/or modify it under the terms        *
  * of the GNU General Public License as published by the Free Software Foundation, either         *
  * version 3 of the License, or (at your option) any later version.                               *
  * *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;      *
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.      *
  * See the GNU General Public License for more details.                                           *
  * *
  * You should have received a copy of the GNU General Public License along with this program.     *
  * If not, see <https://www.gnu.org/licenses/>.                                                   *
  * *************************************************************************************************/

package edu.stmc

import java.util

import parser.State
import parser.ast.{Expression, ModulesFile, PropertiesFile}
import prism.{ModelType, PrismComponent, PrismException}
import simulator.SimulatorEngine
import simulator.method.SimulationMethod
import simulator.sampler.Sampler


class SimulatorEngineStratified(parent: PrismComponent) extends SimulatorEngine(parent) {

  @throws[PrismException]
  def modelCheckMultipleProperties(
                                    modulesFile: ModulesFile,
                                    propertiesFile: PropertiesFile,
                                    exprs: util.List[Expression],
                                    initialState: State,
                                    maxPathLength: Long,
                                    simMethod: SimulationMethod): Array[AnyRef] = {

    // Load model into simulator
    createNewOnTheFlyPath(modulesFile)

    // Make sure any missing parameters that can be computed before simulation
    // are computed now (sometimes this has been done already, e.g. for GUI display).
    simMethod.computeMissingParameterBeforeSim()

    // Print details to log
    mainLog.println("\nSimulation method: " + simMethod.getName + " (" + simMethod.getFullName + ")")
    mainLog.println("Simulation method parameters: " + simMethod.getParametersString)
    mainLog.println("Simulation parameters: max path length=" + maxPathLength)

    // Add the properties to the simulator (after a check that they are valid)
    val results: Array[AnyRef] = new Array[AnyRef](exprs.size)
    val indices: Array[Int] = new Array[Int](exprs.size)
    var validPropsCount: Int = 0
    var i: Int = 0

    for (i <- 0 until exprs.size)
      try {
        checkPropertyForSimulation(exprs.get(i))
        indices(i) = addProperty(exprs.get(i), propertiesFile)
        validPropsCount += 1
        // Attach a SimulationMethod object to each property's sampler
        val simMethodNew: SimulationMethod = simMethod.copy
        propertySamplers.get(indices(i)).setSimulationMethod(simMethodNew)
        // Pass property details to SimulationMethod
        // (note that we use the copy stored in properties, which has been processed)
        try
          simMethodNew.setExpression(properties.get(indices(i)))
        catch {
          case e: PrismException =>
            // In case of error, also need to remove property/sampler from list
            properties.remove(indices(i))
            propertySamplers.remove(indices(i))
            throw e
        }
      } catch {
        case e: PrismException =>
          results(i) = e
          indices(i) = -1
      }

    // As long as there are at least some valid props, do sampling
    if (validPropsCount > 0)
      doSampling(initialState, maxPathLength)

    // Process the results
    for (i <- results.indices)
    // If there was an earlier error, nothing to do
      if (indices(i) != -1) {
        val sampler: Sampler = propertySamplers.get(indices(i))
        //mainLog.print("Simulation results: mean: " + sampler.getMeanValue());
        //mainLog.println(", variance: " + sampler.getVariance());
        val sm: SimulationMethod = sampler.getSimulationMethod
        // Compute/print any missing parameters that need to be done after simulation
        sm.computeMissingParameterAfterSim()
        // Extract result from SimulationMethod and store
        try
          results(i) = sm.getResult(sampler)
        catch {
          case e: PrismException =>
            results(i) = e
        }
      }

    var resultNote: String = ""
    if (results.length > 0) {
      val currentModelType: ModelType = modulesFile.getModelType
      if (currentModelType.nondeterministic && (currentModelType.removeNondeterminism ne currentModelType))
        resultNote += " (with nondeterminism in " + currentModelType.name + " being resolved uniformly)"
    }

    // Display results to log
    if (results.length == 1) {
      mainLog.print("\nSimulation method parameters: ")
      mainLog.println(if (indices(0) == -1) "no simulation" else propertySamplers.get(indices(0)).getSimulationMethod().getParametersString())
      mainLog.print("\nSimulation result details: ");
      mainLog.println(if (indices(0) == -1) "no simulation" else propertySamplers.get(indices(0)).getSimulationMethodResultExplanation())
      if (!results(0).isInstanceOf[PrismException])
        mainLog.println("\nResult: " + results(0) + resultNote)
    } else {
      mainLog.println("\nSimulation method parameters:")
      for (i <- results.indices) {
        mainLog.print(exprs.get(i) + " : ")
        mainLog.println(if (indices(i) == -1) "no simulation" else propertySamplers.get(indices(i)).getSimulationMethod().getParametersString())
      }
      mainLog.println("\nSimulation result details:")
      for (i <- results.indices) {
        mainLog.print(exprs.get(i) + " : ")
        mainLog.println(if (indices(i) == -1) "no simulation" else propertySamplers.get(indices(i)).getSimulationMethodResultExplanation())
      }
      mainLog.println("\nResults:")
      for (i <- results.indices)
        mainLog.println(exprs.get(i) + " : " + results(i) + resultNote)
    }

    results
  }
}
