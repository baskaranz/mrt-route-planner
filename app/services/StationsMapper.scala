package services

import com.typesafe.scalalogging.Logger
import javax.inject.{Inject, Singleton}
import models.{Edge, Station}
import org.slf4j.LoggerFactory
import play.api.cache.SyncCacheApi

import scala.collection.mutable.ListBuffer
import scala.util.Try

@Singleton
class StationsMapper @Inject()(cache: SyncCacheApi) {

  val logger = Logger(LoggerFactory.getLogger(classOf[StationsMapper].getName))

  loadData

  def loadData = {
    try {
      val edges = scala.collection.mutable.Map[String, ListBuffer[String]]()
      val weights = scala.collection.mutable.Map[(String, String), Int]()

      val stations = scala.collection.mutable.ListBuffer[Station]()
      val stationsMap = scala.collection.mutable.Map[String, String]()

      val bufferedSource = scala.io.Source.fromFile("resources/StationMap.csv")
      var i = 0
      for (line <- bufferedSource.getLines.drop(1)) {
        val cols = line.split(",").map(_.trim)
        stations.+=(Station(cols(0).toLowerCase, cols(1), i))
        stationsMap.+=(cols(0).toLowerCase -> cols(1))
        i += 1
      }
      bufferedSource.close

      val sortedStations = stations.sortBy(_.counter)

      val stationsCountMap = stations.toList.groupBy(_.stationName).mapValues(_.size)

      for (i <- 0 to sortedStations.size - 1) {
        Try(
          if (sortedStations(i).stationCode.slice(0, 2) == sortedStations(i + 1).stationCode.slice(0, 2)) {
            addEdges(edges, weights, Edge(sortedStations(i).stationCode, sortedStations(i + 1).stationCode, 2))
          }).toOption
        Try(
          if (stationsCountMap(sortedStations(i).stationName) > 1) {
            val filteredList = sortedStations.filter(_.stationName.equals(sortedStations(i).stationName))
            for (item <- filteredList.combinations(2).toList) {
              addEdges(edges, weights, Edge(item(0).stationCode, item(1).stationCode, 1))
            }
          }
        ).toOption
      }

      cache.set("edges", edges)
      cache.set("weights", weights)
      cache.set("stations_map", stationsMap)
    } catch {
      case e: Exception =>
      logger.error("Exception while loading station data in cache:", e)
    }
  }

  private def addEdges(edges: scala.collection.mutable.Map[String, ListBuffer[String]], weights: scala.collection.mutable.Map[(String, String), Int], edge: Edge): (scala.collection.mutable.Map[String, ListBuffer[String]], scala.collection.mutable.Map[(String, String), Int]) = {
    Try(edges(edge.fromNode) = edges.get(edge.fromNode).get.+=(edge.toNode)).getOrElse(edges(edge.fromNode) = ListBuffer(edge.toNode))
    Try(edges(edge.toNode) = edges.get(edge.toNode).get.+=(edge.fromNode)).getOrElse(edges(edge.toNode) = ListBuffer(edge.fromNode))
    weights((edge.fromNode, edge.toNode)) = edge.weight
    weights((edge.toNode, edge.fromNode)) = edge.weight
    (edges, weights)
  }

  def main(args: Array[String]): Unit = {
    loadData
  }
}
