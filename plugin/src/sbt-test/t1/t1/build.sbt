def scala3 = "3.7.3"

lazy val common = Def.settings(
  scalaVersion := scala3,
  crossScalaVersions := Seq(scala3, "2.13.17")
)

common

lazy val a1 = project
  .settings(
    common,
    libraryDependencies += "org.scalatest" %% "scalatest-freespec" % "3.2.19" % Test
  )

lazy val a2 = project
  .settings(
    common,
    libraryDependencies += "org.scalatest" %% "scalatest-freespec" % "3.2.19" % Test
  )

lazy val a3 = project
  .settings(
    common,
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.18.1" % Test
  )

lazy val a4 = project
  .disablePlugins(TestTimesPlugin)
  .settings(
    common,
    autoScalaLibrary := false
  )

lazy val root = project
  .in(file("."))
  .aggregate(a1, a2, a3, a4)
  .settings(
    InputKey[Unit]("check") := {
      assert((a1 / Test / testOptions).value.nonEmpty)
      assert((a2 / Test / testOptions).value.nonEmpty)
      assert((a3 / Test / testOptions).value.isEmpty)
      assert((a4 / libraryDependencies).value.isEmpty)

      val lines = IO.readLines(testTimesAggregateFile.value)
      assert(lines(0) == "## test times", lines)
      assert(lines(1).isEmpty, lines)
      val values = lines.drop(2).map { l =>
        l.split(" ") match {
          case Array("1.", x2, x3) =>
            val n = x3.toLong
            assert(n >= 0)
            x2 -> n
        }
      }
      val expect = Seq(
        "example.X1",
        "example.X2",
        "example.Y1",
        "example.Y2"
      ).map(_ -> 2).toMap
      val actual = values.map(_._1).groupBy(identity).map { case (k, v) => k -> v.size }
      assert(actual == expect, s"${actual} != ${expect}")
      assert(values.sortBy(-_._2) == values)
    }
  )
