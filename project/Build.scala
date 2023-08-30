import com.github.retronym.SbtOneJar
import sbt._
import Keys._

object Build extends Build {

  lazy val project = Project("root", file("."), settings = Seq(
    name := "postgresql-to-sqlite",
    organization := "com.github.caiiiycuk",
    version := "1.0.2",
    scalaVersion := "2.11.12",

    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "3.3.0",
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "org.xerial" % "sqlite-jdbc" % "3.42.0.0",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
  ) ++ SbtOneJar.oneJarSettings)

}
