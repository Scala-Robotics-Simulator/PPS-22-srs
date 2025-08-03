package io.github.srs.model

import scala.concurrent.duration.FiniteDuration

final case class SimulationState(
    i: Int,
    simulationTime: FiniteDuration,
    simulationStatus: SimulationStatus,
) extends ModelModule.State
