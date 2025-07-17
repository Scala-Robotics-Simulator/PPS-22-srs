package io.github.srs.model.environment

import io.github.srs.model.{ Entity, Orientation, ShapeType }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

private def createEntity(p: (Double, Double), s: ShapeType, o: Orientation): Entity =
  new Entity:
    override def position: (Double, Double) = p
    override def shape: ShapeType = s
    override def orientation: Orientation = o

class EnvironmentTest extends AnyFlatSpec with should.Matchers:

  "Environment" should "have a width and height" in:
    val environment = Environment(10, 10);
    (environment.width, environment.height) should be((10.0, 10.0))

  it should "not contain any entities by default" in:
    val environment = Environment(10, 10)
    environment.entities should be(Set.empty)

  it should "allow adding entities" in:
    val entity1 = createEntity((1.0, 1.0), ShapeType.Circle(1.0), Orientation(0.0))
    val entity2 = createEntity((1.0, 1.0), ShapeType.Rectangle(2.0, 3.0), Orientation(90.0))
    val environment = Environment(width = 10, height = 10, entities = Set(entity1, entity2))
    environment.entities should contain theSameElementsAs Set(entity1, entity2)
end EnvironmentTest
