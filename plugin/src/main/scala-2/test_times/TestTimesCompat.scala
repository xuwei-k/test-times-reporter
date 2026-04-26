package test_times

import sbt.*
import sbt.Keys.{externalDependencyClasspath, scalaBinaryVersion, target}

private[test_times] trait TestTimesCompat { self: TestTimesPlugin.type =>
  import self.autoImport.*

  val hasScalaTestDependency: Def.Initialize[Task[Boolean]] = Def.task(
    (Test / externalDependencyClasspath).value
      .flatMap(_.get(Keys.moduleID.key))
      .exists(m => (m.organization == "org.scalatest") && (m.name == s"scalatest-core_${scalaBinaryVersion.value}"))
  )

  def withJVMPlatform(moduleId: ModuleID): ModuleID = moduleId

  implicit class DefOps(self: Def.type) {
    def uncached[A](a: A): A = a
  }

  val testTimesRootSettings: Seq[Def.Setting[?]] = Def.settings(
    testTimesDirectory := (LocalRootProject / target).value / "test-times",
    testTimesAggregateFile := (LocalRootProject / target).value / "test-times.md"
  )
}
