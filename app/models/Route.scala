package models

case class Edge(fromNode: String, toNode: String, weight: Int)

case class Station(stationCode: String, stationName: String, counter: Int)
