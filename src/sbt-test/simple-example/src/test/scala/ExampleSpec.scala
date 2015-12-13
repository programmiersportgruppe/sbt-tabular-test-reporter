import org.scalatest._

class ExampleSpec extends FunSuite with Matchers {

  test("should pass") {
    // successful nothingness
  }


  test("failure should be reported") {
    "A" should be ("B")
  }

  test("failure with fancy characters") {
    "ç" should be ("Ä")
                                     }

  test("errors should be reported") {
    throw new RuntimeException("My error\nWith a second line.   ")
  }

  test("test should take approximately 2 seconds") {
    Thread.sleep(2000)
  }

  test("test should take approximately 0.5 seconds") {
    Thread.sleep(500)
  }

  ignore("this should be ignored") {
    // nothing again
  }

}
