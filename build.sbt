name := "trifinity"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "io.reactivex" %% "rxscala" % "0.25.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.14",
  "com.typesafe.akka" %% "akka-http-core-experimental" % "1.0-RC4",
  "com.typesafe.play" %% "play-json" % "2.4.0-M3",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)