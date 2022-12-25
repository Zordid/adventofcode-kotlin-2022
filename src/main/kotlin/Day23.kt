import utils.*
import utils.Direction8.*

class Day23 : Day(23, 2022, "Unstable Diffusion") {

    val initialState = inputAsGrid.toMapGrid('.').keys

    val proposalStrategies = listOf(
        listOf(NORTH, NORTHEAST, NORTHWEST),
        listOf(SOUTH, SOUTHEAST, SOUTHWEST),
        listOf(WEST, NORTHWEST, SOUTHWEST),
        listOf(EAST, NORTHEAST, SOUTHEAST)
    )

    fun createProposals() = proposalStrategies.asInfiniteSequence().windowed(4, 1).iterator()

    fun Set<Point>.move(strategy: List<List<Direction8>>): Set<Point> =
        partition { elf -> elf.surroundingNeighbors().any { it in this } }.let { (move, stay) ->
            move.associateWith { elf ->
                val dir = strategy.firstOrNull { pn -> pn.none { (elf + it) in this } }
                elf + (dir?.first()?.vector ?: origin)
            }.let { proposals ->
                val proposalsPerLocation = proposals.values.groupingBy { it }.eachCount()
                proposals.entries.mapTo(HashSet()) { (elf, prop) -> if (proposalsPerLocation[prop]!! == 1) prop else elf }
            } + stay
        }

    override fun part1(): Int {
        log { initialState.plot(off = ".") }
        var elves = initialState
        val proposalStrategies = createProposals()
        repeat(10) {
            elves = elves.move(proposalStrategies.next())
            log { "After round ${it + 1}" }
            log { elves.plot(off = ".") }
        }
        return elves.boundingArea()!!.size - elves.size
    }

    override fun part2(): Int {
        log { initialState.plot(off = ".") }
        var elves = initialState
        val proposalStrategies = createProposals()
        var count = 0
        while (true) {
            count++
            val newElves = elves.move(proposalStrategies.next())
            if (newElves == elves) {
                return count
            }
            elves = newElves
            log { "After round $count" }
            log { elves.plot(off = ".") }
        }
    }
}

fun main() {
    solve<Day23> {
        """
            ....#..
            ..###.#
            #...#.#
            .#...##
            #.###..
            ##.#.##
            .#..#..
        """.trimIndent() part1 110 part2 20
    }
}