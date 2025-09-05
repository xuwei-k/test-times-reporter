import sbt.*
import sbt.Keys.*

object TestCompat extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val testFull = taskKey[Unit]("")
  }

  import autoImport.*

  override lazy val projectSettings = Seq(
    testFull := (Test / test).value
  )
}
