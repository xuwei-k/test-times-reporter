package example

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object Test3 extends Properties("Test3") {

  property("reverse.reverse") = forAll { (a: String) =>
    a.reverse.reverse == a
  }

}
