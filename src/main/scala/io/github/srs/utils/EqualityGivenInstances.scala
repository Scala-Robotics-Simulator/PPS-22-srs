package io.github.srs.utils

import java.util.UUID

import cats.effect.IO
import io.github.srs.model.entity.*
import io.github.srs.model.environment.Environment
import io.github.srs.model.entity.dynamicentity.*
import io.github.srs.model.entity.dynamicentity.action.*
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Rule
import io.github.srs.model.entity.dynamicentity.sensor.*
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.staticentity.StaticEntity.{ Light, Obstacle }

object EqualityGivenInstances:
  given CanEqual[StaticEntity, StaticEntity] = CanEqual.derived
  given CanEqual[DynamicEntity, DynamicEntity] = CanEqual.derived
  given CanEqual[Entity, Entity] = CanEqual.derived
  given CanEqual[Obstacle, Obstacle] = CanEqual.derived
  given CanEqual[Light, Light] = CanEqual.derived
  given CanEqual[Robot, Robot] = CanEqual.derived
  given CanEqual[UUID, UUID] = CanEqual.derived
  given CanEqual[ShapeType, ShapeType] = CanEqual.derived
  given CanEqual[Orientation, Orientation] = CanEqual.derived
  given CanEqual[Seq[Actuator[Robot]], Seq[Actuator[Robot]]] = CanEqual.derived
  given CanEqual[Vector[Sensor[Robot, Environment]], Vector[Sensor[Robot, Environment]]] = CanEqual.derived
  given CanEqual[Rule[IO, SensorReadings, Action[IO]], Rule[IO, SensorReadings, Action[IO]]] = CanEqual.derived
