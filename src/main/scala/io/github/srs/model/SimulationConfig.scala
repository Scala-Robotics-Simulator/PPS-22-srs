package io.github.srs.model

enum SimulationStatus derives CanEqual:
  case RUNNING
  case PAUSED
  case STOPPED

object Configuration:

  trait SimulationConfig:
    def simulationStatus: SimulationStatus

  given SimulationConfig with
    override def simulationStatus: SimulationStatus = SimulationStatus.RUNNING
