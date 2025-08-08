package io.github.srs.model.lighting

import io.github.srs.model.lighting.Diffuser
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

/** Ensures the curried signature compiles and runs. */
class DiffuserTest extends AnyFlatSpec:

  private object DummyDiffuser extends Diffuser[Int, Int]:
    def step(v: Int)(s: Int): Int = v + s
    def intensityAt(s: Int)(c: io.github.srs.model.Cell): Double = 0.0

  "Diffuser" should "compile and run" in:
    val result = DummyDiffuser.step(5)(10)
    result shouldEqual 15
