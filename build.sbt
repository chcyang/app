import sbt.Keys._

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "app",
    organization := "io.github.chc",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Seq(
      guice,
      ws, // Calling REST APIs with Play WS
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )

