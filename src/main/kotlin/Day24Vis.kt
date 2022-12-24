import utils.*
import java.awt.Color
import kotlin.random.Random

fun main() {
    Day24Vis().start()
}

class Day24Vis : KPixelGameEngine("AoC in Kotlin 2022 Day 24: \"Blizzard Basin\"") {

    val day24 = Day24()
    val map = day24.map
    val area = day24.area
    val horizontal = day24.horizontal
    val vertical = day24.vertical
    val start = day24.start
    val dest = day24.destination
    val w = area.width - 2
    val h = area.height - 2

    override fun onCreate() {
        construct(area.width * 3, area.height * 3 + 12, 4)
        limitFps = 10
    }

    val solution = part2()

    val hcolor = horizontal.mapValues { (_, b) ->
        b.map {
            Color.WHITE.let {
                if (Random.nextBoolean())
                    it.darker() else it
            }.withAlpha(Random.nextInt(60, 100))
        }
    }
    val vcolor = vertical.mapValues { (_, b) ->
        b.map {
            Color.WHITE.let {
                if (Random.nextBoolean())
                    it.darker() else it
            }.withAlpha(Random.nextInt(60, 100))
        }
    }

    val tailLen = 50
    val topTailColor = Color.RED
    val persistTailColor = Color(213, 97, 97).darker().withAlpha(150)
    val tail = createGradient(persistTailColor, topTailColor, tailLen)
    val textOrigin = 2 to area.height * 3 + 2

    val startDelay = 60

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        val time = (frame.toInt() - startDelay) % (solution.size + 60)

        clear()
        drawRect(1 to 1, area.width * 3 -2, area.height * 3-2, Color.GREEN)
        fillRect(start * 3, 3, 3, Color.BLACK)
        fillRect(dest * 3, 3, 3, Color.BLACK)

        horizontal.forEach { (y, b) ->
            val colors = hcolor[y]!!
            b.forEachIndexed { idx, (startX, d) ->
                val x = (startX - 1 + d * time).mod(w) + 1
                val p = (x to y) * 3
                if (d > 1)
                    fillRect(p, 3, 2, colors[idx])
                else
                    fillRect(p + (0 to 1), 3, 2, colors[idx])
            }
        }

        vertical.forEach { (x, b) ->
            val colors = vcolor[x]!!
            b.forEachIndexed { idx, (startY, d) ->
                val y = (startY - 1 + d * time).mod(h) + 1
                val p = (x to y) * 3
                if (d > 1)
                    fillRect(p, 2, 3, colors[idx])
                else
                    fillRect(p + (1 to 0), 2, 3, colors[idx])
            }
        }

        if (frame < startDelay) {
            drawStringProp(textOrigin, "It's a cold, cold, blizzardy day")
            return
        }

        limitFps = if (time in 100..solution.size - 50)
            60
        else
            10

        solution.take(time).forEachIndexed { idx, p ->
            fillRect(p * 3, 3, 3, tail[(tail.lastIndex - time + idx).coerceIn(0, tail.lastIndex)])
        }

        drawStringProp(textOrigin, "Path so far: ${time.coerceAtMost(solution.size)}")
    }

    fun part2(): List<Point> {
        val q = ArrayDeque<Pair<Point, Int>>()
        val seen = mutableSetOf<Pair<Point, Int>>()
        var trip = 0
        lateinit var goal: Point

        fun setGoal(pos: Point, currentPos: Point, currentTime: Int) {
            trip++
            seen.clear()
            q.clear()
            q.add(currentPos to currentTime)
            goal = pos
        }

        val prev = mutableMapOf<Pair<Point, Int>, Pair<Point, Int>>()
        lateinit var final: Pair<Point, Int>
        setGoal(dest, start, 0)
        while (q.isNotEmpty()) {
            val (p, t) = q.removeFirst()
            if (p == goal)
                when (trip) {
                    1 -> setGoal(start, p, t)
                    2 -> setGoal(dest, p, t)
                    3 -> {
                        final = p to t
                        break
                    }
                }
            if (!seen.add(p to t % day24.repeats)) continue

            val neighbors = neighbors(p, t)
            neighbors.forEach {
                prev[it] = p to t
            }
            q += neighbors
        }

        val path = ArrayDeque<Point>()
        var x = final
        while (x != start to 0) {
            path.addFirst(x.first)
            x = prev[x]!!
        }

        return path
    }

    fun noBlizzard(pos: Point, time: Int): Boolean =
        horizontal[pos.y]?.none { pos.x == (((it.first - 1) + it.second * time).mod(w)) + 1 } ?: true &&
                vertical[pos.x]?.none { pos.y == (((it.first - 1) + it.second * time).mod(h)) + 1 } ?: true

    fun neighbors(pos: Point, time: Int) = (pos.directNeighbors(area) + pos).filter {
        map[it] != '#' && noBlizzard(it, time + 1)
    }.map { it to time + 1 }

}