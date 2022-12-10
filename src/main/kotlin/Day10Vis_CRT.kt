import utils.*
import java.awt.Color
import kotlin.math.roundToInt

fun main() {
    Day10Vis().start()
}

class Day10Vis : KPixelGameEngine("AoC 2022 in Kotlin Day 10 - CRT") {

    val day = Day10()

    val startFps = 300
    val offset = 5 to 4
    val crt = (origin to (39 to 5)) + offset
    val sprite = (origin to (39 to 0)) + crt.lowerLeft + (0 to 2)

    val x = buildList {
        day.simulator(day.microOps, preCycle = { cycle, s ->
            add(cycle to s.x)
        })
    }.asInfiniteSequence().iterator()

    override fun onCreate() {
        construct(40 + 10, 6 + 10, 10)
        limitFps = startFps
    }

    private lateinit var current: Pair<Int, Int>

    val screen = MutableGrid(40, 6) { 0.0 }

    var frameDivider = 60
    var decay = 0.996
    var loops = -1
    val noiseFor = frameDivider * 25

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        if (frame >= noiseFor && frame % frameDivider == 0L) {
            fadeOutScreen()
            if (x.hasNext()) {
                current = x.next()
                if (current.first == 1) loops++

            }
            val (cycle, x) = current
            val col = (cycle - 1) % 40
            val row = (cycle - 1) / 40
            val drawPixel = x in col - 1..col + 1
            if (drawPixel)
                screen[col to row] = 1.0

            if (cycle % 80 == 0) frameDivider = (frameDivider / 2).coerceAtLeast(1)
            if (loops == 2) limitFps = 800
        }
        if (frame < noiseFor)
            noise()
        else {
            clear()
            drawCRT()
            drawSprite(current.second)
        }
    }

    fun fadeOutScreen() {
        screen.forArea {
            screen[it] = screen[it] * decay
        }
    }

    val ray = Color(255, 0, 0, 128)

    private fun drawCRT() {
        val x = (current.first - 1) % 40
        val y = (current.first - 1) / 40
        crt.forEach {
            val p = it - offset
            val alpha = (255 * screen[p]).roundToInt()
            val pixel = Color(255, 255, 255, alpha)
            draw(it, pixel mixedAbove randomGrayColor())
        }
        pixelMode = PixelMode.ALPHA
        draw((x to y) + offset, ray)
        pixelMode = PixelMode.NORMAL
    }

    private fun drawFinalCRT() {
        crt.forEach {
            val p = it - offset
            val on = screen[p] > 0.0
            if (on)
                draw(it, Color.WHITE)
            else
                draw(it, randomGrayColor())
        }
    }

    private fun noise() {
        crt.forEach {
            draw(it, randomGrayColor())
        }
    }

    fun drawSprite(x: Int) {
        val start = sprite.upperLeft + (x - 1 to 0)
        repeat(3) {
            draw(start + (it to 0))
        }
    }

    operator fun Area.plus(offset: Point) = (first + offset) to second + offset

}