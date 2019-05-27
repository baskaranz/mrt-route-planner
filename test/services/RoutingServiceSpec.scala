package services

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSuite, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Injecting

class RoutingServiceSpec extends FunSuite with Matchers with ScalaFutures with GuiceOneAppPerTest with Injecting {

  test("A route response should be returned to a station code requests") {
    val routingService = inject[RoutingService]
    whenReady(routingService.suggestRoute("ne1", "ne3")) { result =>
      result shouldBe Some(List("Take ne line from HarbourFront to Outram Park"))
    }
  }
}
