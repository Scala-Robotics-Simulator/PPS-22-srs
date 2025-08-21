package io.github.srs.model.entity.dynamicentity.behavior

import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Behavior
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings
import io.github.srs.model.entity.dynamicentity.action.Action
import cats.effect.IO

enum Policy(behavior: Behavior[SensorReadings, Action[IO]]):
  case Simple extends Policy(Behaviors.simple[IO])
  case AlwaysForward extends Policy(Behaviors.alwaysForward[IO])

  def run(readings: SensorReadings): Action[IO] =
    behavior.run(readings)
