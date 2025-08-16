package io.github.srs.model

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import io.github.srs.model.SimulationConfig.{ SimulationSpeed, SimulationStatus }
import io.github.srs.model.environment.ValidEnvironment
import io.github.srs.utils.random.SimpleRNG

final case class SimulationState(
    i: Int,
    override val simulationTime: Option[FiniteDuration],
    override val elapsedTime: FiniteDuration = FiniteDuration(0, MILLISECONDS),
    override val dt: FiniteDuration = FiniteDuration(100, MILLISECONDS),
    override val simulationSpeed: SimulationSpeed,
    override val simulationRNG: SimpleRNG,
    override val simulationStatus: SimulationStatus,
    override val environment: ValidEnvironment,
) extends ModelModule.State
