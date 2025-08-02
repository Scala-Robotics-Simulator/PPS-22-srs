package io.github.srs.model.environment

import io.github.srs.model.entity.*
import io.github.srs.model.entity.staticentity.StaticEntity.Boundary.createBoundaries
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.validation.DomainError
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
    inside(Environment(10, 10).validate):
      case Right(environment) => (environment.width, environment.height) should be((10.0, 10.0))

  it should "only contain boundaries if no entities are passed" in:
    inside(Environment(10, 10).validate):
      case Right(environment) => environment.entities should contain theSameElementsAs createBoundaries(10, 10)

  it should "allow adding entities" in:
    val entity1 = createEntity((2.0, 2.0), ShapeType.Circle(1.0), Orientation(0.0))
    val entity2 = createEntity((5.0, 2.0), ShapeType.Circle(1.0), Orientation(90.0))
    val env = Environment(10, 10, Set(entity1, entity2)).validate
    inside(env):
      case Right(environment) =>
        environment.entities should contain theSameElementsAs Set(entity1, entity2) ++ createBoundaries(10, 10)

  it should "not be created with negative width" in:
    inside(Environment(-10, 10).validate):
      case Left(DomainError.OutOfBounds("width", _, _, _)) => succeed

  it should "not be created with negative height" in:
    inside(Environment(10, -10).validate):
      case Left(DomainError.OutOfBounds("height", _, _, _)) => succeed

  it should "not be created with zero width" in:
    inside(Environment(0, 10).validate):
      case Left(DomainError.OutOfBounds("width", _, _, _)) => succeed

  it should "not be created with zero height" in:
    inside(Environment(10, 0).validate):
      case Left(DomainError.OutOfBounds("height", _, _, _)) => succeed

  it should "not be created with width exceeding maximum" in:
    import io.github.srs.utils.SimulationDefaults.Environment.maxWidth
    inside(Environment(maxWidth + 1, 10).validate):
      case Left(DomainError.OutOfBounds("width", _, _, _)) => succeed

  it should "not be created with height exceeding maximum" in:
    import io.github.srs.utils.SimulationDefaults.Environment.maxHeight
    inside(Environment(10, maxHeight + 1).validate):
      case Left(DomainError.OutOfBounds("height", _, _, _)) => succeed

  it should "not be created with too many entities" in:
    import io.github.srs.utils.SimulationDefaults.Environment.maxEntities
    inside(
      Environment(
        10,
        10,
        (1 to maxEntities + 1)
          .map(i => createEntity((i.toDouble, i.toDouble), ShapeType.Circle(1.0), Orientation(0.0)))
          .toSet,
      ).validate,
    ):
      case Left(DomainError.OutOfBounds("entities", _, _, _)) => succeed

  it should "detect collisions in circular entities" in:
    val entity1 = createEntity((1.0, 1.0), ShapeType.Circle(1.0), Orientation(0.0))
    val entity2 = createEntity((1.5, 1.5), ShapeType.Circle(1.0), Orientation(90.0))
    inside(Environment(10, 10, Set(entity1, entity2)).validate):
      case Left(error) => error.errorMessage shouldBe "entities have 1 collision(s), expected none"

  it should "detect collisions in rectangular entities" in:
    val entity1 = createEntity((1.0, 1.0), ShapeType.Rectangle(2.0, 2.0), Orientation(0.0))
    val entity2 = createEntity((1.5, 1.5), ShapeType.Rectangle(2.0, 2.0), Orientation(90.0))
    inside(Environment(10, 10, Set(entity1, entity2)).validate):
      case Left(error) => error.errorMessage shouldBe "entities have 1 collision(s), expected none"

  it should "not detect collisions in non-overlapping circular entities" in:
    val entity1 = createEntity((1.0, 1.0), ShapeType.Circle(0.99), Orientation(0.0))
    val entity2 = createEntity((3.0, 3.0), ShapeType.Circle(1.0), Orientation(90.0))
    inside(Environment(10, 10, Set(entity1, entity2)).validate):
      case Right(environment) =>
        environment.entities should contain theSameElementsAs Set(entity1, entity2) ++ createBoundaries(10, 10)

  it should "not detect collisions in non-overlapping rectangular entities" in:
    val entity1 = createEntity((1.0, 1.0), ShapeType.Rectangle(2.0, 2.0), Orientation(0.0))
    val entity2 = createEntity((3.0, 3.0), ShapeType.Rectangle(2.0, 2.0), Orientation(90.0))
    inside(Environment(10, 10, Set(entity1, entity2)).validate):
      case Right(environment) =>
        environment.entities should contain theSameElementsAs Set(entity1, entity2) ++ createBoundaries(10, 10)

  it should "detect collisions in mixed entities" in:
    val circularEntity = createEntity((1.0, 1.0), ShapeType.Circle(1.0), Orientation(0.0))
    val rectangularEntity = createEntity((1.5, 1.5), ShapeType.Rectangle(2.0, 2.0), Orientation(90.0))
    inside(Environment(10, 10, Set(circularEntity, rectangularEntity)).validate):
      case Left(error) => error.errorMessage shouldBe "entities have 1 collision(s), expected none"

  it should "detect collisions between circular and rectangular entities" in:
    val circularEntity = createEntity((1.0, 1.0), ShapeType.Circle(1.0), Orientation(0.0))
    val rectangularEntity = createEntity((1.5, 1.5), ShapeType.Rectangle(2.0, 2.0), Orientation(90.0))
    inside(Environment(10, 10, Set(rectangularEntity, circularEntity)).validate):
      case Left(error) => error.errorMessage shouldBe "entities have 1 collision(s), expected none"

  it should "not detect collisions in mixed entities when they do not overlap" in:
    val circularEntity = createEntity((1.0, 1.0), ShapeType.Circle(0.99), Orientation(0.0))
    val rectangularEntity = createEntity((3.0, 3.0), ShapeType.Rectangle(2.0, 2.0), Orientation(90.0))
    inside(Environment(10, 10, Set(circularEntity, rectangularEntity)).validate):
      case Right(environment) =>
        environment.entities should contain theSameElementsAs Set(
          circularEntity,
          rectangularEntity,
        ) ++ createBoundaries(10, 10)

  it should "validate entities within bounds" in:
    val entity1 = createEntity((1.0, 1.0), ShapeType.Circle(0.99), Orientation(0.0))
    val entity2 = createEntity((5.0, 5.0), ShapeType.Circle(1.0), Orientation(90.0))
    inside(Environment(10, 10, Set(entity1, entity2)).validate):
      case Right(environment) =>
        environment.entities should contain theSameElementsAs Set(entity1, entity2) ++ createBoundaries(10, 10)

  it should "not validate entities out of bounds" in:
    val entity1 = createEntity((-1.0, 1.0), ShapeType.Circle(1.0), Orientation(0.0))
    val entity2 = createEntity((5.0, 5.0), ShapeType.Circle(1.0), Orientation(90.0))
    inside(Environment(10, 10, Set(entity1, entity2)).validate):
      case Left(error) =>
        error.errorMessage shouldBe "entities = (-1.0, 1.0) is outside the bounds (width: [0.0, 10.0], height: [0.0, 10.0])"

  it should "not validate entities out of x axis bounds" in:
    val entity1 = createEntity((11.0, 1.0), ShapeType.Circle(1.0), Orientation(0.0))
    val entity2 = createEntity((5.0, 5.0), ShapeType.Circle(1.0), Orientation(90.0))
    inside(Environment(10, 10, Set(entity1, entity2)).validate):
      case Left(_) => succeed

  it should "not validate entities out of y axis bounds" in:
    val entity1 = createEntity((1.0, 11.0), ShapeType.Circle(1.0), Orientation(0.0))
    val entity2 = createEntity((5.0, 5.0), ShapeType.Circle(1.0), Orientation(90.0))
    inside(Environment(10, 10, Set(entity1, entity2)).validate):
      case Left(_) => succeed

  it should "not validate entities out of bounds with negative coordinates" in:
    val entity1 = createEntity((1.0, -1.0), ShapeType.Circle(1.0), Orientation(0.0))
    val entity2 = createEntity((5.0, 5.0), ShapeType.Circle(1.0), Orientation(90.0))
    inside(Environment(10, 10, Set(entity1, entity2)).validate):
      case Left(_) => succeed

end EnvironmentTest
