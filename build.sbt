import Dependencies.*

val scala3Version = "3.7.3"
enablePlugins(JavaAppPackaging)
dockerBaseImage := "openjdk:21"
dockerRepository := Some("ghcr.io/scala-robotics-simulator")
dockerExposedPorts ++= Seq(50051)
dockerCmd := Seq("--rl")

lazy val protobuf = project
  .in(file("protobuf"))
  .settings(
    name := "protobuf",
    scalaVersion := scala3Version,
  )
  .enablePlugins(Fs2Grpc)

lazy val root = project
  .in(file("."))
  .settings(
    name := "PPS-22-srs",
    scalaVersion := scala3Version,
    organization := "io.github.scala-robotics-simulator",
    description := "A robotics simulator written in scala.",
    version := "latest",
    versionScheme := Some("SemVer"),
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
      // "-Ycheck-reentrant",
      "-Xcheck-macros",
      "-rewrite",
      "-indent",
      "-unchecked",
      "-explain",
      "-feature",
      "-language:strictEquality",
      "-language:implicitConversions",
      "-deprecation",
    ),
    coverageEnabled := false,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    wartremoverErrors ++= Warts.all,
    wartremoverErrors --= Seq(
      Wart.DefaultArguments,
      Wart.Equals,
      Wart.Any,
      Wart.IsInstanceOf,
      Wart.ListUnapply,
      Wart.Overloading,
      Wart.Recursion,
      Wart.ImplicitParameter,
      Wart.SeqUpdated,
      Wart.Nothing,
      Wart.Var,
      Wart.ToString,
      Wart.MutableDataStructures,
    ),
    jacocoExcludes := Seq(
      "io.github.srs.view.*",
      "io.github.srs.utils.SimulationDefaults.*",
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
    libraryDependencies ++= loggingBundle,
    libraryDependencies ++= catsBundle,
    libraryDependencies ++= yamlBundle,
    libraryDependencies += parallelCollections,
    libraryDependencies += scalaTestJUnit5,
    libraryDependencies += squidLib,
    libraryDependencies += fs2Io,
    libraryDependencies += scopt,
    libraryDependencies += grpc,
  )
  .enablePlugins(JacocoCoverallsPlugin)
  .dependsOn(protobuf)
  .aggregate(protobuf)

Compile / packageBin / packageOptions +=
  Package.ManifestAttributes(
    "Implementation-Title" -> name.value,
  )

assembly / assemblyMergeStrategy := {
  case PathList("module-info.class") => MergeStrategy.discard
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
