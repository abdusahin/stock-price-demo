name := "stock-price-demo"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

lazy val stockPrice = (project in file("."))