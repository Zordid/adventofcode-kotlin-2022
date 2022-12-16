import utils.Dijkstra
import utils.Graph
import utils.buildStack
import utils.completeAcyclicTraverse

class Day16 : Day(16, 2022) {

    val p = input.map {
        val (_, id) = it.split(" ", limit = 3)
        val rate = it.sequenceContainedIntegers().first()
        val leadTo =
            (it.substringAfter("lead to valves ", "").split(", ") +
                    it.substringAfter("to valve ", "")).filter { !it.isBlank() }
        Valve(id, rate, leadTo)
    }.show()

    val v = p.associateBy { it.id }

    val f = State("AA", 30, v.filter { it.value.rate > 0 })

    data class Valve(val id: String, val rate: Int, val leadsTo: List<String>, val open: Boolean = false)

    data class State(
        val pos: String,
        val time: Int = 30,
        val closedValves: Map<String, Valve>,
        val totalRelease: Int = 0,
    )

    fun State.moveTo(moveTo: String, steps: Int) = copy(time = time - steps, pos = moveTo)
    fun State.open() = copy(
        time = time - 1, closedValves = closedValves - pos,
        totalRelease = totalRelease + (time - 1) * closedValves[pos]!!.rate
    )

    fun State.moveToAndOpen(moveTo: String, steps: Int) = moveTo(moveTo, steps).open()

    fun State.whatToDo(): List<Pair<String, Int>> {
        val targets = closedValves.keys

        log { "At $time remaining and ${totalRelease} total release, I could go and open:" }
        val go = targets.mapNotNull { t ->
            val r = Dijkstra<String>({ n -> v[n]!!.leadsTo }, { _, _ -> 1 }).search(pos) { it == t }

            r.buildStack().takeIf { r.currentNode != null && it.size < time }
        }
        go.forEach { log { it.lastElement() + " ${it.size - 1} steps: $it" } }

        return go.map { it.lastElement() to it.size - 1 }
    }


    override fun part1(): Any? {
        val M = object : Graph<State> {
            override fun neighborsOf(node: State): Collection<State> =
                node.whatToDo().map { node.moveToAndOpen(it.first, it.second) }

            override fun cost(from: State, to: State): Int {
                val stillRemaining = to.closedValves.values
                return super.cost(from, to)
            }
        }

        return M.completeAcyclicTraverse(f).maxOf { it.second.maxOf { it.totalRelease } }


        return 0
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
        """.trimIndent() part1 1651
    }
}