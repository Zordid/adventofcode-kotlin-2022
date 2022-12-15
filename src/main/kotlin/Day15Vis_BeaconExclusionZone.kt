import utils.*
import java.awt.Color

fun main() {
    Day15Vis().start()
}

class Day15Vis : KPixelGameEngine("AoC in Kotlin 2022 Day 15: \"Beacon Exclusion Zone\"") {

    val day15 = Day15()
    val scannerBeacons = day15.sensorsAndBeacons

    val colors = scannerBeacons.associate { it.first() to randomDullColor().withAlpha(128) }

    val area = 0..4_000_000

    val factor = 5_000
    val solution = day15.part2.let { day15.solutionPart2 } / factor

    lateinit var background: Array<Color>

    override fun onCreate() {
        construct(area.size / factor, area.size / factor)

        drawDiamonds()
        drawSensorsAndBeacons()
        background = saveBuffer()

        limitFps = 10
    }

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        restoreBuffer(background)
        drawFocus(frame, solution, 10)
    }

    fun drawSensorsAndBeacons(){
        for ((s,b) in scannerBeacons){
            fillCircle(s/factor, 5, Color.RED)
            drawCross(b/factor, 5, Color.MAGENTA)
            drawLine(s/factor, b/factor, Color.MAGENTA, Pattern.DOTTED)
        }
    }

    private fun drawDiamonds() {
        pixelMode = PixelMode.ALPHA
        for ((s, b) in scannerBeacons) {

            val radius = (s manhattanDistanceTo b) / factor
            var p1 = s / factor
            var p2 = s / factor

            val c = colors[s]!!

            drawLine(p1.left(radius), p1.right(radius), c)
            p1 += Direction4.NORTH
            p2 += Direction4.SOUTH
            for (r in radius - 1 downTo 1) {
                drawLine(p1.left(r), p1.right(r), c)
                drawLine(p2.left(r), p2.right(r), c)
                p1 += Direction4.NORTH
                p2 += Direction4.SOUTH
            }
        }
    }

    /**
     * Draw a circular focus, pulsating from [radius] down to 0 every second.
     */
    fun drawFocus(frame: Long, p: Point, radius: Int = 10, color: Color = Color.RED) {
        val step = (radius / limitFps).coerceAtLeast(1)
        val steps = radius / step
        drawCircle(p, radius - ((frame % steps).toInt()) * step, color)
    }

    fun drawCross(p: Point, size: Int = 10, color: Color = Color.RED) {
        drawLine(p+Direction8.NORTHWEST*size, p+Direction8.SOUTHEAST*size, color)
        drawLine(p+Direction8.NORTHEAST*size, p+Direction8.SOUTHWEST*size, color)
    }

}