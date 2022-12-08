import utils.Point
import utils.allPoints
import utils.area

class Day08 : Day(8, 2022) {

    val h = input.map { it.map { it.digitToInt() } }
    val cols = h.first().size
    val rows = h.size

    override fun part1(): Any? {
        val r = h.indices.flatMap { row ->
            h[row].foldIndexed(emptyList<Point>() to -1) { idx, (v, m), t ->
                if (t > m) (v + (idx to row) to t) else (v to m)
            }.first
        }
        val l = h.indices.flatMap { row ->
            h[row].reversed().foldIndexed(emptyList<Point>() to -1) { idx, (v, m), t ->
                if (t > m) (v + (cols - idx - 1 to row) to t) else (v to m)
            }.first
        }
        val t = h.first().indices.flatMap { col ->
            h.map { it[col] }.foldIndexed(emptyList<Point>() to -1) { idx, (v, m), t ->
                if (t > m) (v + (col to idx) to t) else (v to m)
            }.first
        }
        val b = h.first().indices.flatMap { col ->
            h.map { it[col] }.reversed().foldIndexed(emptyList<Point>() to -1) { idx, (v, m), t ->
                if (t > m) (v + (col to rows - idx - 1) to t) else (v to m)
            }.first
        }

        return ((r + l + t + b).toSet()).count()
    }

    override fun part2(): Any? {
        val x = h.area.allPoints().map { (x, y) ->
            val s = h[y][x]
            var stop = false
            val u = (y - 1 downTo 0).map { h[it][x] }.fold(0 to -1) { (c, m), t ->
                (if (!stop ) (c + 1 to t) else (c to m)).also { if (t>=s) stop = true }
            }.first
            stop = false
            val d = (y + 1 until rows).map { h[it][x] }.fold(0 to -1) { (c, m), t ->
                (if (!stop ) (c + 1 to t) else (c to m)).also { if (t>=s) stop = true }
            }.first
            stop = false
            val r = (x + 1 until cols).map { h[y][it] }.fold(0 to -1) { (c, m), t ->
                (if (!stop )(c + 1 to t) else (c to m)).also { if (t>=s) stop = true }
            }.first
            stop = false
            val l = (x - 1 downTo 0).map { h[y][it] }.fold(0 to -1) { (c, m), t ->
                (if (!stop ) (c + 1 to t) else (c to m)).also { if (t>=s) stop = true }
            }.first

            (x to y) to u*d*l*r
        }.maxBy { it.second }
        return x.second
    }

}

fun main() {
    solve<Day08>(true) {
        """
    30373
    25512
    65332
    33549
    35390
""".trimIndent()(21, 8)


    }
}