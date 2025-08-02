package io.github.srs.model

import scala.concurrent.duration.FiniteDuration

import monix.eval.Task

object SimulationLogic:

  given IncrementLogic[SimulationState] with

    def increment(s: SimulationState): Task[SimulationState] =
      Task(s.copy(i = s.i + 1))

  given ChangeTimeLogic[SimulationState] with

    def changeTime(s: SimulationState, simulationTime: FiniteDuration): Task[SimulationState] =
      Task(s.copy(i = 0, simulationTime = simulationTime))
