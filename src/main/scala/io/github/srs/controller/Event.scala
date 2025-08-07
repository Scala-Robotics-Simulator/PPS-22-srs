package io.github.srs.controller

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.SimulationConfig.SimulationSpeed

enum Event derives CanEqual:
  case Increment
  case Stop
  case Pause
  case Resume
  case Tick(delta: FiniteDuration)
  case TickSpeed(speed: SimulationSpeed)
