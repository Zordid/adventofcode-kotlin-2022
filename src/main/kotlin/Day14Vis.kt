import utils.*
import java.awt.Color

fun main() {
    Day14Vis().start()
}

class Day14Vis : KPixelGameEngine("AoC in Kotlin 2022 Day 14: \"Regolith Reservoir\"") {

    val day14 = Day14()
    val rocks = day14.map.keys
    val entry = day14.entry
    val floor = rocks.maxOf { it.y } + 2
    val displayArea = (rocks + day14.entry).boundingArea()!!.grow(5).growLeft(100).growRight(100)
    val offset = -displayArea.upperLeft
    val sandGoes = listOf(Direction8.SOUTH, Direction8.SOUTHWEST, Direction8.SOUTHEAST)

    override fun onCreate() {
        construct(displayArea.width, displayArea.height, 4)
        drawRocks()
        limitFps = 200
    }

    var sandpattern = Pattern.DASHED
    var previous: Point? = null
    var drop: Iterator<Point>? = null
    val map = day14.map.toMutableMap()
    var dropCount = 0
    var skipSand = 0

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        if (frame == 0L) Thread.sleep(2000)
        drawLine(entry + (0 to -10) + offset, entry + offset, Color.BLACK)
        drawLine(entry + (0 to -10) + offset, entry + offset, Color.YELLOW, sandpattern)
        if (frame % 5L == 0L)
            sandpattern = sandpattern.rotate()

        drop?.let { path ->
            if (path.hasNext()) {
                previous?.let { draw(it + offset, Color.BLACK) }
                val n = path.next()
                draw(n + offset, Color.YELLOW)
                previous = n
            } else {
                previous?.let {
                    draw(it + offset, Color.YELLOW.darker())
                    map[it] = 'o'
                    dropCount++
                    previous = null
                    if (it.y == floor - 1) {
                        skipSand = 100
                    }
                }
                drop = null
            }
        }

        if (drop == null) {
            repeat(skipSand) {
                if (map[entry] == null) {
                    val next = map.dropSand(floor - 1, 10_000).last()
                    map[next] = 'o'
                    dropCount++
                    draw(next + offset, Color.YELLOW.darker())
                }
            }
            drop = map.dropSand(floor - 1, dropCount / 5).iterator()
        }
        if (map[entry] != null) {
            drawLine(entry + (0 to -10) + offset, entry + Direction4.NORTH + offset, Color.BLACK)
            drawStringProp(1, 1, "$dropCount units!")
            stop()
        }
    }

    fun drawRocks(drawFloor: Boolean = true) {
        rocks.forEach {
            draw(it + offset, Color.LIGHT_GRAY)
        }
        if (drawFloor) fillRect((0 to floor + offset.y), screenWidth , screenHeight-(floor+offset.y), Color.LIGHT_GRAY)
    }

    // drop sand at entry point and give the last position where it cannot go any further
    fun Map<Point, Char>.dropSand(bottomY: Int, every: Int) = sequence {
        val map = this@dropSand
        var s = entry
        var count = 0
        drop@ while (map[s] == null && s.y < bottomY) {
            val nextPos = sandGoes.map { s + it }.firstOrNull { map[it] == null } ?: break
            if (count++ % every.coerceAtLeast(1) == 0)
                yield(nextPos)
            s = nextPos
        }
        yield(s)
    }


}