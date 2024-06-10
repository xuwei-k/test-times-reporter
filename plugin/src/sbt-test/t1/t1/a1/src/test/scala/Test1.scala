package example

import org.scalatest.freespec.AnyFreeSpec
import scala.util.Random

class X1 extends AnyFreeSpec {
  "X1" in {
    Thread.sleep(Random.nextInt(100))
  }
}

class X2 extends AnyFreeSpec {
  "X2" in {
    Thread.sleep(Random.nextInt(100))
  }
}
