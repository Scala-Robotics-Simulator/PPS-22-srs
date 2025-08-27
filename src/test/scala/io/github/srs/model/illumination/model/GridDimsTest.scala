package io.github.srs.model.illumination.model

import scala.language.postfixOps

import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.illumination.model.{ GridDims, ScaleFactor }
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Tests for [[GridDims]], which provides utility methods for creating grid dimensions from an environment.
 */
final class GridDimsTest extends AnyFlatSpec with Matchers:

  private object C:

    /** Creates an environment with specified width and height, inserting boundaries */
    def envWithBoundaries(w: Int, h: Int): ValidEnvironment =
      (environment withWidth w withHeight h).validate.toOption.value

    val S1: ScaleFactor = ScaleFactor.validate(1).toOption.value
    val S3: ScaleFactor = ScaleFactor.validate(3).toOption.value
    val S4: ScaleFactor = ScaleFactor.validate(4).toOption.value

  "GridDims" should "multiply env size by scale (env validation with boundary)" in:
    val env = C.envWithBoundaries(11, 7)
    val dims = GridDims.from(env)(C.S3)
    dims shouldBe GridDims(33, 21)

  it should "work with different scales" in:
    val env = C.envWithBoundaries(5, 4)
    (GridDims(5, 4), GridDims(20, 16)) shouldBe (
      GridDims.from(env)(C.S1),
      GridDims.from(env)(C.S4),
    )
end GridDimsTest
