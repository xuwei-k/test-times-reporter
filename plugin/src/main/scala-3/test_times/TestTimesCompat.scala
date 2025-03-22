package test_times

import sbt./
import sbt.Def
import sbt.File
import sbt.Task
import sbt.Test
import sbt.given
import sbt.Keys.{externalDependencyClasspath, fileConverter}

private[test_times] trait TestTimesCompat { self: TestTimesPlugin.type =>

  val testExternalDependencyClasspathValue: Def.Initialize[Task[Seq[File]]] = Def.task {
    val converter = fileConverter.value
    (Test / externalDependencyClasspath).value.map(x => converter.toPath(x.data).toFile)
  }

}
