package io.github.srs.controller

import scala.concurrent.duration.FiniteDuration

enum Event derives CanEqual:
  case ChangeTime(simulationTime: FiniteDuration)
  case Increment
  case Stop
  case Tick(delta: FiniteDuration)
