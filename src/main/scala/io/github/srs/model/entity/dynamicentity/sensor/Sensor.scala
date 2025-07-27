package io.github.srs.model.entity.dynamicentity.sensor

import io.github.srs.model.PositiveDouble
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.model.environment.Environment
import io.github.srs.model.validation.Validation
import io.github.srs.utils.Ray.intersectRay

/**
 * Represents the range of a sensor.
 */
type Range = PositiveDouble

/**
 * Represents the distance from the center of a dynamic entity to a sensor.
 */
type Distance = PositiveDouble

/**
 * Represents a sensor reading for a specific sensor type and value.
 *
 * @tparam S
 *   the type of sensor.
 * @tparam A
 *   the type of value that the sensor returns.
 * @param sensor
 *   the sensor that produced the reading.
 * @param value
 *   the value sensed by the sensor.
 */
final case class SensorReading[S <: Sensor[?, ?, A], A](sensor: S, value: A)

/**
 * Represents a collection of sensor readings for a dynamic entity.
 *
 * @param proximity
 *   a sequence of proximity sensor readings, each containing the sensor and its sensed value.
 */
final case class SensorReadings(
    proximity: Vector[SensorReading[ProximitySensor[?, ?], Double]],
)

/**
 * Represents a sensor for a dynamic entity.
 *
 * @tparam Entity
 *   the type of dynamic entity that the sensor can sense.
 * @tparam Env
 *   the type of environment in which the sensor operates.
 * @tparam Data
 *   the type of data that the sensor returns.
 */
trait Sensor[-Entity <: DynamicEntity, -Env <: Environment, +Data]:
  /**
   * The offset orientation of the sensor relative to the entity.
   *
   * This is used to determine the direction in which the sensor is oriented.
   */
  val offset: Orientation

  /**
   * The distance from the center of the entity to the sensor.
   */
  val distance: Distance

  /**
   * The range of the sensor, which defines how far it can sense.
   */
  val range: Range

  /**
   * Senses the environment using the given entity.
   *
   * @param entity
   *   the entity that is sensing the environment.
   * @param environment
   *   the environment in which the entity is operating.
   * @return
   *   the data sensed by the sensor.
   */
  def sense(entity: Entity)(environment: Env): Data

end Sensor

trait ProximitySensor[Entity <: DynamicEntity, Env <: Environment] extends Sensor[Entity, Env, Double]:

  override val offset: Orientation

  override val distance: Distance

  override val range: Range

  /**
   * Senses the environment using the given entity.
   *
   * @param entity
   *   the entity that is sensing the environment.
   * @param environment
   *   the environment in which the entity is operating.
   * @return
   *   the data sensed by the sensor, which is a normalized distance to the nearest obstacle.
   */
  def sense(entity: Entity)(environment: Env): Double =
    import Point2D.*
    val globalOrientation = entity.orientation.toRadians + offset.toRadians
    val direction = Point2D(math.cos(globalOrientation), -math.sin(globalOrientation)) // x is right, y is down
    val origin = entity.position + direction * distance.toDouble
    val end = origin + direction * range.toDouble

    val distances = environment.entities.filter(!_.equals(entity)).flatMap(intersectRay(_, origin, end))

    distances.filter(_ <= range.toDouble).minOption.getOrElse(range.toDouble) / range.toDouble

end ProximitySensor

object ProximitySensor:

  /**
   * Creates a new `ProximitySensor` with the specified offset, distance, and range.
   *
   * @param offset
   *   the offset orientation of the sensor relative to the entity.
   * @param distance
   *   the distance from the center of the entity to the sensor.
   * @param range
   *   the range of the sensor.
   * @return
   *   a new instance of `ProximitySensor`.
   */
  def apply(
      offset: Orientation,
      distance: Double,
      range: Double,
  ): Validation[ProximitySensor[DynamicEntity, Environment]] =
    for
      distance <- PositiveDouble(distance)
      range <- PositiveDouble(range)
    yield new ProximitySensorImpl(offset, distance, range)

  private class ProximitySensorImpl(
      override val offset: Orientation,
      override val distance: Distance,
      override val range: Range,
  ) extends ProximitySensor[DynamicEntity, Environment]

end ProximitySensor

object Sensor:

  extension (e: DynamicEntity)

    /**
     * Senses the environment using the entity's sensors.
     *
     * @param env
     *   the environment in which the entity is operating.
     * @return
     *   a collection of sensor readings.
     */
    def sense(env: Environment): SensorReadings =
      e.sensors.sense(e, env)
