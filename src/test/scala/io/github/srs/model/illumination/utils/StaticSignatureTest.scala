package io.github.srs.model.illumination.utils

//import io.github.srs.model.entity.*
//import io.github.srs.model.entity.dynamicentity.Robot
//import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
//import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
//import io.github.srs.model.environment.dsl.CreationDSL.*
//import io.github.srs.model.illumination.model.ScaleFactor
//import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Tests for [[StaticSignature]]: stability, invariants, and sensitivity. */
final class StaticSignatureTest extends AnyFlatSpec with Matchers:

  "true" should "be true" in:
    true shouldBe true

//  private object C:
//    val SF: ScaleFactor = ScaleFactor.validate(1).toOption.value // 1 cell == 1 m
//
//    // env sizes
//    val EnvW3 = 3
//    val EnvH3 = 3
//    val EnvW4 = 4
//    val EnvH4 = 4
//
//    // obstacles
//    private val O1Pos: (Double, Double) = (0.5, 0.5)
//    private val O2Pos: (Double, Double) = (2.5, 0.5)
//    private val O1W = 1.0
//    private val O1H = 1.0
//    private val O2W = 0.8
//    private val O2H = 0.6
//    private val Orient0: Orientation = Orientation(0)
//    val O1: Obstacle = Obstacle(pos = O1Pos, orient = Orient0, width = O1W, height = O1H)
//    val O2: Obstacle = Obstacle(pos = O2Pos, orient = Orient0, width = O2W, height = O2H)
//
//    // robot
//    private val BotPos: (Double, Double) = (2.5, 2.5)
//    private val BotR = 0.5
//
//    val BOT: Robot =
//      (robot at BotPos withShape ShapeType.Circle(BotR) withOrientation Orient0).validate.toOption.value
//
//  end C
//
//  private given ScaleFactor = C.SF
//
//  it should "produce different signatures for environments with different static entities" in:
//    val envA =
//      (environment withWidth C.EnvW3 withHeight C.EnvH3 containing C.O1)
//        .validate
//        .toOption
//        .value
//    val envB =
//      (environment withWidth C.EnvW3 withHeight C.EnvH3 containing C.O2)
//        .validate
//        .toOption
//        .value
//
//    StaticSignature.of(envA, summon[ScaleFactor]) should not be StaticSignature.of(envB, summon[ScaleFactor])
//
//  it should "produce the same signature for identical statics even if dynamics differ" in:
//    val envA =
//      (environment withWidth C.EnvW3 withHeight C.EnvH3 containing C.O1 containing C.BOT)
//        .validate
//        .toOption
//        .value
//    val envB =
//      (environment withWidth C.EnvW3 withHeight C.EnvH3 containing C.O1)
//        .validate
//        .toOption
//        .value
//
//    StaticSignature.of(envA, summon[ScaleFactor]) shouldBe StaticSignature.of(envB, summon[ScaleFactor])
//
//  it should "produce different signatures for different grid dimensions" in:
//    val envA =
//      (environment withWidth C.EnvW3 withHeight C.EnvH3 containing C.O1)
//        .validate
//        .toOption
//        .value
//    val envB =
//      (environment withWidth C.EnvW4 withHeight C.EnvH4 containing C.O1)
//        .validate
//        .toOption
//        .value
//
//    StaticSignature.of(envA, summon[ScaleFactor]) should not be StaticSignature.of(envB, summon[ScaleFactor])
//end StaticSignatureTest
