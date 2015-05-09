name := """cbi-flag-conditions"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
    jdbc,
    anorm,
    cache,
    ws,
    "postgresql"         % "postgresql"    % "9.1-901-1.jdbc4",
    "com.typesafe.slick" %% "slick" % "2.1.0",
    "com.typesafe.play" %% "play-slick" % "0.8.1",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-RC2"
)
