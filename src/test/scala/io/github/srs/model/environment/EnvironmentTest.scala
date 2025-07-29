package io.github.srs.model.environment

import io.github.srs.model.entity.*
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EnvironmentTest extends AnyFlatSpec with Matchers:
  given CanEqual[Entity, Entity] = CanEqual.derived

  private def createEntity(p: (Double, Double), s: ShapeType, o: Orientation): Entity =
    new Entity:
      override def position: (Double, Double) = p
      override def shape: ShapeType = s
      override def orientation: Orientation = o

  "Environment" should "have a width and height" in:
    inside(Environment(10, 10)):
      case Right(environment) => (environment.width, environment.height) should be((10.0, 10.0))

  it should "not contain any entities by default" in:
    inside(Environment(10, 10)):
      case Right(environment) => environment.entities should be(Set.empty)

  it should "allow adding entities" in:
    val entity1 = createEntity((1.0, 1.0), ShapeType.Circle(1.0), Orientation(0.0))
    val entity2 = createEntity((5.0, 1.0), ShapeType.Circle(1.0), Orientation(90.0))
    val env = Environment(10, 10, Set(entity1, entity2))
    inside(env):
      case Right(environment) => environment.entities should contain theSameElementsAs Set(entity1, entity2)

  it should "extract environment fields correctly" in:
    val entity = createEntity((5.0, 5.0), ShapeType.Circle(2.0), Orientation(45.0))
    inside(Environment(20, 15, Set(entity))):
      case Right(environment) =>
        val result = environment match
          case Environment(w, h, es) => (w, h, es)
          case _ => fail("Pattern match failed")
        result shouldBe (20.0, 15.0, Set(entity))

end EnvironmentTest
