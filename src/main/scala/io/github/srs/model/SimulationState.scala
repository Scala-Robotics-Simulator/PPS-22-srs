package io.github.srs.model

import scala.concurrent.duration.FiniteDuration

final case class SimulationState(i: Int, simulationTime: FiniteDuration) extends ModelModule.State
