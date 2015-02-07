import org.scalatest._

class ExampleSpec extends FunSuite with Matchers {

  test("should pass") {
    // successful nothingness
  }


  test("failure should be reported") {
    "A" should be ("B")
  }

  test("errors should be reported") {
    throw new RuntimeException("My error")
  }

  test("test should take approximately 3 seconds") {
    Thread.sleep(3000)
  }

  test("test should take approximately 1 second") {
    Thread.sleep(1000)
  }

  ignore("this should be ignored") {
    // nothing again
  }

}
