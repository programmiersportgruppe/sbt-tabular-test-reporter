import org.scalatest._

class AfterAllExceptionSpec extends FunSuite with Matchers with  BeforeAndAfterAll with BeforeAndAfter {

    override def beforeAll() = { Thread.sleep(3200) }

    override def afterAll() = { Thread.sleep(1600); throw new RuntimeException(null: String) }

    before { Thread.sleep(800) }

    after { Thread.sleep(200) }

    test("test should take approximately 400 ms") { Thread.sleep(400) }

    test("test should take approximately 100 ms") { Thread.sleep(100) }

}

class AfterExceptionSpec extends FunSuite with Matchers with  BeforeAndAfterAll with BeforeAndAfter {

    after { throw new RuntimeException(null: String) }

    test("This could succeed") { }

}

class BeforeAllExceptionSpec extends FunSuite with Matchers with  BeforeAndAfterAll with BeforeAndAfter {

    override def beforeAll() = { throw new RuntimeException(null: String) }

    test("This could succeed") { }
}

class BeforeExceptionSpec extends FunSuite with Matchers with  BeforeAndAfterAll with BeforeAndAfter {

    before { throw new RuntimeException(null: String) }

    test("This could succeed") { }
}

class ConstructorThrowsSpec extends FunSuite with Matchers with  BeforeAndAfterAll with BeforeAndAfter {

    throw new RuntimeException(null: String)

    test("This could succeed") { }
}
