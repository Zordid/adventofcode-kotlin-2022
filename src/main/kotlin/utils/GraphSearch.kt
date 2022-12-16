package utils

import java.util.*
import kotlin.collections.ArrayDeque

enum class SearchControl { STOP, CONTINUE }
typealias DebugHandler<N> = (level: Int, nodesOnLevel: Collection<N>, nodesVisited: Collection<N>) -> SearchControl

typealias SolutionPredicate<N> = (node: N) -> Boolean

data class SearchResult<N>(val currentNode: N?, val distance: Map<N, Int>, val prev: Map<N, N>) {
    val distanceToStart: Int? = currentNode?.let { distance[it] }
    val steps: Int? by lazy { (path.size - 1).takeIf { it >= 0 } }
    val path by lazy { buildPath() }

    private fun buildPath(): List<N> {
        val path = ArrayDeque<N>()
        if (currentNode in distance) {
            var nodeFoundThroughPrevious: N? = currentNode
            while (nodeFoundThroughPrevious != null) {
                path.addFirst(nodeFoundThroughPrevious)
                nodeFoundThroughPrevious = prev[nodeFoundThroughPrevious]
            }
        }
        return path
    }

}

class AStarSearch<N>(
    vararg startNodes: N,
    val neighborNodes: (N) -> Collection<N>,
    val cost: (N, N) -> Int,
    val costEstimation: (N, N) -> Int,
    val onExpand: ((SearchResult<N>) -> Unit)? = null,
) {
    private val dist = HashMap<N, Int>().apply { startNodes.forEach { put(it, 0) } }
    private val prev = HashMap<N, N>()
    private val openList = minPriorityQueueOf(elements = startNodes)
    private val closedList = HashSet<N>()

    fun search(destNode: N, limitSteps: Int? = null): SearchResult<N> {

        fun expandNode(currentNode: N) {
            onExpand?.invoke(SearchResult(currentNode, dist, prev))
            for (successor in neighborNodes(currentNode)) {
                if (successor in closedList)
                    continue

                val tentativeDist = dist[currentNode]!! + cost(currentNode, successor)
                if (successor in openList && tentativeDist >= dist[successor]!!)
                    continue

                prev[successor] = currentNode
                dist[successor] = tentativeDist

                val f = tentativeDist + costEstimation(successor, destNode)
                openList.insertOrUpdate(successor, f)
            }
        }

        if (destNode in dist)
            return SearchResult(destNode, dist, prev)

        var steps = 0
        while (steps++ != limitSteps && openList.isNotEmpty()) {
            val currentNode = openList.extractMin()
            if (currentNode == destNode)
                return SearchResult(destNode, dist, prev)

            closedList += currentNode
            expandNode(currentNode)
        }

        return SearchResult(openList.peekOrNull(), dist, prev)
    }
}

class Dijkstra<N>(
    val startNode: N,
    val neighborNodes: (N) -> Collection<N>,
    val cost: (N, N) -> Int,
) {
    private val dist = HashMap<N, Int>().apply { put(startNode, 0) }
    private val prev = HashMap<N, N>()
    private val queue = minPriorityQueueOf(startNode to 0)

    fun search(predicate: SolutionPredicate<N>): SearchResult<N> {
        while (queue.isNotEmpty()) {
            val u = queue.extractMin()
            if (predicate(u)) {
                return SearchResult(u, dist, prev)
            }
            for (v in neighborNodes(u)) {
                val alt = dist[u]!! + cost(u, v)
                if (alt < dist.getOrDefault(v, Int.MAX_VALUE)) {
                    dist[v] = alt
                    prev[v] = u
                    queue.insertOrUpdate(v, alt)
                }
            }
        }

        // failed search
        return SearchResult(null, dist, prev)
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
            if (debugHandler?.invoke(level, nodesOnLevel, nodesVisited) == SearchControl.STOP)
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

fun <N> loggingDebugger(): DebugHandler<N> = { level: Int, nodesOnLevel: Collection<N>, nodesVisited: Collection<N> ->
    println("I am on level $level, searching through ${nodesOnLevel.size}. Visited so far: ${nodesVisited.size}")
    SearchControl.CONTINUE
}
