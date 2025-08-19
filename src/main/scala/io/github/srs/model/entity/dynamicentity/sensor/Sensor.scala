package io.github.srs.model.entity.dynamicentity.sensor

import cats.syntax.all.*
import io.github.srs.model.PositiveDouble
import io.github.srs.model.entity.dynamicentity.{ DynamicEntity, Robot }
import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.model.environment.Environment
import io.github.srs.model.validation.Validation
import io.github.srs.utils.Ray.intersectRay
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Sensor.ProximitySensor as ProximitySensorDefaults
import io.github.srs.model.illumination.LightMap
import io.github.srs.model.illumination.engine.SquidLibFovEngine
import io.github.srs.model.illumination.model.ScaleFactor
import cats.effect.kernel.Sync

/**
 * Represents the range of a sensor.
 */
type Range = Double

/**
 * Represents the distance from the center of a dynamic entity to a sensor.
 */
type Distance = Double

/**
 * Represents a sensor that can sense the environment for a dynamic entity.
 * @tparam Entity
 *   the type of dynamic entity that the sensor can act upon.
 * @tparam Env
 *   the type of environment in which the sensor operates.
 */
trait Sensor[-Entity <: DynamicEntity, -Env <: Environment]:
  /**
   * The type of data that the sensor returns. This type can vary based on the specific sensor implementation.
   */
  type Data

  /**
   * The offset orientation of the sensor relative to the entity's orientation.
   * @return
   *   the orientation offset of the sensor.
   */
  def offset: Orientation

  /**
   * The distance from the center of the entity to the sensor.
   * @return
   *   the distance of the sensor from the entity's center.
   */
  def distance: Distance

  /**
   * The range of the sensor, which defines how far it can sense.
   * @note
   *   The range is typically a positive value that indicates the maximum distance the sensor can detect.
   * @return
   *   the range of the sensor.
   */
  def range: Range

  /**
   * Senses the environment for the given entity and returns the data collected by the sensor.
   * @param entity
   *   the dynamic entity that the sensor is attached to.
   * @param env
   *   the environment in which the sensor operates.
   * @tparam F
   *   the effect type in which the sensing operation is performed.
   * @return
   *   a monadic effect containing the data sensed by the sensor.
   */
  def sense[F[_]: Sync](entity: Entity, env: Env): F[Data]
end Sensor

/**
 * Represents a reading from a sensor. This case class encapsulates the sensor and the value it has sensed.
 * @param sensor
 *   the sensor that has taken the reading.
 * @param value
 *   the value sensed by the sensor.
 * @tparam S
 *   the type of sensor, which is a subtype of [[Sensor]].
 * @tparam A
 *   the type of data sensed by the sensor.
 */
final case class SensorReading[S <: Sensor[?, ?], A](sensor: S, value: A)

/**
 * A collection of sensor readings. This type is used to represent multiple sensor readings from a dynamic entity. It is
 * a vector of [[SensorReading]] instances, allowing for efficient access and manipulation of sensor data.
 */
type SensorReadings = Vector[SensorReading[? <: Sensor[?, ?], ?]]

/**
 * A proximity sensor that can sense the distance to other entities in the environment. It calculates the distance to
 * the nearest entity within its range and returns a normalized value. The value is normalized to a range between 0.0
 * (closest) and 1.0 (farthest).
 * @param offset
 *   the offset orientation of the sensor relative to the entity's orientation.
 * @param distance
 *   the distance from the center of the entity to the sensor.
 * @param range
 *   the range of the sensor, which defines how far it can sense.
 * @tparam Entity
 *   the type of dynamic entity that the sensor can act upon.
 * @tparam Env
 *   the type of environment in which the sensor operates.
 */
final case class ProximitySensor[Entity <: DynamicEntity, Env <: Environment](
    offset: Orientation = Orientation(ProximitySensorDefaults.defaultOffset),
    distance: Distance = ProximitySensorDefaults.defaultDistance,
    range: Range = ProximitySensorDefaults.defaultRange,
) extends Sensor[Entity, Env]:

  override type Data = Double

  override def sense[F[_]: Sync](entity: Entity, env: Env): F[Data] =
    Sync[F].pure:
      import Point2D.*
      val globalOrientation = entity.orientation.toRadians + offset.toRadians
      val direction = Point2D(math.cos(globalOrientation), -math.sin(globalOrientation))
      val origin = entity.position + direction * distance
      val end = origin + direction * range

      val distances = env.entities
        .filter(!_.equals(entity))
        .flatMap(intersectRay(_, origin, end))
        .filter(_ <= range)

      distances.minOption.map(_ / range).getOrElse(1.0)

end ProximitySensor

/**
 * A light sensor that senses the light intensity in the environment.
 * @param offset
 *   the offset orientation of the sensor relative to the entity's orientation.
 * @param distance
 *   the distance from the center of the entity to the sensor.
 * @param range
 *   the range of the sensor, which defines how far it can sense.
 * @tparam Entity
 *   the type of dynamic entity that the sensor can act upon.
 * @tparam Env
 *   the type of environment in which the sensor operates.
 */
final case class LightSensor[Entity <: DynamicEntity, Env <: Environment](
    offset: Orientation = Orientation(ProximitySensorDefaults.defaultOffset),
    distance: Distance = ProximitySensorDefaults.defaultDistance,
    range: Range = ProximitySensorDefaults.defaultRange,
) extends Sensor[Entity, Env]:

  override type Data = Double

  /**
   * Senses the light intensity at the position of the sensor in the environment. It uses a cached light map to compute
   * the field of light and samples it at the sensor's position.
   * @param entity
   *   the dynamic entity that the sensor is attached to.
   * @param env
   *   the environment in which the sensor operates.
   * @return
   *   a monadic effect containing the light intensity sensed by the sensor.
   */
  override def sense[F[_]: Sync](entity: Entity, env: Env): F[Data] =
    import Point2D.*
    val globalOrientation = entity.orientation.toRadians + offset.toRadians
    val direction = Point2D(math.cos(globalOrientation), -math.sin(globalOrientation))
    val origin = entity.position + direction * distance
    for
      lightMap <- LightMap.cached[F](SquidLibFovEngine, ScaleFactor.default)
      field <- lightMap.computeField(env = env, includeDynamic = true)
    yield field.sampleAtWorld(origin)(using ScaleFactor.default)

end LightSensor

object Sensor:

  extension [E <: DynamicEntity, Env <: Environment](s: Sensor[E, Env])

    /**
     * Validates the properties of a sensor.
     */
    def validate: Validation[Sensor[E, Env]] =
      for
        _ <- PositiveDouble(s.distance).validate
        _ <- PositiveDouble(s.range).validate
      yield s

  extension (r: Robot)

    /**
     * Senses all sensors of the robot in the given environment.
     * @param env
     *   the environment in which to sense.
     * @return
     *   a vector of sensor readings.
     */
    def senseAll[F[_]: Sync](env: Environment): F[SensorReadings] =
      r.sensors.traverse: sensor =>
        sensor.sense(r, env).map(reading => SensorReading(sensor, reading))
end Sensor
