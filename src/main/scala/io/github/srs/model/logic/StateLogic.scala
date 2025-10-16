package io.github.srs.model.logic

import io.github.srs.model.ModelModule
import io.github.srs.config.SimulationConfig
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.utils.random.RNG

trait StateLogic[S <: ModelModule.BaseState]:
  def createState(cfg: SimulationConfig[ValidEnvironment]): S
  def updateState(state: S, rng: RNG): S
