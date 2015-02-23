import org.scalatest._

class AnotherSpec extends FunSuite with Matchers {

    test("this should take more time") {
        Thread.sleep((Math.random() * 1000 + 100).toInt)
    }

    test("a rather quick test") {
        // nothing ain't slow
    }

    test("i am flaky") {
        Math.random() should be < 0.3
    }


}
