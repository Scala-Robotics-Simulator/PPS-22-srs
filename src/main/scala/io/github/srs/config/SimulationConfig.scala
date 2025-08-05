package io.github.srs.config

import io.github.srs.model.Simulation
import io.github.srs.model.environment.Environment

trait SimulationConfig:
  def simulation: Simulation
  def environment: Environment

object SimulationConfig:

  def apply(sim: Simulation, env: Environment): SimulationConfig =
    new SimulationConfig:
      override def simulation: Simulation = sim
      override def environment: Environment = env
