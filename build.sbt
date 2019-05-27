name := """mrt-route-planner"""
organization := "com.zendesk"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  guice,
  caffeine,
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % Test,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)