package example

import org.scalatest.freespec.AnyFreeSpec
import scala.util.Random

class Y1 extends AnyFreeSpec {
  "Y1" in {
    Thread.sleep(Random.nextInt(100))
  }
}

class Y2 extends AnyFreeSpec {
  "Y2" in {
    Thread.sleep(Random.nextInt(100))
  }
}
