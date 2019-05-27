package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.Helpers.{GET, contentAsString, status, _}
import play.api.test.{FakeRequest, Injecting}


class RouteControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "RouteController GET" should {

    "respond with bad request when the inputs stations codes are invalid" in {
      val controller = inject[RouteController]
      val route = controller.route("sd", "sdsd").apply(FakeRequest(GET, "/"))

      status(route) mustBe BAD_REQUEST
      contentAsString(route) must include("try again")
    }

    "respond with Ok when the inputs stations are valid" in {
      val controller = inject[RouteController]
      val route = controller.route("ne1", "ne3").apply(FakeRequest(GET, "/"))

      status(route) mustBe OK
      contentAsString(route) must include("Take ne line from HarbourFront to Outram Park")
    }

  }
}
