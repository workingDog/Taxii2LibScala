
name := "taxii2lib"

scalaVersion := "2.12.8"

version := (version in ThisBuild).value

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.0-RC2",
  "com.typesafe.play" %% "play-ws-standalone-json" % "2.0.0-RC2",
  "com.typesafe.play" %% "play-json" % "2.7.0-RC2",
  "com.github.workingDog" %% "scalastix" % "0.7",
 //   "ch.qos.logback" % "logback-classic" % "1.2.3",
 //   "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
  "org.slf4j" % "slf4j-nop" % "1.7.25"
)

organization := "com.github.workingDog"

homepage := Some(url("https://github.com/workingDog/Taxii2LibScala"))

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")

test in assembly := {}

assemblyMergeStrategy in assembly := {
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".rsa" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".dsa" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".sf" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last.toLowerCase endsWith ".des" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "LICENSES.txt" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "LICENSE.txt" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "logback.xml" => MergeStrategy.discard
  case PathList(xs@_*) if xs.last endsWith "shaded-asynchttpclient-1.1.3.jar" => MergeStrategy.first
  case PathList(xs@_*) if xs.last endsWith "netty-all-4.1.17.Final.jar" => MergeStrategy.first
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assemblyJarName in assembly := "taxii2lib-" + version.value + ".jar"