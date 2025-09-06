package test_times

import sbt./
import sbt.Def
import sbt.File
import sbt.Keys
import sbt.Task
import sbt.Test
import sbt.given
import sbt.Classpaths
import sbt.Keys.{externalDependencyClasspath, fileConverter}
import sbt.ModuleID

private[test_times] trait TestTimesCompat { self: TestTimesPlugin.type =>
  import self.autoImport.*

  val testExternalDependencyClasspathValue: Def.Initialize[Task[Seq[ModuleID]]] = Def.task {
    (Test / externalDependencyClasspath).value
      .flatMap(_.get(Keys.moduleIDStr))
      .map(Classpaths.moduleIdJsonKeyFormat.read)
  }

  val testTimesRootSettings: Seq[Def.Setting[?]] = Def.settings(
    testTimesDirectory := sbt.Keys.rootOutputDirectory.value.toFile / "test-times",
    testTimesAggregateFile := sbt.Keys.rootOutputDirectory.value.toFile / "test-times.md"
  )
}
