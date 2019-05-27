package services

import com.typesafe.scalalogging.Logger
import javax.inject.Inject
import org.slf4j.LoggerFactory
import play.api.cache.SyncCacheApi

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class RoutingService @Inject()(cache: SyncCacheApi) {

  val logger = Logger(LoggerFactory.getLogger(classOf[RoutingService].getName))

  val maybeEdges: Option[scala.collection.mutable.Map[String, ListBuffer[String]]] = cache.get("edges")
  val maybeWeights: Option[scala.collection.mutable.Map[(String, String), Int]] = cache.get("weights")
  val maybeStationsMap: Option[scala.collection.mutable.Map[String, String]] = cache.get("stations_map")


  /** Function to return the suggested route for the given station codes.
    *
    *  @param fromStation the from/source station code
    *  @param toStation the to/destination station code
    *  @return Async list of suggested route
    */
  def suggestRoute(fromStation: String, toStation: String): Future[Option[List[String]]] = Future {

    (maybeEdges, maybeWeights) match {
      case (Some(edges), Some(weights)) =>
        try {
          val shortestPaths = scala.collection.mutable.Map[String, (String, Int)]()
          shortestPaths(fromStation) = (null, 0)
          var currentNode = fromStation
          val visited = scala.collection.mutable.Set[String]()
          var i = 0
          while (currentNode != toStation) {
            i = i + 1
            visited += currentNode
            val destinations = edges(currentNode)
            val weightToCurrentNode = shortestPaths(currentNode)._2
            for (nextNode <- destinations) {
              val weight = weights((currentNode, nextNode)) + weightToCurrentNode
              if (!shortestPaths.contains(nextNode)) {
                shortestPaths(nextNode) = (currentNode, weight)
              } else {
                val currentShortestWeight = shortestPaths(nextNode)._2
                if (currentShortestWeight > weight) {
                  shortestPaths(nextNode) = (currentNode, weight)
                }
              }
            }
            val nextDestinations = scala.collection.mutable.Map[String, (String, Int)]()
            for (node <- shortestPaths.keys) {
              if (!visited.contains(node)) {
                nextDestinations(node) = (shortestPaths(node)._1, shortestPaths(node)._2)
              }
            }
            if (nextDestinations.size == 0) {
              logger.info("Route Not Possible")
            }
            currentNode = nextDestinations.toList.sortBy(_._2._2).head._1
            currentNode
          }
          val path = ListBuffer[String]()
          while (currentNode != null) {
            path += currentNode
            val nextNode = shortestPaths(currentNode)._1
            currentNode = nextNode
          }
          val newPath = path.reverse.toList
          buildResponse(newPath)
        } catch {
          case e: Exception =>
            logger.error("Exception while suggesting the route", e)
            None
        }
      case _ =>
        None
    }
  }

  /** Function to validate the station codes.
    *
    *  @param stationCode the station code
    *  @return Boolean value of whether if the station is valid
    */
  def validateStationCodes(stationCode: String): Boolean = {
    Try(maybeStationsMap.get.get(stationCode)) match {
      case Success(maybeStation) => maybeStation.isDefined
      case Failure(_) => false
    }
  }

  /** Helper function to build the response for the controller.
    *
    *  @param route the list of suggested station codes.
    *  @return the list of suggested route description with station names.
    */
  private def buildResponse(route: List[String]): Option[List[String]] = {
    try {
      var routeWithDesc = scala.collection.mutable.ListBuffer[String]()
      val stationCodeLineMap = route.map(f => (f.slice(0, 2), f))
      for (i <- 0 to stationCodeLineMap.size - 2) {
        val startRoute = stationCodeLineMap(i)
        val endRoute = stationCodeLineMap(i + 1)
        if (startRoute._1 == endRoute._1) {
          routeWithDesc.+=(s"Take ${startRoute._1} line from ${getStationName(startRoute._2)} to ${getStationName(endRoute._2)}")
        } else {
          routeWithDesc.+=(s"Change from ${startRoute._1} line to ${endRoute._1} line")
        }
      }
      Try {
        if (routeWithDesc.last.contains("Change from") && routeWithDesc.head.contains("Change from")) {
          routeWithDesc = routeWithDesc.slice(1, routeWithDesc.size - 2)
        }
        if (routeWithDesc.last.contains("Change from")) {
          routeWithDesc = routeWithDesc.drop(1).dropRight(1)
        }
        if (routeWithDesc.head.contains("Change from")) {
          routeWithDesc = routeWithDesc.slice(1, routeWithDesc.size - 1)
        }
        routeWithDesc.toList
      }.toOption
    } catch {
      case e: Exception =>
        logger.error("Exception while building response: ", e)
        None
    }
  }

  private def getStationName(stationCode: String): String = {
    Try(maybeStationsMap.get(stationCode)).getOrElse(stationCode)
  }
}
