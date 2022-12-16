import guru.nidi.graphviz.attribute.Attributes.attr
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.toGraphviz
import utils.Dijkstra
import utils.Graph
import utils.combinations
import utils.completeAcyclicTraverse
import java.io.File

class Day16 : Day(16, 2022, "Proboscidea Volcanium") {

    val valves = input.map {
        val (_, id) = it.split(" ", limit = 3)
        val rate = it.sequenceContainedIntegers().first()
        val leadTo =
            (it.substringAfter("lead to valves ", "").split(", ") +
                    it.substringAfter("to valve ", "")).filter { !it.isBlank() }
        Valve(id, rate, leadTo)
    }.show()

    val relevantValves = valves.filter { it.rate > 0 }.map { it.id }

    val vMap = valves.associateBy { it.id }

    data class Valve(val id: String, val rate: Int, val leadsTo: List<String>)

    data class State(
        val time: Int,
        val stillClosed: List<String>,
        val pos: String = "AA",
        val totalRelease: Int = 0,
    )

    fun State.moveTo(moveTo: String, steps: Int) = copy(time = time - steps, pos = moveTo)
    fun State.open() = copy(
        time = time - 1, stillClosed = stillClosed - pos,
        totalRelease = totalRelease + (time - 1) * vMap[pos]!!.rate
    )

    fun State.moveToAndOpen(moveTo: String, steps: Int) = moveTo(moveTo, steps).open()

    val distances = buildMap {
        (relevantValves + "AA").combinations(2).forEach { (from, to) ->
            val path = Dijkstra(from, { n: String -> vMap[n]!!.leadsTo }, { _, _ -> 1 }).search { it == to }
            path.steps?.let {
                put(setOf(from, to), it)
            }
        }
    }

    fun State.whatCanYouDo() = stillClosed.mapNotNull { target ->
        distances[setOf(pos, target)]?.takeIf { it < time - 1 }?.let { target to it }
    }

    fun State.unreleasedFlow() = stillClosed.sumOf { vMap[it]!!.rate }

    inner class G : Graph<State> {
        override fun neighborsOf(node: State): Collection<State> =
            node.whatCanYouDo().map { node.moveToAndOpen(it.first, it.second) }
    }

    val g = G()

    override fun part1(): Any? {
        val initial = State(30, stillClosed = relevantValves)
        return g.completeAcyclicTraverse(initial).maxOf { it.second.maxOf { it.totalRelease } }
    }

    fun buildCluster(): Pair<Set<String>, Set<String>> {

        val maxDistance = relevantValves.map { it to distances[setOf("AA", it)]!! }
            .sortedByDescending { it.second }
        println(maxDistance)


        TODO()
    }

    fun State.energy(timeRem: Int) = stillClosed.sumOf {
        vMap[it]!!.rate
    }


    override fun part2(): Int {
        val initial = State(26, relevantValves)

//        println(distances.entries.sortedBy { it.value }.joinToString("\n"))

//        graphViz()

        val elephantRange = 3..relevantValves.size / 2
        val solution = elephantRange.map { howMany ->
            alog { "Trying $howMany of ${relevantValves.size} out of $elephantRange for the elephant" }
            val best = relevantValves.combinations(howMany).map { elephant ->
                val me = relevantValves - elephant.toSet()
                log { "Me: $me, elephant: $elephant" }

                val meState = initial.copy(stillClosed = me)
                val elState = initial.copy(stillClosed = elephant)

                val maxMe = g.completeAcyclicTraverse(meState).maxOf { it.second.maxOf { it.totalRelease } }
                val maxEl = g.completeAcyclicTraverse(elState).maxOf { it.second.maxOf { it.totalRelease } }

                log { "Me: $maxMe, elephant: $maxEl" }
                (maxMe + maxEl) to (me to elephant)
            }.maxBy { it.first }
            alog { "best so far: $best" }
            best
        }.maxBy { it.first }
        println(solution)
        return solution.first
    }

    fun graphViz() {
        graph("Day 16") {

            for (v in valves) {
                (-v.id)[attr(
                    "label",
                    "${v.id}" + "\n${v.rate}".takeIf { v.rate > 0 }.orEmpty()
                ), if (v.rate > 0) Color.RED else Color.GRAY]

                for (l in v.leadsTo) {
                    v.id - l
                }
            }


        }.toGraphviz().render(Format.PNG).toFile(File("d16.png"))
    }
}

fun main() {
    solve<Day16>(true) {
        """
            Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
            Valve BB has flow rate=13; tunnels lead to valves CC, AA
            Valve CC has flow rate=2; tunnels lead to valves DD, BB
            Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
            Valve EE has flow rate=3; tunnels lead to valves FF, DD
            Valve FF has flow rate=0; tunnels lead to valves EE, GG
            Valve GG has flow rate=0; tunnels lead to valves FF, HH
            Valve HH has flow rate=22; tunnel leads to valve GG
            Valve II has flow rate=0; tunnels lead to valves AA, JJ
            Valve JJ has flow rate=21; tunnel leads to valve II
        """.trimIndent() // part1 1651 part2 1707
    }
}