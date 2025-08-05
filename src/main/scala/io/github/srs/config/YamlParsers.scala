package io.github.srs.config

import scala.annotation.unused

import io.github.srs.config.ConfigResult
import io.github.srs.config.ConfigResult.Success
import io.github.srs.model.Simulation
import io.github.srs.model.environment.dsl.CreationDSL.environment

object YamlParsers:

  def parseSimulationConfig(@unused content: String): ConfigResult[SimulationConfig] =
    import Simulation.*
    Success(SimulationConfig(simulation, environment))
