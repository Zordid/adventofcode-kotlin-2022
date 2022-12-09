import utils.*
import java.awt.Color

class Day09Vis(val day: Day09) : KPixelGameEngine("AoC 2022 Day 9: Rope Bridge") {

    private val pixelSize = 1
    private val simulateKnots = 9

    private val solutionColor = Color.GREEN.darker()
    private val ropeColor = Color.RED

    private val headPos: List<Point> = buildList {
        addAll(with(day) { moves.moveHead() })
        repeat(simulateKnots) { add(last()) }
    }
    private val tails: List<List<Point>>
    private val area: Area

    init {
        var a = headPos.boundingArea()!!
        tails = (1..simulateKnots).runningFold(headPos) { prev, _ ->
            with(day) {
                prev.moveTail().also {
                    a += it.boundingArea()!!
                }
            }
        }
        area = a
    }

    private var offset = area.first
    private val colors = listOf(Color.BLUE) + createGradient(ropeColor, Color.GRAY, tails.size - 1)
    private val allLastTail = mutableSetOf<Point>()

    override fun onCreate() {
        construct(area.width, area.height, pixelSize)
        limitFps = 600
    }

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        val f = frame.toInt()

        if (f !in tails.first().indices) {
            stop()
            return
        }

        clear()
        allLastTail.forEach { draw(it, solutionColor) }
        tails.withIndex().reversed().forEach { (idx, p) ->
            val d = p[f] - offset
            draw(d, colors[idx])
            if (idx == tails.lastIndex) allLastTail += d
        }
        drawString(10, screenHeight - 10 - 8, "${allLastTail.size} visited", solutionColor)

        if (frame == 0L) hold(3000)
    }

}

fun main() {
    Day09Vis(Day09()).start()
}