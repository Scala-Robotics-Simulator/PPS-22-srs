package io.github.srs.model.entity.dynamicentity.sensor

import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.{Orientation, Point2D}
import io.github.srs.model.environment.Environment
import io.github.srs.utils.PositiveDouble
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

/**
 * Represents a proximity sensor for a dynamic entity.
 * @param offset
 *   the offset orientation of the sensor relative to the entity.
 */
final case class ProximitySensor(
    override val offset: Orientation,
    override val distance: Distance,
    override val range: Range,
) extends Sensor[DynamicEntity, Environment, Double]:

  def sense(entity: DynamicEntity)(env: Environment): Double = 42.0 // Dummy implementation for testing purposes
