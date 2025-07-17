package io.github.srs.model.entity.dynamic_entity

import io.github.srs.model.entity.dynamic_entity.Robot
import io.github.srs.model.entity.{Orientation, Point2D, ShapeType}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RobotTest extends AnyFlatSpec with Matchers:

  val p: Point2D = Point2D(0.0, 0.0)
  val s: ShapeType.Circle = ShapeType.Circle(1.0)
  val o: Orientation = Orientation(0.0)

  "Robot" should "have an initial position" in:
    val robot: Robot = Robot(p, s, o, None)
    robot.position should be(Point2D(0.0, 0.0))

  it should "move to a new position" in:
    val newPosition: Point2D = Point2D(1.0, 1.0)
    val robot: Robot = Robot(p, s, o, None).moveTo(newPosition)
    robot.position should be(Point2D(1.0, 1.0))
