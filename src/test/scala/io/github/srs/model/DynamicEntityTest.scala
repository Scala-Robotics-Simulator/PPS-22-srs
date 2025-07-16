package io.github.srs.model

import io.github.srs.model.entity.dynamic.{ Actuator, DynamicEntity, Wheel }
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DynamicEntityTest extends AnyFlatSpec with Matchers:

  val p = Point2D(0.0, 0.0)
  val o = Orientation(0.0)
  val shape = ShapeType.Circle(1.0)

  class Dummy(
      val position: Point2D,
      val shape: ShapeType,
      val orientation: Orientation,
      val actuators: Option[Seq[Actuator]],
  ) extends DynamicEntity

  "DynamicEntity" should "support having no actuators" in:
    val entity = new Dummy(p, shape, o, None)
    entity.actuators should be(None)

  it should "support having some actuators" in:
    val actuator = Actuator.WheelMotor(Wheel(1.0, ShapeType.Circle(0.05)), Wheel(1.0, ShapeType.Circle(0.05)))
    val entity = new Dummy(p, shape, o, Some(Seq(actuator)))
    entity.actuators should be(Some(Seq(actuator)))
end DynamicEntityTest
