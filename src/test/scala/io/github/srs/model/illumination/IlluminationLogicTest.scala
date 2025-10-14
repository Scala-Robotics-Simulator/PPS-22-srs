package io.github.srs.model.illumination

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues.*
import io.github.srs.model.entity.*
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.illumination.engine.{ FovEngine, SquidLibFovEngine }
import io.github.srs.model.illumination.model.{ Cell, GridDims, ScaleFactor }

/**
 * Unit tests for the [[IlluminationLogic]] object.
 */
final class IlluminationLogicTest extends AnyFlatSpec with Matchers:

  private val usedFov: FovEngine = SquidLibFovEngine

  private object C:
    val EnvW = 6
    val EnvH = 6
    val S: ScaleFactor = ScaleFactor.validate(10).toOption.value
    val RobotPos: (Double, Double) = (2.5, 2.5)
    val LightPos: Point2D = Point2D(3.0, 3.0)
    val LightPosTwo: Point2D = Point2D(4.0, 4.0)
    val LightBulbRadius = 0.2
    val IlluminationRadius = 2.0
    val Tol = 1e-9

  private def mkEnv(entities: List[Entity]) =
    (environment withWidth C.EnvW withHeight C.EnvH containing entities).validate.toOption.value

  "IlluminationLogic" should "compute a light field with correct dimensions" in:
    val l = light
      .at(C.LightPos)
      .withRadius(C.LightBulbRadius)
      .withIlluminationRadius(C.IlluminationRadius)
      .withIntensity(1.0)
      .withAttenuation(1.0)

    val envV = mkEnv(List(l))
    val field = IlluminationLogic.computeLightField(C.S)(usedFov)(includeDynamic = false)(envV)
    val dims = field.dims

    dims shouldBe GridDims(C.EnvW * C.S, C.EnvH * C.S)

  it should "map light to the correct source cell" in:
    val l = light
      .at(C.LightPos)
      .withRadius(C.LightBulbRadius)
      .withIlluminationRadius(C.IlluminationRadius)
      .withIntensity(1.0)
      .withAttenuation(1.0)

    val envV = mkEnv(List(l))
    val field = IlluminationLogic.computeLightField(C.S)(usedFov)(includeDynamic = false)(envV)
    val dims = field.dims

    val (sx, sy) = Cell.toCellFloor(C.LightPos)(using C.S)
    val srcIdx = dims.toIndex(sx, sy)
    field.data.lift(srcIdx).value should be > 0.0

  it should "respect the illumination radius" in:
    val l = light
      .at(C.LightPos)
      .withRadius(C.LightBulbRadius)
      .withIlluminationRadius(C.IlluminationRadius)
      .withIntensity(1.0)
      .withAttenuation(1.0)

    val envV = mkEnv(List(l))
    val field = IlluminationLogic.computeLightField(C.S)(usedFov)(includeDynamic = false)(envV)
    val dims = field.dims
    val (sx, sy) = Cell.toCellFloor(C.LightPos)(using C.S)
    val rCells = Cell.radiusCells(C.IlluminationRadius)(using C.S)
    val outIdx = dims.toIndex(math.min(sx + rCells + 2, dims.widthCells - 1), sy)
    field.data.lift(outIdx).value should be <= C.Tol

  it should "map light to the correct source cell and respect the radius (cells)" in:
    val l = light
      .at(C.LightPos)
      .withRadius(C.LightBulbRadius)
      .withIlluminationRadius(C.IlluminationRadius)
      .withIntensity(1.0)
      .withAttenuation(1.0)

    val envV = mkEnv(List(l))
    val field = IlluminationLogic.computeLightField(C.S)(usedFov)(includeDynamic = false)(envV)
    val dims = field.dims
    val (sx, sy) = Cell.toCellFloor(C.LightPos)(using C.S)
    val rCells = Cell.radiusCells(C.IlluminationRadius)(using C.S)
    val inIdx = dims.toIndex(math.max(sx + math.max(rCells - 1, 0), 0), sy)
    field.data.lift(inIdx).value should be > 0.0

  it should "sum contributions from multiple lights correctly without exceeding 1.0" in:
    val l1 = light.at(C.LightPos).withRadius(0.1).withIlluminationRadius(2.0).withIntensity(1.0).withAttenuation(1.0)
    val l2 = light.at(C.LightPosTwo).withRadius(0.1).withIlluminationRadius(2.0).withIntensity(1.0).withAttenuation(1.0)
    val envV = mkEnv(List(l1, l2))

    val f = IlluminationLogic.computeLightField(C.S)(usedFov)(includeDynamic = false)(envV)

    val (sx, sy) = Cell.toCellFloor(C.LightPos)(using C.S)
    val idx = f.dims.toIndex(sx, sy)
    math.abs(f.data.lift(idx).value - 1.0) should be <= C.Tol

  it should "saturate the whole field to [0,1] when multiple lights overlap" in:
    val l1 = light.at(C.LightPos).withRadius(0.1).withIlluminationRadius(2.0).withIntensity(1.0).withAttenuation(1.0)
    val l2 = light.at(C.LightPosTwo).withRadius(0.1).withIlluminationRadius(2.0).withIntensity(1.0).withAttenuation(1.0)
    val envV = mkEnv(List(l1, l2))

    val f = IlluminationLogic.computeLightField(C.S)(usedFov)(includeDynamic = false)(envV)
    all(f.data) should (be >= 0.0 and be <= 1.0)

  it should "scale intensity correctly" in:
    val l = light.at(C.LightPos).withRadius(0.1).withIlluminationRadius(2.0).withIntensity(0.4).withAttenuation(1.0)
    val envV = mkEnv(List(l))
    val f = IlluminationLogic.computeLightField(C.S)(usedFov)(includeDynamic = false)(envV)

    val (sx, sy) = Cell.toCellFloor(C.LightPos)(using C.S)
    val idx = f.dims.toIndex(sx, sy)
    math.abs(f.data.lift(idx).value - 0.4) should be <= 1e-6

  it should "toggle dynamic occlusion with includeDynamic flag" in:
    val r = robot.at(C.RobotPos).withShape(ShapeType.Circle(0.2)).withOrientation(Orientation(0.0))
    val l = light.at(C.LightPos).withRadius(0.2).withIlluminationRadius(3.0).withIntensity(1.0).withAttenuation(1.0)
    val envV = mkEnv(List(r, l))

    val withDyn = IlluminationLogic.computeLightField(C.S)(usedFov)(includeDynamic = true)(envV)
    val noDyn = IlluminationLogic.computeLightField(C.S)(usedFov)(includeDynamic = false)(envV)

    val (rx, ry) = Cell.toCellFloor(C.RobotPos)(using C.S)
    val idx = withDyn.dims.toIndex(rx, ry)

    val _ = withDyn.data.lift(idx).value should be <= noDyn.data.lift(idx).value
    noDyn.data.lift(idx).value should be > 0.0
end IlluminationLogicTest
