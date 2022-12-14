import utils.*

class Day14 : Day(14, 2022) {

    val p = input.map { it.extractAllIntegers().chunked(2).map { (x, y) -> Point(x, y) } }

    override fun part1(): Any? {

        val area = ((p + listOf(listOf(500 to 0))).flatten().boundingArea()!!).grow(1)
        println(area)

        val map = MutableGrid(area.right + 1, area.bottom + 1) { '.' }

        for (path in p) {
            path.zipWithNext().forEach { (from, to) ->
                val d = Direction4.ofVector(to - from)!!.vector
                var s = from
                while (s != to) {
                    map[s] = '#'
                    s += d
                }
                map[to] = '#'
            }
        }


        val entry: Point = 500 to 0
        var rest = 0
        while (true) {
            var s = entry

            while (s.y < area.bottom-1) {
                if (map[s.down()] == '.') s = s.down()
                else if (map[s.down().left()] == '.') s = s.down().left()
                else if (map[s.down().right()] == '.') s = s.down().right()
                else {
                    map[s] = 'o'
                    rest++
                    break
                }
            }

            if (s.y >= area.bottom-1) break
        }

        return rest
    }

}

fun main() {
    solve<Day14>(true) {

        """
            498,4 -> 498,6 -> 496,6
            503,4 -> 502,4 -> 502,9 -> 494,9
        """.trimIndent() part1 24


    }
}