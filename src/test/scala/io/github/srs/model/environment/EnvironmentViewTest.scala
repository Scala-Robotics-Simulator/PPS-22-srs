package io.github.srs.model.environment

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.OptionValues.*
import io.github.srs.model.*
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.entity.staticentity.StaticEntity.{ Light, Obstacle }
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.{ DifferentialWheelMotor, Robot }
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.dynamicentity.sensor.Sensor

class EnvironmentViewTest extends AnyFlatSpec:

  given CanEqual[StaticEntity, StaticEntity] = CanEqual.derived

  // ––– Scenario constants ––––––––––––––––––––––––––––––––––––––––––––––––
  private val W = 10
  private val H = 10

  private val obsOrigin = Point2D(1.0, 1.0)
  private val obstacleWidth = 2
  private val obstacleHeight = 1

  private val obstacleCells: Set[Cell] =
    val tl = obsOrigin.toCell
    (for
      dx <- 0 until obstacleWidth
      dy <- 0 until obstacleHeight
    yield Cell(tl.x + dx, tl.y + dy)).toSet

  private val lightOrigin = Point2D(4.0, 4.0)
  private val illuminationRadius = 3.0
  private val lightIntensity = 1.0
  private val lightAttenuation = 0.2

  private val expectedLight =
    Light(
      lightOrigin,
      Orientation(0),
      illuminationRadius = illuminationRadius,
      intensity = lightIntensity,
      attenuation = lightAttenuation,
    )

  private val robotPos = Point2D(5.0, 5.0)

  private val robot: Robot =
    Robot(
      position = robotPos,
      shape = ShapeType.Circle(0.5),
      orientation = Orientation(0),
      actuators = Seq.empty[DifferentialWheelMotor], // no motors needed for the test
      sensors = Vector.empty[Sensor[Robot, Environment]], // no sensors needed for the test
    ).validate.toOption.value

  private val env: Environment =
    Environment(
      W,
      H,
      Set(
        Obstacle(obsOrigin, Orientation(0), obstacleWidth, obstacleHeight),
        expectedLight,
        robot,
      ),
    ).validate.toOption.value

  private val viewStatic: EnvironmentView = env.view // robots are transparent
  private val viewDynamic: EnvironmentView = EnvironmentView.dynamic(env) // robots are solid

  "EnvironmentView.static" should "report correct dimensions" in:
    (viewStatic.width, viewStatic.height) shouldBe (W, H)

  it should "contain exactly the obstacle cells" in:
    viewStatic.obstacles should contain theSameElementsAs obstacleCells

  it should "list robot correctly" in:
    viewStatic.robots should contain theSameElementsAs Vector(robot)

  it should "list the robot but NOT block its cell in resistance grid" in:
    val rc = robotPos.toCell
    viewStatic.resistance(rc.x)(rc.y) shouldBe 0.0 // transparent

  it should "list exactly the static lights declared" in:
    viewStatic.lights should contain only expectedLight

  it should "set resistance = 1.0 only on obstacle cells" in:
    val grid = viewStatic.resistance
    obstacleCells.foreach(c => grid(c.x)(c.y) shouldBe 1.0)
    Seq(Cell(0, 0), Cell(3, 3)).foreach(c => grid(c.x)(c.y) shouldBe 0.0)

  "EnvironmentView.dynamic" should "make robot cells opaque" in:
    val rc = robotPos.toCell
    viewDynamic.resistance(rc.x)(rc.y) shouldBe 1.0 // robot blocks light

  it should "list the robot as opaque" in:
    obstacleCells.foreach(c => viewDynamic.resistance(c.x)(c.y) shouldBe 1.0)

end EnvironmentViewTest
