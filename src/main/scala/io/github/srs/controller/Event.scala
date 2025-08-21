package io.github.srs.controller

import scala.concurrent.duration.FiniteDuration

import cats.effect.IO
import cats.effect.std.Queue
import io.github.srs.controller.RobotProposal
import io.github.srs.model.SimulationConfig.SimulationSpeed
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.utils.random.RNG

enum Event derives CanEqual:
  case Increment
  case Stop
  case Pause
  case Resume
  case Tick(delta: FiniteDuration)
  case TickSpeed(speed: SimulationSpeed)
  case Random(rng: RNG)
  case RobotAction(queue: Queue[IO, Event], robot: Robot, action: Action[IO])
  case CollisionDetected(queue: Queue[IO, Event], robot: Robot, updatedRobot: Robot)
  case RobotActionProposals(queue: Queue[IO, Event], proposals: List[RobotProposal])
