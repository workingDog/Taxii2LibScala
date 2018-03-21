
name := "taxii2lib"

scalaVersion := "2.12.5"

version := (version in ThisBuild).value

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.6",
  "com.typesafe.play" %% "play-ws-standalone-json" % "1.1.6",
  "com.typesafe.play" %% "play-json" % "2.6.9",
  "com.github.workingDog" %% "scalastix" % "0.7",
 //   "ch.qos.logback" % "logback-classic" % "1.2.3",
 //   "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
  "org.slf4j" % "slf4j-nop" % "1.7.25"
)

organization := "com.github.workingDog"

homepage := Some(url("https://github.com/workingDog/Taxii2LibScala"))

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")

