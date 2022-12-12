package utils

import java.util.*

typealias DebugHandler<N> = (level: Int, nodesOnLevel: Collection<N>, nodesVisited: Collection<N>) -> Boolean

typealias SolutionPredicate<N> = (node: N) -> Boolean

data class AStar<N>(
    val startNode: N,
    val neighborNodes: (N) -> Collection<N>,
    val cost: (N, N) -> Int,
    val costEstimation: (N, N) -> Int,
) {
    private val dist = mutableMapOf(startNode to 0)
    private val prev = mutableMapOf<N, N>()
    private val openList = minPriorityQueueOf(startNode to 0)
    private val closedList = mutableSetOf<N>()

    data class Result<N>(val currentNode: N?, val distanceToStart: Int?, val distance: Map<N, Int>, val prev: Map<N, N>)

    fun search(destNode: N, limitSteps: Int? = null): Result<N> {

        fun expandNode(currentNode: N) {
            for (successor in neighborNodes(currentNode)) {
                if (closedList.contains(successor))
                    continue

                val tentativeDist = dist[currentNode]!! + cost(currentNode, successor)
                if (openList.contains(successor) && tentativeDist >= dist[successor]!!)
                    continue

                prev[successor] = currentNode
                dist[successor] = tentativeDist

                val f = tentativeDist + costEstimation(successor, destNode)
                if (openList.contains(successor)) {
                    openList.decreasePriority(successor, f)
                } else {
                    openList.insert(successor, f)
                }
            }
        }

        if (destNode in dist)
            return Result(destNode, dist[destNode], dist, prev)

        var steps = 0
        while (steps++ != limitSteps && !openList.isEmpty()) {
            val currentNode = openList.extractMin()
            if (currentNode == destNode)
                return Result(destNode, dist[destNode], dist, prev)

            closedList.add(currentNode)
            expandNode(currentNode)
        }

        return openList.peekOrNull().let { Result(it, dist[it], dist, prev) }
    }
}

fun <N> AStar.Result<N>.buildStack(): Stack<N> {
    val pathStack = Stack<N>()
    if (currentNode in distance) {
        var nodeFoundThroughPrevious: N? = currentNode
        while (nodeFoundThroughPrevious != null) {
            pathStack.add(0, nodeFoundThroughPrevious)
            nodeFoundThroughPrevious = prev[nodeFoundThroughPrevious]
        }
    }
    return pathStack
}

class Dijkstra<N>(val neighborNodes: (N) -> Collection<N>, val cost: (N, N) -> Int) {

    fun search(startNode: N, predicate: SolutionPredicate<N>): Pair<N?, Pair<Map<N, Int>, Map<N, N>>> {
        val dist = mutableMapOf<N, Int>()
        val prev = mutableMapOf<N, N>()

        dist[startNode] = 0
        val queue = minPriorityQueueOf(startNode to 0)

        while (!queue.isEmpty()) {
            val u = queue.extractMin()
            if (predicate(u)) {
                return u to (dist to prev)
            }
            for (v in neighborNodes(u)) {
                val alt = dist[u]!! + cost(u, v)
                if (alt < dist.getOrDefault(v, Int.MAX_VALUE)) {
                    dist[v] = alt
                    prev[v] = u
                    queue.insert(v, alt)
                }
            }
        }

        return null to (dist to prev)
    }

}

open class SearchEngineWithEdges<N, E>(
    private val edgesOfNode: (N) -> Iterable<E>,
    private val walkEdge: (N, E) -> N,
) {

    var debugHandler: DebugHandler<N>? = null

    private inner class BfsSearch(val startNode: N, val isSolution: SolutionPredicate<N>) {
        private val nodesVisited = mutableSetOf<N>()
        private val nodesDiscoveredThrough = mutableMapOf<N, N>()

        private tailrec fun searchLevel(nodesOnLevel: Set<N>, level: Int = 0): N? {
            //println("Searching on level $level: ${nodesOnLevel.size} nodes.")
            if (debugHandler?.invoke(level, nodesOnLevel, nodesVisited) == true)
                return null
            val nodesOnNextLevel = mutableSetOf<N>()
            nodesOnLevel.forEach { currentNode ->
                nodesVisited.add(currentNode)
                edgesOfNode(currentNode).forEach { edge ->
                    val node = walkEdge(currentNode, edge)
                    if (!nodesVisited.contains(node) && !nodesOnLevel.contains(node)) {
                        nodesDiscoveredThrough[node] = currentNode
                        if (isSolution(node))
                            return node
                        else
                            nodesOnNextLevel.add(node)
                    }
                }
            }
            return if (nodesOnNextLevel.isEmpty())
                null
            else
                searchLevel(nodesOnNextLevel, level + 1)
        }

        private fun buildStack(node: N?): Stack<N> {
            //println("Building stack for solution node $node")
            val pathStack = Stack<N>()
            var nodeFoundThroughPrevious = node
            while (nodeFoundThroughPrevious != null) {
                pathStack.add(0, nodeFoundThroughPrevious)
                nodeFoundThroughPrevious = nodesDiscoveredThrough[nodeFoundThroughPrevious]
            }
            return pathStack
        }

        fun search() = buildStack(if (isSolution(startNode)) startNode else searchLevel(setOf(startNode)))

    }

    private inner class DepthSearch(val startNode: N, val isSolution: SolutionPredicate<N>) {

        private val stack = Stack<N>()
        private val nodesVisited = mutableSetOf<N>()
        private val nodesDiscoveredThrough = mutableMapOf<N, N>()

        private fun searchFrom(node: N, isSolution: SolutionPredicate<N>): N? {
            if (isSolution(node))
                return node
            nodesVisited.add(node)
            val edges = edgesOfNode(node)
            for (edge in edges) {
                val nextNode = walkEdge(node, edge)
                if (!nodesVisited.contains(nextNode)) {
                    nodesDiscoveredThrough[nextNode] = node
                    val found = searchFrom(nextNode, isSolution)
                    if (found != null)
                        return found
                }
            }
            return null
        }

        private fun buildStack(node: N?): Stack<N> {
            //println("Building stack for solution node $node")
            val pathStack = Stack<N>()
            var nodeFoundThroughPrevious = node
            while (nodeFoundThroughPrevious != null) {
                pathStack.add(0, nodeFoundThroughPrevious)
                nodeFoundThroughPrevious = nodesDiscoveredThrough[nodeFoundThroughPrevious]
            }
            return pathStack
        }

        fun search() = buildStack(searchFrom(startNode, isSolution))

    }

    fun bfsSearch(startNode: N, isSolution: SolutionPredicate<N>): Stack<N> {
        return BfsSearch(startNode, isSolution).search()
    }

    fun depthFirstSearch(startNode: N, isSolution: SolutionPredicate<N>): Stack<N> {
        return DepthSearch(startNode, isSolution).search()
    }

    fun completeAcyclicTraverse(startNode: N): Sequence<Pair<Int, Set<N>>> =
        sequence {
            var nodesOnPreviousLevel: Set<N>
            var nodesOnLevel = setOf<N>()
            var nodesOnNextLevel = setOf(startNode)
            var level = 0
            while (nodesOnNextLevel.isNotEmpty()) {
                nodesOnPreviousLevel = nodesOnLevel
                nodesOnLevel = nodesOnNextLevel
                yield(level++ to nodesOnLevel)
                nodesOnNextLevel = mutableSetOf()
                nodesOnLevel.forEach { node ->
                    nodesOnNextLevel.addAll(edgesOfNode(node).map { e -> walkEdge(node, e) }
                        .filter { neighbor ->
                            !nodesOnPreviousLevel.contains(neighbor) &&
                                    !nodesOnLevel.contains(neighbor)
                        })
                }
            }
        }

}

class SearchEngineWithNodes<N>(neighborNodes: (N) -> Collection<N>) :
    SearchEngineWithEdges<N, N>(neighborNodes, { _, edge -> edge })

fun <N, E> breadthFirstSearch(
    startNode: N,
    edgesOf: (N) -> Collection<E>,
    walkEdge: (N, E) -> N,
    isSolution: SolutionPredicate<N>,
) =
    SearchEngineWithEdges(edgesOf, walkEdge).bfsSearch(startNode, isSolution)

fun <N> breadthFirstSearch(
    startNode: N,
    neighborNodes: (N) -> Collection<N>,
    isSolution: SolutionPredicate<N>,
): Stack<N> =
    SearchEngineWithNodes(neighborNodes).bfsSearch(startNode, isSolution)

fun <N> depthFirstSearch(
    startNode: N,
    neighborNodes: (N) -> Collection<N>,
    isSolution: SolutionPredicate<N>,
): Stack<N> =
    SearchEngineWithNodes(neighborNodes).depthFirstSearch(startNode, isSolution)

fun loggingDebugger(level: Int, nodesOnLevel: Collection<Any>, nodesVisited: Collection<Any>): Boolean {
    println("I am on level $level, searching through ${nodesOnLevel.size}. Visited so far: ${nodesVisited.size}")
    return false
}
