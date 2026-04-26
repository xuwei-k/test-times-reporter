package test_times

import sbt./
import sbt.Def
import sbt.File
import sbt.Keys
import sbt.Task
import sbt.Test
import sbt.given
import sbt.Classpaths
import sbt.Keys.{externalDependencyClasspath, fileConverter, scalaBinaryVersion}
import sbt.ModuleID
import sbt.Platform

private[test_times] trait TestTimesCompat { self: TestTimesPlugin.type =>
  import self.autoImport.*

  val hasScalaTestDependency: Def.Initialize[Task[Boolean]] = Def.task(
    (Test / externalDependencyClasspath).value
      .flatMap(_.get(Keys.moduleIDStr))
      .map(Classpaths.moduleIdJsonKeyFormat.read)
      .exists(m =>
        (m.organization == "org.scalatest") && (m.name == s"scalatest-core_${scalaBinaryVersion.value}") && m.platformOpt
          .forall(Platform.jvm == _)
      )
  )

  def withJVMPlatform(moduleId: ModuleID): ModuleID = moduleId.platform(Platform.jvm)

  val testTimesRootSettings: Seq[Def.Setting[?]] = Def.settings(
    testTimesDirectory := sbt.Keys.rootOutputDirectory.value.toFile / "test-times",
    testTimesAggregateFile := sbt.Keys.rootOutputDirectory.value.toFile / "test-times.md"
  )
}
