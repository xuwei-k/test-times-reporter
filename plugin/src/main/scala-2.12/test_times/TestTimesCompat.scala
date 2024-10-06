package test_times

import sbt.*
import sbt.Keys.externalDependencyClasspath

private[test_times] trait TestTimesCompat { self: TestTimesPlugin.type =>

  val testExternalDependencyClasspathValue: Def.Initialize[Task[Seq[File]]] = Def.task {
    (Test / externalDependencyClasspath).value.map(_.data)
  }

}
