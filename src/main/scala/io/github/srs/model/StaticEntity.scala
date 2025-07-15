package io.github.srs.model

import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.*

enum StaticEntity(val position: Point2D, val orientation: Orientation) extends Entity:

  case Obstacle(
      pos: Point2D,
      orient: Orientation,
      width: Double,
      height: Double,
  ) extends StaticEntity(pos, orient)

  case Light(
      pos: Point2D,
      orient: Orientation,
      radius: Double,
      intensity: Double,
      attenuation: Double,
  ) extends StaticEntity(pos, orient)

  override def shape: ShapeType = this match
    case Obstacle(_, _, w, h) => ShapeType.Rectangle(w, h)
    case Light(_, _, r, _, _) => ShapeType.Circle(r)

end StaticEntity

object StaticEntity:

  def obstacle(
      pos: Point2D,
      orient: Orientation,
      width: Int,
      height: Int,
  ): Validation[StaticEntity] =
    for
      w <- positive("width", width)
      h <- positive("height", height)
    yield StaticEntity.Obstacle(pos, orient, w, h)

  def light(
      pos: Point2D,
      orient: Orientation,
      radius: Double,
      intensity: Double,
      attenuation: Double,
  ): Validation[StaticEntity] =
    for
      i <- positive("intensity", intensity)
      a <- positive("attenuation", attenuation)
    yield StaticEntity.Light(pos, orient, radius, i, a)
end StaticEntity
