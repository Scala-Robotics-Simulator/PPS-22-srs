package io.github.srs.model.logic

import io.github.srs.model.ModelModule
import io.github.srs.config.SimulationConfig
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.utils.random.RNG
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.BaseSimulationState

trait StateLogic[S <: ModelModule.BaseState]:
  def createState(cfg: SimulationConfig[ValidEnvironment]): S
  def updateState(state: S, rng: RNG): S

object StateLogic:

  given StateLogic[BaseSimulationState] with

    def createState(cfg: SimulationConfig[ValidEnvironment]): BaseSimulationState =
      BaseSimulationState.from(cfg)

    def updateState(state: BaseSimulationState, rng: RNG): BaseSimulationState =
      state.copy(simulationRNG = rng)
