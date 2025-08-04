package io.github.srs.model

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.SimulationConfig.{ SimulationSpeed, SimulationStatus }
import io.github.srs.utils.SimpleRNG

final case class SimulationState(
    i: Int,
    simulationTime: FiniteDuration,
    simulationSpeed: SimulationSpeed,
    simulationRNG: SimpleRNG,
    simulationStatus: SimulationStatus,
) extends ModelModule.State
