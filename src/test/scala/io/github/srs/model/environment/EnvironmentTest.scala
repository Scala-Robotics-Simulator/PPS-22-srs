package io.github.srs.model.environment

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class EnvironmentTest extends AnyFlatSpec with should.Matchers:

  "Environment" should "have a width and height" in:
    val environment = Environment(width = 10, height = 10);
    (environment.width, environment.height) should be((10.0, 10.0))
