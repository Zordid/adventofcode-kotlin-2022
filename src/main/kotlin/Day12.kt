import utils.*
import java.util.*

class Day12 : Day(12, 2022, "Hill Climbing Algorithm") {

    val map = inputAsGrid
    val p = inputAsGrid.map {
        it.map {
            when (it) {
                in 'a'..'z' -> it
                'S' -> 'a'
                'E' -> 'z'
                else -> error("$it")
            } - 'a'
        }
    }.show()
    val area = inputAsGrid.area
    val start = inputAsGrid.searchIndices('S').single()
    val dest = inputAsGrid.searchIndices('E').single()

    override fun part1(): Int {
        val path = findPathFrom(start)
        return path.size - 1
    }

    override fun part2(): Int {
        val possibleStarts = inputAsGrid.searchIndices('a')
        val starts = possibleStarts.mapNotNull { lowStart ->
            val s = breadthFirstSearch(lowStart, neighborNodes = { here ->
                Direction4.all.map { d -> here + d }
                    .filter { it in p.area && (p[it] - p[here] <= 1) }
            }) { it == dest }
            (s.size - 1).takeIf { s.isNotEmpty() }
        }.min()
        return starts
    }

    private fun findPathFrom(start: Point): Stack<Point> =
        breadthFirstSearch(start, neighborNodes = { here ->
            here.directNeighbors(area).filter { (p[it] - p[here] <= 1) }
        }) { it == dest }

}

fun main() {
    solve<Day12>(true) {

        """
            Sabqponm
            abcryxxl
            accszExk
            acctuvwj
            abdefghi
        """.trimIndent() part1 31 part2 29

    }
}