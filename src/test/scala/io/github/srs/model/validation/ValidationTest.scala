package io.github.srs.model.validation

import io.github.srs.model.validation.Validation
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.EitherValues.*

class ValidationTest extends AnyFlatSpec:

  "positive" should "accept positive Int" in :
    Validation.positive("count", 7).value shouldBe 7

  it should "accept positive Double" in :
    Validation.positive("ratio", 3.14).value shouldBe 3.14

  it should "reject zero Int" in :
    inside(Validation.positive("count", 0)):
      case Left(DomainError.NegativeOrZero("count", 0.0)) => succeed

  it should "return reject zero Double" in :
    inside(Validation.positive("ratio", 0.0)):
      case Left(DomainError.NegativeOrZero("ratio", 0.0)) => succeed

  it should "return reject negative Double" in :
    inside(Validation.positive("ratio", -1.2)):
      case Left(DomainError.NegativeOrZero("ratio", -1.2)) => succeed
    
