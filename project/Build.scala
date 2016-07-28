import sbt.Keys._
import sbt._

object Build extends Build {

  override lazy val settings = super.settings :+ {
    shellPrompt := (s => Project.extract(s).currentProject.id + " > ")
  }

  lazy val root = Project("akka-microservice-starter", file("."))
    .settings(
      description := "Akka Microservice Starter",
      version := "1.0.0",
      homepage := Some(new URL("https://github.com/yangbajing/scala-microservice-starter")),
      organization := "me.yangbajing",
      organizationHomepage := Some(new URL("https://github.com/yangbajing/scala-microservice-starter")),
      startYear := Some(2016),
      scalaVersion := "2.11.8",
      scalacOptions ++= Seq(
        "-encoding", "utf8",
        "-unchecked",
        "-feature",
        "-deprecation"
      ),
      javacOptions ++= Seq(
        "-encoding", "utf8",
        "-Xlint:unchecked",
        "-Xlint:deprecation"
      ),
      javaOptions += "-Dproject.base=" + baseDirectory.value,
      publish := (),
      publishLocal := (),
      publishTo := None,
      offline := true,
      resolvers ++= Seq(
        "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
        "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases",
        "ERI OSS" at "http://dl.bintray.com/elderresearch/OSS",
        "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
        "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"),
      libraryDependencies ++= Seq(
        _ssc,
        _asyncHttpClient,
        _json4sJackson,
        _akkaHttp,
        _akkaActor,
        _akkaSlf4j,
        _guice,
        _logback,
        _typesafeConfig,
        _scalaReflect,
        _scalaLogging,
        _scalatest))

  val _scalaReflect = "org.scala-lang" %  "scala-reflect"  % "2.11.8"
  val _scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
  val _scalatest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  val _typesafeConfig = "com.typesafe" % "config" % "1.3.0"

  val _scalaLogging = ("com.typesafe.scala-logging" %% "scala-logging" % "3.4.0").exclude("org.scala-lang", "scala-library").exclude("org.scala-lang", "scala-reflect")

  val verAkka = "2.4.8"
  val _akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % verAkka
  val _akkaActor = "com.typesafe.akka" %% "akka-actor" % verAkka
  val _akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % verAkka

  val _ssc = "com.elderresearch" %% "ssc" % "0.2.0"

  val _guice = "com.google.inject" % "guice" % "4.1.0"

  val _redisclient = "net.debasishg" %% "redisclient" % "3.0"

  val _json4sJackson = "org.json4s" %% "json4s-jackson" % "3.3.0"

  val _mongoScala = "org.mongodb.scala" %% "mongo-scala-driver" % "1.0.0"

  val _asyncHttpClient = "org.asynchttpclient" % "async-http-client" % "2.0.10"

  val _logback = "ch.qos.logback" % "logback-classic" % "1.1.3"
  val _commonsEmail = "org.apache.commons" % "commons-email" % "1.4"
  val _guava = "com.google.guava" % "guava" % "19.0"

}

