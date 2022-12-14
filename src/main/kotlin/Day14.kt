import utils.*

class Day14 : Day(14, 2022, "Regolith Reservoir") {

    val rockPaths: List<List<Point>> = input.map { it.extractAllIntegers().chunked(2).map(::asPoint) }
    val entry: Point = 500 to 0
    val area = rockPaths.flatten().boundingArea()!!
    val sandGoes = listOf(Direction8.SOUTH, Direction8.SOUTHWEST, Direction8.SOUTHEAST)

    val map = buildMap {
        rockPaths.forEach { path ->
            path.zipWithNext().forEach { (from, to) ->
                areaOf(from, to).forEach { put(it, '#') }
            }
        }
    }

    override fun part1(): Int {
        var rest = 0
        val map = this.map.toMutableMap()
        while (true) {
            log { map.formatted(area.grow(2)) }
            val p = map.dropSand(area.bottom)
            if (p.y >= area.bottom) break
            map[p] = 'o'
            rest++
        }

        return rest
    }

    override fun part2(): Int {
        var rest = 0
        val map = this.map.toMutableMap()
        while (map[entry]==null) {
            log { map.formatted(area.grow(2)) }
            val p = map.dropSand(area.bottom)
            map[p] = 'o'
            rest++
        }
        return rest
    }

    // drop sand at entry point and give the last position where it cannot go any further
    fun Map<Point, Char>.dropSand(bottomY: Int): Point {
        val map = this
        var s = entry
        drop@ while (map[s] == null && s.y <= bottomY) {
            s = sandGoes.map { s + it }.firstOrNull { map[it] == null } ?: return s
        }
        return s
    }

}

fun main() {
    solve<Day14>(true) {

        """
            498,4 -> 498,6 -> 496,6
            503,4 -> 502,4 -> 502,9 -> 494,9
        """.trimIndent() part1 24 part2 93

    }
}