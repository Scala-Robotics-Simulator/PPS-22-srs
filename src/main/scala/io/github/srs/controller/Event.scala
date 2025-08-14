package io.github.srs.controller

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.SimulationConfig.SimulationSpeed
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.Action

enum Event derives CanEqual:
  case Increment
  case Stop
  case Pause
  case Resume
  case Tick(delta: FiniteDuration)
  case TickSpeed(speed: SimulationSpeed)
  case RobotAction(robot: Robot, action: Action[?])
