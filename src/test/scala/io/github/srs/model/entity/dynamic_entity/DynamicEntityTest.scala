package io.github.srs.model.entity.dynamic_entity

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
      val actuators: Seq[Actuator[Dummy]],
  ) extends DynamicEntity:
    def act(): Dummy = this

  class DummyActuator extends Actuator[Dummy]:
    override def act(entity: Dummy): Dummy = entity

  "DynamicEntity" should "support having no actuators" in:
    val entity = new Dummy(p, shape, o, Seq.empty)
    entity.actuators should be(Seq.empty)

  it should "support having some actuators" in:
    val actuator = new DummyActuator()
    val entity = new Dummy(p, shape, o, Seq(actuator))
    entity.actuators should be(Seq(actuator))
end DynamicEntityTest
