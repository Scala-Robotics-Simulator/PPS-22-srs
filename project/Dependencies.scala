import sbt.*

object Dependencies {
  /*
   * Versions
   */
  private lazy val scalaTestVersion = "3.2.19"
  private val squidLibVersion  = "3.0.6"
  /*
   * Libraries
   */
  private val scalaTest = "org.scalactic" %% "scalactic" % scalaTestVersion
  private val scalactic =
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  val scalaTestJUnit5 =
    "org.scalatestplus" %% "junit-5-10" % "3.2.19.1" % "test"
  val squidLib = "com.squidpony" % "squidlib-util" % squidLibVersion
  val catsCore = "org.typelevel" %% "cats-core" % "2.13.0"

  /*
   * Bundles
   */
  val scalaTestBundle = Seq(scalaTest, scalactic)
}

