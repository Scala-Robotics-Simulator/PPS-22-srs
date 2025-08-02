package io.github.srs.model.environment.dsl

import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.entity.{ Entity, Orientation }
import io.github.srs.model.environment.Environment
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CreationDSLTest extends AnyFlatSpec with Matchers:
  import CreationDSL.*

  "CreationDSL" should "create an environment with default values" in:
    import io.github.srs.utils.SimulationDefaults.Environment.*
    val env = Environment()
    val _ = env.width shouldBe defaultWidth
    val _ = env.height shouldBe defaultHeight
    env.entities shouldBe empty

  it should "allow setting width" in:
    val env = Environment() withWidth 20
    env.width shouldBe 20

  it should "allow setting height" in:
    val env = Environment() withHeight 30
    env.height shouldBe 30

  it should "allow adding multiple entities with infix notation" in:
    val entity1 = Obstacle(pos = (5.0, 5.0), orient = Orientation(0.0), width = 1.0, height = 1.0)
    val entity2 = Obstacle(pos = (2.0, 2.0), orient = Orientation(0.0), width = 1.0, height = 1.0)
    val env = Environment() containing entity1 and entity2
    env.entities should contain theSameElementsAs Set(entity1, entity2)

  it should "allow adding a set of entities" in:
    val entity1 = Obstacle(pos = (5.0, 5.0), orient = Orientation(0.0), width = 1.0, height = 1.0)
    val entity2 = Obstacle(pos = (2.0, 2.0), orient = Orientation(0.0), width = 1.0, height = 1.0)
    val entities: Set[Entity] = Set(entity1, entity2)
    val env = Environment() containing entities
    env.entities should contain theSameElementsAs Set(entity1, entity2)

  it should "allow adding a single entity" in:
    val entity = Obstacle(pos = (3.0, 3.0), orient = Orientation(0.0), width = 1.0, height = 1.0)
    val env = Environment() containing entity
    env.entities should contain(entity)

  it should "validate the environment" in:
    val env = Environment()
    val validationResult = env.validate
    validationResult.isRight shouldBe true

  it should "validate the environment without adding boundaries" in:
    val env = Environment()
    val validationResult = env.validate(insertBoundaries = false)
    validationResult.isRight shouldBe true

  it should "not allow colliding entities" in:
    val entity1 = Obstacle(pos = (1.0, 1.0), orient = Orientation(0.0), width = 1.0, height = 1.0)
    val entity2 = Obstacle(pos = (1.5, 1.5), orient = Orientation(0.0), width = 1.0, height = 1.0)
    val env = Environment() containing entity1 and entity2
    val validationResult = env.validate
    validationResult.isLeft shouldBe true
end CreationDSLTest
