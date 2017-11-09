
name := "taxii2lib"

version := "0.1"

scalaVersion := "2.12.3"

version := (version in ThisBuild).value

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.2",
  "com.typesafe.play" %% "play-ws-standalone-json" % "1.1.2",
  "com.typesafe.play" %% "play-json" % "2.6.6",
  "com.github.workingDog" %% "scalastix" % "0.6-SNAPSHOT",
  "org.slf4j" % "slf4j-nop" % "1.7.25"
)

organization := "com.github.workingDog"

homepage := Some(url("https://github.com/workingDog/taxii2LibScala"))

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

scalacOptions ++= Seq(
  //  "-Ypartial-unification", // to improves type constructor inference
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xlint" // Enable recommended additional warnings.
)
