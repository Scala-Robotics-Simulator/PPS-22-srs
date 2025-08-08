import sbt.*

object Dependencies {

  /*
   * Versions
   */
  private lazy val scalaTestVersion = "3.2.19"
  private lazy val squidLibVersion = "3.0.6"

  /*
   * Libraries
   */
  private val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
  private val scalactic = "org.scalactic" %% "scalactic" % scalaTestVersion % "test"
  val scalaTestJUnit5 = "org.scalatestplus" %% "junit-5-10" % "3.2.19.1" % "test"
  val squidLib = "com.squidpony" % "squidlib-util" % squidLibVersion
  val catsCore = "org.typelevel" %% "cats-core" % "2.13.0"
  val catsEffect = "org.typelevel" %% "cats-effect" % "3.6.3"

  /*
   * Bundles
   */
  val scalaTestBundle: Seq[ModuleID] = Seq(scalaTest, scalactic)
  val catsBundle: Seq[ModuleID] = Seq(catsCore, catsEffect)
}

