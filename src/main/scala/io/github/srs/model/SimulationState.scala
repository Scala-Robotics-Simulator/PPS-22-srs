package io.github.srs.model

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.SimulationConfig.{ SimulationSpeed, SimulationStatus }

final case class SimulationState(
    i: Int,
    simulationTime: FiniteDuration,
    simulationSpeed: SimulationSpeed,
    simulationStatus: SimulationStatus,
) extends ModelModule.State
