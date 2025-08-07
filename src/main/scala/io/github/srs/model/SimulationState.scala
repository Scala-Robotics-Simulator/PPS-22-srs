package io.github.srs.model

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import io.github.srs.model.SimulationConfig.{ SimulationSpeed, SimulationStatus }
import io.github.srs.utils.random.SimpleRNG

final case class SimulationState(
    i: Int,
    simulationTime: Option[FiniteDuration],
    elapsedTime: FiniteDuration = FiniteDuration(0, MILLISECONDS),
    simulationSpeed: SimulationSpeed,
    simulationRNG: SimpleRNG,
    simulationStatus: SimulationStatus,
) extends ModelModule.State
