import utils.*
import java.awt.Color

fun main() {
    Day08Vis_TreetopTreeHouse().start()
}

class Day08Vis_TreetopTreeHouse : KPixelGameEngine("AoC in Kotlin 2022 Day 8 - Treetop Tree House") {

    val heights = AoC.getPuzzleInput(8, Event(2022)).map { it.map(Char::digitToInt) }
    val dimensions = heights.area

    val colors = createGradient(Color(20, 0, 0), Color.RED, 10)

    val visibleTrees = heights.area.allPoints().filter { here ->
        val height = heights[here]
        Direction4.allVectors.any { d ->
            here.treesInDirection(d).all { tree -> heights[tree] < height }
        }
    }.toList()

    fun Point.treesInDirection(d: Point) = sequence {
        var n = this@treesInDirection + d
        while (n in heights.area) {
            yield(n)
            n += d
        }
    }

    override fun onCreate() {
        construct(dimensions.width, dimensions.height+8, 5)
        limitFps = 10
    }

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        inFrame(frame, 0..50) { frame ->
            if (frame <= 9L) {
                val maxLevel = frame.toInt()
                dimensions.forEach {
                    draw(it, colors[heights[it].coerceAtMost(maxLevel)])
                }
            } else if (frame <= 30) {
                val maxLevel = 30 - frame.toInt()

                dimensions.forEach {
                    if (it !in visibleTrees) draw(it, colors[heights[it].coerceAtMost(maxLevel)])
                }
            } else {
                dimensions.forEach {
                    if (it !in visibleTrees) draw(it, Color.BLACK)
                }
                "${visibleTrees.size} visible!".let {
                    val (width, _) = getTextSizeProp(it)
                    drawStringProp(screenWidth / 2 - width / 2, screenHeight - 8, it)
                }
            }
        }

        inFrame(frame, 100..200) {

        }
    }

    inline fun inFrame(frame: Long, frameRange: IntRange, block: (Long) -> Unit) {
        if (frame in frameRange) block(frame - frameRange.first)
    }

}