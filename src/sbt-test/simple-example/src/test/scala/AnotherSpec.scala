import org.scalatest._

class AnotherSpec extends FunSuite with Matchers {

    test("this should take more time") {
        Thread.sleep(300)
    }

    test("a rather quick test") {
        // nothing ain't slow
    }

}
