import sbt.*

object Dependencies {
  /*
   * Versions
   */
  private lazy val scalaTestVersion = "3.2.19"

  /*
   * Libraries
   */
  private val scalaTest = "org.scalactic" %% "scalactic" % scalaTestVersion
  private val scalactic =
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  val scalaTestJUnit5 =
    "org.scalatestplus" %% "junit-5-10" % "3.2.19.1" % "test"

  /*
   * Bundles
   */
  val scalaTestBundle = Seq(scalaTest, scalactic)
}

