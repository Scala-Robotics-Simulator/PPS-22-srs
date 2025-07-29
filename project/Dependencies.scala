import sbt.*

object Dependencies {

  /*
   * Versions
   */
  private lazy val scalaTestVersion = "3.2.19"
  private lazy val squidLibVersion = "3.0.6"
  private lazy val scalaTestPlusVersion = "3.2.19.1"
  private lazy val monixVersion = "3.4.1"

  /*
   * Libraries
   */
  private val scalaTest = "org.scalactic" %% "scalactic" % scalaTestVersion
  private val scalactic = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  val scalaTestPlusJUnit5 = "org.scalatestplus" %% "junit-5-10" % scalaTestPlusVersion % "test"
  val squidLib = "com.squidpony" % "squidlib-util" % squidLibVersion
  val catsCore = "org.typelevel" %% "cats-core" % "2.13.0"

  /*
   * Bundles
   */
  val scalaTestBundle: Seq[ModuleID] = Seq(scalaTest, scalactic)
}

