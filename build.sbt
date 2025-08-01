import Dependencies.*

enablePlugins(JacocoCoverallsPlugin)

lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "3.7.2",
    organization := "io.github.scala-robotics-simulator",
    description := "A robotics simulator written in scala.",
    homepage := Some(
      url(
        "https://github.com/Scala-Robotics-Simulator/PPS-22-srs",
      ),
    ),
    licenses := List(
      "MIT" -> url("https://mit-license.org/"),
    ),
    versionScheme := Some("early-semver"),
    developers := List(
      Developer(
        "sceredi",
        "Simone Ceredi",
        "ceredi.simone@gmail.com",
        url("https://github.com/sceredi"),
      ),
      Developer(
        "davidcohenDC",
        "David Cohen",
        "david.cohen@studio.unibo.it",
        url("https://github.com/davidcohenDC"),
      ),
      Developer(
        "GiuliaNardicchia",
        "Giulia Nardicchia",
        "giulia.nardicchia@studio.unibo.it",
        url("https://github.com/GiuliaNardicchia/"),
      ),
    ),
    scalacOptions ++= Seq(
      "-Werror",
      "-Wunused:all",
      "-Wvalue-discard",
      "-Wnonunit-statement",
      "-Yexplicit-nulls",
      "-Wsafe-init",
      "-Ycheck-reentrant",
      "-Xcheck-macros",
      "-rewrite",
      "-indent",
      "-unchecked",
      "-explain",
      "-feature",
      "-language:strictEquality",
      "-language:implicitConversions",
    ),
    coverageEnabled := true,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    wartremoverErrors ++= Warts.all,
    wartremoverErrors --= Seq(
      Wart.DefaultArguments,
      Wart.Equals,
      Wart.Any,
      Wart.IsInstanceOf,
      Wart.ListUnapply,
    ),
    jacocoReportSettings := JacocoReportSettings(
      title = "PR report",
      None,
      JacocoThresholds(),
      formats = Seq(JacocoReportFormats.ScalaHTML, JacocoReportFormats.XML),
      "utf-8",
    ),
    jacocoCoverallsServiceName := "github-actions",
    jacocoCoverallsBranch := sys.env.get("CI_BRANCH"),
    jacocoCoverallsPullRequest := sys.env.get("GITHUB_EVENT_NAME"),
    jacocoCoverallsRepoToken := sys.env.get("COVERALLS_REPO_TOKEN"),

    /*
     * Dependencies
     */
    libraryDependencies ++= scalaTestBundle,
    libraryDependencies += scalaTestJUnit5,
    libraryDependencies += squidLib,
  )
