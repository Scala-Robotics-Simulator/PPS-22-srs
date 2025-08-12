package io.github.srs.model

import io.github.srs.model.SimulationConfig.{SimulationSpeed, SimulationStatus}
import io.github.srs.model.environment.ValidEnvironment
import io.github.srs.utils.random.SimpleRNG

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

final case class SimulationState(
    i: Int,
    simulationTime: Option[FiniteDuration],
    elapsedTime: FiniteDuration = FiniteDuration(0, MILLISECONDS),
    simulationSpeed: SimulationSpeed,
    simulationRNG: SimpleRNG,
    simulationStatus: SimulationStatus,
    environment: ValidEnvironment,
) extends ModelModule.State
