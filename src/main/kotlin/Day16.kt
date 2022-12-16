import utils.*

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

    val f = State("AA", 26, v.filter { it.value.rate > 0 }.map { it.key })

    data class Valve(val id: String, val rate: Int, val leadsTo: List<String>, val open: Boolean = false)

    data class State(
        val pos: String,
        val time: Int,
        val stillClosed: List<String>,
        val totalRelease: Int = 0,
    )

    fun State.moveTo(moveTo: String, steps: Int) = copy(time = time - steps, pos = moveTo)
    fun State.open() = copy(
        time = time - 1, stillClosed = stillClosed - pos,
        totalRelease = totalRelease + (time - 1) * v[pos]!!.rate
    )

    fun State.moveToAndOpen(moveTo: String, steps: Int) = moveTo(moveTo, steps).open()

    val cacheDistance: MutableMap<List<String>, Int?> = mutableMapOf()

    fun State.whatToDo(): List<Pair<String, Int>> {
        val targets = stillClosed

        //log { "At $time remaining and $totalRelease total release, I could go and open:" }
        val go = targets.mapNotNull { target ->
            val distance = cacheDistance.getOrPut(listOf(pos, target)) {
                val r = Dijkstra<String>({ n -> v[n]!!.leadsTo }, { _, _ -> 1 }).search(pos) { it == target }
                r.buildStack().takeIf { r.currentNode != null }?.let { it.size - 1 }
            }
            distance?.let { target to it }?.takeIf { it.second < time }
        }

        //  go.forEach { log { it.lastElement() + " ${it.size - 1} steps: $it" } }

        return go
    }

    fun State.unreleasedFlow() = stillClosed.sumOf { v[it]!!.rate }

    override fun part2(): Any? {
        cacheDistance.clear()

        val M = object : Graph<State> {
            override fun neighborsOf(node: State): Collection<State> =
                node.whatToDo().map { node.moveToAndOpen(it.first, it.second) }

            override fun cost(from: State, to: State): Int {
                val potentialFrom = from.unreleasedFlow()
                val potentialTo = to.unreleasedFlow()
                return 1000 - (potentialFrom - potentialTo)
            }

        }

        val initial = f

        logEnabled = true
        val valves = v.filter { it.value.rate > 0 }.map { it.key }
        return (3..valves.size - 3).maxOf { howMany ->
            log { "Trying $howMany for the elephant" }
            valves.combinations(howMany).maxOf { elephant ->
                val me = valves - elephant.toSet()
                log { "Me: $me, elephant: $elephant" }

                val meState = initial.copy(time = 26, stillClosed = initial.stillClosed - elephant)
                val elState = initial.copy(time = 26, stillClosed = initial.stillClosed - me)

                val maxMe = M.completeAcyclicTraverse(meState).maxOf { it.second.maxOf { it.totalRelease }}
                val maxEl = M.completeAcyclicTraverse(elState).maxOf { it.second.maxOf { it.totalRelease }}


                log { "Me: $maxMe, elephant: $maxEl" }
                maxMe + maxEl
            }
        }
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
        """.trimIndent() part1 1651 part2 1707
    }
}