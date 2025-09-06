package test_times

import sbt.*
import sbt.Keys.externalDependencyClasspath
import sbt.Keys.target

private[test_times] trait TestTimesCompat { self: TestTimesPlugin.type =>
  import self.autoImport.*

  val testExternalDependencyClasspathValue: Def.Initialize[Task[Seq[ModuleID]]] = Def.task {
    (Test / externalDependencyClasspath).value.flatMap(_.get(Keys.moduleID.key))
  }

  implicit class DefOps(self: Def.type) {
    def uncached[A](a: A): A = a
  }

  val testTimesRootSettings: Seq[Def.Setting[?]] = Def.settings(
    testTimesDirectory := (LocalRootProject / target).value / "test-times",
    testTimesAggregateFile := (LocalRootProject / target).value / "test-times.md"
  )
}
