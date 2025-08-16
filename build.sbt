import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

def Scala212 = "2.12.20"

lazy val commonSettings = Def.settings(
  publishTo := (if (isSnapshot.value) None else localStaging.value),
  scalacOptions ++= Seq("-deprecation", "-feature", "-language:implicitConversions"),
  Compile / doc / scalacOptions ++= {
    val hash = sys.process.Process("git rev-parse HEAD").lineStream_!.head
    if (scalaBinaryVersion.value != "3") {
      Seq(
        "-sourcepath",
        (LocalRootProject / baseDirectory).value.getAbsolutePath,
        "-doc-source-url",
        s"https://github.com/xuwei-k/test-times-reporter/blob/${hash}€{FILE_PATH}.scala"
      )
    } else {
      Nil
    }
  },
  pomExtra := (
    <developers>
      <developer>
        <id>xuwei-k</id>
        <name>Kenji Yoshida</name>
        <url>https://github.com/xuwei-k</url>
      </developer>
    </developers>
      <scm>
        <url>git@github.com:xuwei-k/test-times-reporter.git</url>
        <connection>scm:git:git@github.com:xuwei-k/test-times-reporter.git</connection>
      </scm>
  ),
  organization := "com.github.xuwei-k",
  homepage := Some(url("https://github.com/xuwei-k/test-times-reporter")),
  licenses := List(
    "MIT License" -> url("https://opensource.org/licenses/mit-license")
  ),
  scalacOptions ++= {
    scalaBinaryVersion.value match {
      case "2.13" | "3" =>
        Seq("-Wconf:msg=JavaConverters:silent")
      case _ =>
        Nil
    }
  },
  scalacOptions ++= {
    scalaBinaryVersion.value match {
      case "2.13" =>
        Seq("-Xsource:3-cross")
      case "2.12" =>
        Seq("-Xsource:3")
      case _ =>
        Nil
    }
  }
)

lazy val reporter = projectMatrix
  .in(file("reporter"))
  .defaultAxes()
  .settings(
    commonSettings,
    name := "scalatest-test-times-reporter",
    libraryDependencies += "org.scalatest" %% "scalatest-core" % "3.2.19" % Provided
  )
  .jvmPlatform(scalaVersions = Seq(Scala212, "2.13.16", "3.3.6"))

lazy val plugin = projectMatrix
  .in(file("plugin"))
  .defaultAxes()
  .enablePlugins(SbtPlugin)
  .jvmPlatform(scalaVersions = Seq(Scala212, "3.7.2"))
  .settings(
    commonSettings,
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" =>
          sbtVersion.value
        case _ =>
          "2.0.0-RC3"
      }
    },
    scriptedBufferLog := false,
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
    Compile / sourceGenerators += task {
      val src = s"""|package test_times
                    |
                    |private[test_times] object TestTimesBuildInfo {
                    |  def version: String = "${version.value}"
                    |}
                    |""".stripMargin
      val f = (Compile / sourceManaged).value / "TestTimesBuildInfo.scala"
      IO.write(f, src)
      Seq(f)
    },
    name := "test-times-plugin"
  )

commonSettings

publish / skip := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("publishSigned"),
  releaseStepCommandAndRemaining("sonaRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
