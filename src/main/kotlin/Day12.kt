import utils.*

class Day12 : Day(12, 2022, "Hill Climbing Algorithm") {

    val map = inputAsGrid
    val heights = inputAsGrid.mapValues {
        when (it) {
            'S' -> 'a'
            'E' -> 'z'
            else -> it
        } - 'a'
    }.show()
    val area = inputAsGrid.area
    val start = inputAsGrid.searchIndices('S').single()
    val dest = inputAsGrid.searchIndices('E').single()


    override fun part1(): Int {
        val result = breadthFirstSearch(start,
            { here: Point -> here.directNeighbors(area).filter { heights[it] - heights[here] <= 1 } }
        ) { it == dest }
        return result.path().size - 1
    }

    override fun part2(): Int {
        val result = breadthFirstSearch(dest,
            { here -> here.directNeighbors(area).filter { heights[here] - heights[it] <= 1 } }
        ) { heights[it] == 0 }
        return result.path().size - 1
    }

}

fun main() {
    solve<Day12> {
        """
            Sabqponm
            abcryxxl
            accszExk
            acctuvwj
            abdefghi
        """.trimIndent() part1 31 part2 29
    }
}