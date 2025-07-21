package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.validation.{ DomainError, Validation }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues.convertOptionToValuable

class DynamicEntityTest extends AnyFlatSpec with Matchers:

  val initialPosition: Point2D = Point2D(0.0, 0.0).toOption.value
  val initialOrientation: Orientation = Orientation(0.0).toOption.value
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)

  class Dummy(
      val position: Point2D,
      val shape: ShapeType,
      val orientation: Orientation,
      val actuators: Seq[Actuator[Dummy]],
  ) extends DynamicEntity:
    def act(): Validation[Dummy] = Right[DomainError, Dummy](this)

  class DummyActuator extends Actuator[Dummy]:
    override def act(entity: Dummy): Validation[Dummy] = Right[DomainError, Dummy](entity)

  "DynamicEntity" should "support having no actuators" in:
    val entity = new Dummy(initialPosition, shape, initialOrientation, Seq.empty)
    entity.actuators should be(Seq.empty)

  it should "support having some actuators" in:
    val actuator = new DummyActuator()
    val entity = new Dummy(initialPosition, shape, initialOrientation, Seq(actuator))
    entity.actuators should be(Seq(actuator))
end DynamicEntityTest
