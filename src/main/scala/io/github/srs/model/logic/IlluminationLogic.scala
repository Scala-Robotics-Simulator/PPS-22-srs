package io.github.srs.model.logic

import cats.effect.IO
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.utils.SimulationDefaults

trait IlluminationLogic[S <: ModelModule.State]:

  /**
   * Updates the light field in the simulation state.
   * @param s
   *   the current simulation state.
   * @return
   *   an [[IO]] effect that produces the updated simulation state with the new light field.
   */
  def updateLightField(s: S): IO[S]

object IlluminationLogic:

  given IlluminationLogic[SimulationState] with

    def updateLightField(s: SimulationState): IO[SimulationState] =
      println("Updating light field...")
      for newField <- SimulationDefaults.lightMap.computeField(s.environment, includeDynamic = true)
      yield s.copy(lightField = newField)
