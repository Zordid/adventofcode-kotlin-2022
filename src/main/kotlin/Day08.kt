import utils.*

class Day08 : Day(8, 2022, "Treetop Tree House") {

    private val heights = input.map { it.map(Char::digitToInt) }
    private val colIndices = heights.first().indices
    private val rowIndices = heights.indices

    override fun part1() = heights.area.allPoints().count { here ->
        val height = heights[here]
        directions.any { d ->
            here.treesInDirection(d).all { tree -> heights[tree] < height }
        }
    }

    override fun part2() = heights.area.allPoints().maxOf { here ->
        val height = heights[here]
        directions.map { d ->
            here.treesInDirection(d) countVisibleFrom height
        }.reduce(Int::times)
    }

    private fun Point.treesInDirection(d: Point) = sequence {
        var n = this@treesInDirection + d
        while (n.first in colIndices && n.second in rowIndices) {
            yield(n)
            n += d
        }
    }

    private infix fun Sequence<Point>.countVisibleFrom(height: Int) =
        withIndex().firstOrNull { heights[it.value] >= height }
            ?.let { it.index + 1 }  // we found a tree of height greater or equal
            ?: count()              // no tree blocking the view, so count them all

    private val directions = listOf(-1 to 0, +1 to 0, 0 to -1, 0 to +1)

}

fun main() {
    solve<Day08> {
        """
            30373
            25512
            65332
            33549
            35390
        """.trimIndent()(21, 8)
    }
}