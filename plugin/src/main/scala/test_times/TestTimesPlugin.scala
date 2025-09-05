package test_times

import sbt.{given, *}
import sbt.Keys.*
import java.io.File

object TestTimesPlugin extends AutoPlugin with TestTimesCompat {

  override def trigger: PluginTrigger =
    allRequirements

  object autoImport {
    val testTimesCount = settingKey[Int]("")
    val testTimesDirectory = settingKey[File]("")
    val testTimesWriteGitHubStepSummary = taskKey[Unit]("")
    val testTimesAggregateFile = settingKey[File]("")
    val testTimesAggregate = taskKey[String]("")
    val testTimesWrite = taskKey[Unit]("")
  }

  import autoImport.*

  private object AsLong {
    def unapply(x: String): Option[Long] = {
      try {
        Some(x.toLong)
      } catch {
        case _: NumberFormatException =>
          None
      }
    }
  }

  override def globalSettings: Seq[Def.Setting[?]] = Def.settings(
    testTimesRootSettings,
    testTimesWrite := Def.uncached {
      IO.write(
        testTimesAggregateFile.value,
        testTimesAggregate.value
      )
    },
    testTimesAggregate := Def.uncached {
      val log = streams.value.log
      (testTimesDirectory.value ** "*.txt")
        .get()
        .filter(_.isFile)
        .flatMap(IO.readLines(_))
        .flatMap { s =>
          s.split(' ') match {
            case Array(_, AsLong(time)) =>
              Some(s -> time)
            case _ =>
              log.warn(s"invalid line ${s}")
              None
          }
        }
        .sortBy(-_._2)
        .take(testTimesCount.value)
        .map(x => s"1. ${x._1}")
        .mkString("## test times\n\n", "\n", "\n")
    },
    testTimesWriteGitHubStepSummary := Def.uncached {
      val key = "GITHUB_STEP_SUMMARY"
      sys.env
        .get(key)
        .map(file)
        .filter(_.isFile) match {
        case Some(f) =>
          IO.append(
            f,
            testTimesAggregate.value
          )
        case None =>
          streams.value.log.warn(s"not found ${key} file")
      }
    },
    testTimesCount := 50
  )

  private val hasScalaTestDependency: Def.Initialize[Task[Boolean]] = Def.task(
    testExternalDependencyClasspathValue.value
      .exists(
        _.getCanonicalPath.contains(
          Seq("org", "scalatest", s"scalatest-core_${scalaBinaryVersion.value}")
            .mkString(File.separator, File.separator, File.separator)
        )
      )
  )

  override def projectSettings: Seq[Def.Setting[?]] = Def.settings(
    libraryDependencies += {
      "com.github.xuwei-k" %% "scalatest-test-times-reporter" % TestTimesBuildInfo.version % Test
    },
    Test / testOptions ++= {
      if (hasScalaTestDependency.value) {
        val key = "test-time-output-file-path"
        val value = testTimesDirectory.value / scalaVersion.value / s"${thisProject.value.id}.txt"
        Seq(
          Tests.Argument(
            TestFrameworks.ScalaTest,
            "-C",
            "test_times.ScalaTestTestTimesReporter",
            s"-D${key}=${value}"
          )
        )
      } else {
        Nil
      }
    }
  )
}
