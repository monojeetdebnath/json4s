import sbt._
import Keys._
import xml.Group
import MimaSettings.mimaSettings
import com.typesafe.tools.mima.plugin.MimaKeys.mimaPreviousArtifacts

object build {
  import Dependencies._

  val manifestSetting = packageOptions += {
    val (title, v, vendor) = (name.value, version.value, organization.value)
      Package.ManifestAttributes(
        "Created-By" -> "Simple Build Tool",
        "Built-By" -> System.getProperty("user.name"),
        "Build-Jdk" -> System.getProperty("java.version"),
        "Specification-Title" -> title,
        "Specification-Version" -> v,
        "Specification-Vendor" -> vendor,
        "Implementation-Title" -> title,
        "Implementation-Version" -> v,
        "Implementation-Vendor-Id" -> vendor,
        "Implementation-Vendor" -> vendor
      )
  }

  val mavenCentralFrouFrou = Seq(
    publishTo := Some(
      if (isSnapshot.value)
        Opts.resolver.sonatypeSnapshots
      else
        Opts.resolver.sonatypeStaging
    ),
    homepage := Some(new URL("https://github.com/json4s/json4s")),
    startYear := Some(2009),
    licenses := Seq(("Apache-2.0", new URL("http://www.apache.org/licenses/LICENSE-2.0"))),
    pomExtra := {
      pomExtra.value ++ Group(
      <scm>
        <url>http://github.com/json4s/json4s</url>
        <connection>scm:git:git://github.com/json4s/json4s.git</connection>
      </scm>
      <developers>
        <developer>
          <id>casualjim</id>
          <name>Ivan Porto Carrero</name>
          <url>http://flanders.co.nz/</url>
        </developer>
        <developer>
          <id>seratch</id>
          <name>Kazuhiro Sera</name>
          <url>http://git.io/sera</url>
        </developer>
      </developers>
      )
    }
  )

  val Scala212 = "2.12.8"

  val json4sSettings = mavenCentralFrouFrou ++ Def.settings(
    organization := "org.json4s",
    scalaVersion := Scala212,
    crossScalaVersions := Seq("2.11.12", Scala212, "2.13.0"),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:existentials", "-language:implicitConversions", "-language:higherKinds"),
    scalacOptions in (Compile, doc) ++= {
      val base = (baseDirectory in LocalRootProject).value.getAbsolutePath
      val hash = sys.process.Process("git rev-parse HEAD").lineStream_!.head
      Seq("-sourcepath", base, "-doc-source-url", "https://github.com/json4s/json4s/tree/" + hash + "€{FILE_PATH}.scala")
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v <= 12 =>
          Seq("-Xfuture")
        case _ =>
          Nil
      }
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 11)) =>
          Seq("-Ywarn-unused-import")
        case _ =>
          Seq("-Ywarn-unused:imports")
      }
    },
    version := "3.7.0-SNAPSHOT",
    javacOptions ++= Seq("-target", "1.8", "-source", "1.8"),
    Seq(Compile, Test).map { scope =>
      unmanagedSourceDirectories in scope += {
        val base = (sourceDirectory in scope).value.getParentFile / Defaults.nameForSrc(scope.name)
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, v)) if v >= 13 =>
            base / s"scala-2.13+"
          case _ =>
            base / s"scala-2.13-"
        }
      }
    },
    parallelExecution in Test := false,
    manifestSetting,
    resolvers ++= Seq(Opts.resolver.sonatypeReleases),
    crossVersion := CrossVersion.binary
  )

  val noPublish = Seq(
    mimaPreviousArtifacts := Set(),
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
}
