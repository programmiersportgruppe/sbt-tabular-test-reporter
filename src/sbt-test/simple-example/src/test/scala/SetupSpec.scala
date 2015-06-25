import org.scalatest._

class SetupSpec extends FunSuite with Matchers with  BeforeAndAfterAll with BeforeAndAfter {

    override def beforeAll() = { Thread.sleep(3200) }

    override def afterAll() = { Thread.sleep(1600); throw new RuntimeException(null: String) }

    before { Thread.sleep(800) }

    after { Thread.sleep(200) }

    test("test should take approximately 400 ms") { Thread.sleep(400) }

    test("test should take approximately 100 ms") { Thread.sleep(100) }

}
