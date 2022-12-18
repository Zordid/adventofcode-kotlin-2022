@file:Suppress("unused")

package utils

import java.util.*

interface Graph<N> {
    fun neighborsOf(node: N): Collection<N>

    fun cost(from: N, to: N): Int = throw NotImplementedError("needs a cost fun")
    fun costEstimation(from: N, to: N): Int = throw NotImplementedError("needs a costEstimation fun")
}

fun <N> Graph<N>.depthFirstSearch(start: N, destinationPredicate: (N) -> Boolean): Stack<N> =
    depthFirstSearch(start, ::neighborsOf, destinationPredicate)

fun <N> Graph<N>.depthFirstSearch(start: N, destination: N): Stack<N> =
    depthFirstSearch(start, ::neighborsOf) { it == destination }

fun <N> Graph<N>.completeAcyclicTraverse(start: N) =
    SearchEngineWithNodes(::neighborsOf).completeAcyclicTraverse(start)

fun <N> Graph<N>.breadthFirstSearch(start: N, predicate: SolutionPredicate<N>) =
    SearchEngineWithNodes(::neighborsOf).bfsSearch(start, predicate)

fun <N> Graph<N>.aStarSearch(
    start: N,
    destination: N,
    cost: (N, N) -> Int = this::cost,
    costEstimation: (N, N) -> Int = this::costEstimation,
) =
    AStarSearch(start, neighborNodes = ::neighborsOf, cost = cost, costEstimation = costEstimation).search(destination)

fun <N> Graph<N>.dijkstraSearch(start: N, destination: N) =
    Dijkstra(start, ::neighborsOf, ::cost).search { it == destination }

fun <N> Graph<N>.dijkstraSearch(start: N, destinationPredicate: (N) -> Boolean) =
    Dijkstra(start, ::neighborsOf, ::cost).search(destinationPredicate)