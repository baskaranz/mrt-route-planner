# mrt-route-planner
A service which provides the optimal shortest path between two MRT stations

## Problem Statement


You are provided data on the stations and lines of Singapore’s urban rail system, including planned additions over the next few years. Your task is to use this data to build a routing service, to help users find routes from any station to any other station on this future network.


The app should expose an API to find and display one or more routes from a specified origin to a specified destination, ordered by some efficiency heuristic. Routes should have one or more steps, like “Take [line] from [station] to [station]” or “Change to [line]”. You may add other relevant information to the results.


For the line names to be displayed, using the two-letter code is sufficient.


You may use any language/framework. You may also convert the data into another format as needed.

## Implementation
### Language/Framework
	- Play/Scala
### Solution overview
	The service uses `dijkstra algorithm` to suggest the optimal path between two given stations.
	The stations CSV file is used to build the nodes/edges for the graph and loaded into in-memory cache while application startup.

## Using the service

### Follow the steps below
- git clone https://github.com/baskaranz/mrt-route-planner.git
- `sbt clean test` to run the tests
- `sbt run` to run (start) the project in dev mode
### Solution endpoints
- Find the routes for a given `from` and `to` station codes
- GET    /route
- example: `http://localhost:9000/route?from=EW27&to=DT12`
