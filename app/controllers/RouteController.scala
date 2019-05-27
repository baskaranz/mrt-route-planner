package controllers

import javax.inject._
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import services.RoutingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * route suggestion controller.
  */
@Singleton
class RouteController @Inject()(cc: ControllerComponents)(cache: AsyncCacheApi, routingService: RoutingService) extends AbstractController(cc) {

  def route(from: String, to: String): Action[AnyContent] = Action.async {
    (routingService.validateStationCodes(from.toLowerCase), routingService.validateStationCodes(to.toLowerCase)) match {
      case (true, true) =>
        routingService.suggestRoute(from.toLowerCase, to.toLowerCase) map {
          case Some(route) => Ok(Json.toJson(Json.obj("route" -> route)))
          case None => Ok("No route available for the given stations")
        } recover {
          case _: Throwable =>
            InternalServerError
        }
      case _ =>
        Future.successful(BadRequest("Invalid station code(s), please check and try again"))
    }
  }
}


