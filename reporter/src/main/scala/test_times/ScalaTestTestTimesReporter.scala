package test_times

import java.io.File
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import org.scalatest.Reporter
import org.scalatest.events.Event
import org.scalatest.events.RunCompleted
import org.scalatest.events.RunStarting
import org.scalatest.events.SuiteCompleted
import org.scalatest.events.SuiteStarting
import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters.*

class ScalaTestTestTimesReporter extends Reporter {
  private[this] val suiteStart = new ConcurrentHashMap[String, (Long, SuiteStarting)]()
  private[this] val suiteEnd = new ConcurrentHashMap[String, (Long, SuiteCompleted)]()

  private[this] var filePath: String = null
  private[this] def key: String = "test-time-output-file-path"

  override def apply(event: Event): Unit = event match {
    case e: RunStarting =>
      filePath = e.configMap.getRequired[String](key)
    case e: SuiteStarting =>
      suiteStart.put(e.suiteId, (System.currentTimeMillis(), e))
    case e: SuiteCompleted =>
      suiteEnd.put(e.suiteId, (System.currentTimeMillis(), e))
    case _: RunCompleted =>
      Option(filePath).map(_.trim).filter(_.nonEmpty) match {
        case Some(path) =>
          val values = suiteEnd.asScala
            .flatMap {
              case (id, (endTime, endEvent)) =>
                Option(suiteStart.get(id)).map {
                  case (startTime, startEvent) =>
                    ScalaTestTestTimesReporter.Value(
                      startTime = startTime,
                      endTime = endTime,
                      startEvent = startEvent,
                      endEvent = endEvent
                    )
                }
            }
            .map { x =>
              x.startEvent.suiteId -> (x.endTime - x.startTime)
            }
            .toList
            .sortBy(_.swap)
          val text = values.map { case (x1, x2) => s"${x1} ${x2}" }.mkString("", "\n", "\n")
          val f = new File(path).getCanonicalFile
          f.getParentFile.mkdirs()
          Files.write(f.toPath, text.getBytes(StandardCharsets.UTF_8))
        case None =>
          println(s"[warn] does not provide ${key}!?")
      }
    case _ =>
  }
}

object ScalaTestTestTimesReporter {
  private case class Value(
    startTime: Long,
    endTime: Long,
    startEvent: SuiteStarting,
    endEvent: SuiteCompleted
  )
}
