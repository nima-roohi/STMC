package prism

import edu.stmc.STMCConfig
import parser.State
import parser.ast.{Expression, ModulesFile, PropertiesFile}
import simulator.method.SimulationMethod

object PrismHelper {

  def repeatedSimulation(prism:Prism,
                         currentModulesFile: ModulesFile,
                         propertiesFile: PropertiesFile,
                         expr: Expression,
                         initialState: State,
                         maxPathLength: Long,
                         simMethod: SimulationMethod): AnyRef = {
    val range = 1 to STMCConfig.repeat
    val possibly_par = if(STMCConfig.multithread) range.par else range

    possibly_par.map(_ => {
      val simMethodCopy : SimulationMethod = null //simMethod.clone()
      val res = prism.getSimulator.modelCheckSingleProperty(
        currentModulesFile, propertiesFile, expr, initialState, maxPathLength, simMethodCopy)
      val ss = prism.getSimulator.propertySamplers
      true
    })

    Boolean.box(false)
  }
}
