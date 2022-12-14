import utils.*

class Day14 : Day(14, 2022) {

    val p = input.map { it.extractAllIntegers().chunked(2).map { (x, y) -> Point(x, y) } }

    override fun part2(): Any? {

        val bounds = ((p + listOf(listOf(500 to 0))).flatten().boundingArea()!!).grow(1)
        val area = bounds
        println(area)

        val omap = MutableGrid(area.right + 1, area.bottom + 1) { '.' }

        for (path in p) {
            path.zipWithNext().forEach { (from, to) ->
                val d = Direction4.ofVector(to - from)!!.vector
                var s = from
                while (s != to) {
                    omap[s] = '#'
                    s += d
                }
                omap[to] = '#'
            }
        }
        val floorY = bounds.bottom + 1


        val map = omap.toMapGrid('.').toMutableMap()

        for (x in -1000..1000) {
            map[x to floorY] = '#'
        }

        val entry: Point = 500 to 0
        var rest = 0
        while (map[entry] == null) {

            ((490 to 0) to (550 to 12)).forEach {
                log(map.getOrDefault(it, '.'))
                if (it.x == 490) println()
            }

            var s = entry

            while (true) {
                if (map[s.down()] == null) s = s.down()
                else if (map[s.down().left()] == null) s = s.down().left()
                else if (map[s.down().right()] == null) s = s.down().right()
                else {
                    map[s] = 'o'
                    rest++
                    break
                }
            }
        }

        return rest
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