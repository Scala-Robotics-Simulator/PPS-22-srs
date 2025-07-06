import org.scalatest.*

import flatspec.*
import matchers.*

class ExampleSpec extends AnyFlatSpec with should.Matchers:

  "Integer" should "equal itself" in:
    val i = 1
    i should be(1)
