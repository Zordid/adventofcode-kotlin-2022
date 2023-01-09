import utils.*
import java.awt.Color

fun main() {
    Day22Vis().start()
}

class Day22Vis : KPixelGameEngine("AoC in Kotlin 2022 Day 22: \"Monkey Map\"") {

    val day22 = Day22()

    val cube = CubeOrigami(day22.map)
    val op = day22.op

    override fun onCreate() {
        construct(cube.width, cube.height, 5)
        limitFps = 100
    }

    val anim = go().iterator()
    val colors = createGradient(Color.RED, Color(100, 0, 0), 100) + Color.BLACK

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        val (len, trace) = if (anim.hasNext()) anim.next() else (-1 to emptyList())
        if (len == -1) {
            stop()
            return
        }

        cube.folding.forAreaIndexed { f, c ->
            if (c != ' ') with(cube) {
                drawRect(
                    f * faceDimension,
                    faceDimension,
                    faceDimension,
                    Color(200, 200, 200).withAlpha(120)
                )
                drawString(
                    f * faceDimension + (faceDimension to faceDimension) / 2 - (4 to 4) * 3,
                    "$c",
                    Color(30, 30, 30).withAlpha(120),
                    3
                )
                drawString(
                    f * faceDimension + (faceDimension to faceDimension) / 2 - (4 to 4) * 3 - (1 to 1),
                    "$c",
                    Color(70, 70, 70).withAlpha(120),
                    3
                )
            }
        }
        cube.forAreaIndexed { p, c ->
            if (c == '#') draw(p, Color.DARK_GRAY)
            if (trace[p] != 0) {
                val d = len - trace[p]
                if (d in colors.indices)
                    draw(p, colors[d])
            }
        }

    }

    fun go(): Sequence<Pair<Int, Grid<Int>>> = sequence {
        val trace = cube.mapValues { 0 }.toMutableGrid()
        var p = cube.startingPositionOnPaper
        var h = Direction4.RIGHT

        var c = 0
        trace[p] = ++c

        for ((walk, turn) in op) {
            log { "We are at $p and will walk $walk $h" }
            log { trace.formatted() }
            for (w in 1..walk) {
                val (newP, newH) = cube.walkOnPaper(p, h)
                if (cube[newP] == WALL) break

                p = newP
                h = newH
                trace[p] = ++c
                yield(c to trace)
            }
            h = when (turn) {
                'L' -> h.left
                'R' -> h.right
                else -> h
            }
            log { "Now at $p heading $h" }
            log { }
        }
    }


}