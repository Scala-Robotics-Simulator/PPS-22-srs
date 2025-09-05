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
  val catsEffect = "org.typelevel" %% "cats-effect" % "3.7-4972921"
  val scalaYaml = "org.virtuslab" %% "scala-yaml" % "0.3.0"
  val circeYaml = "io.circe" %% "circe-yaml" % "1.15.0"
  val circeGeneric = "io.circe" %% "circe-generic" % "0.14.14"
  val fs2Io = "co.fs2" %% "fs2-io" % "3.12.2"
  val parallelCollections = "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0"
  val scopt = "com.github.scopt" %% "scopt" % "4.1.0"
  /*
   * Bundles
   */
  val scalaTestBundle: Seq[ModuleID] = Seq(scalaTest, scalactic)
  val catsBundle: Seq[ModuleID] = Seq(catsCore, catsEffect)
  val yamlBundle: Seq[ModuleID] = Seq(scalaYaml, circeYaml, circeGeneric)
}

