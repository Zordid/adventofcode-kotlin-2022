import utils.*
import java.awt.Color
import kotlin.time.Duration.Companion.milliseconds

fun main() {
    Day17Vis().start()
}

private const val BLOCK_SIZE = 12
private const val HEIGHT = 15
private const val WIDTH = 14
private const val PIXEL_SIZE = 4
private val background = Color(0xCCCCB0)
private val text = Color(0xCCCCB0)

class Day17Vis : KPixelGameEngine("AoC in Kotlin 2022 Day 17: \"Pyroclastic Flow\"") {

    override fun onCreate() {
        construct(WIDTH.blocks, HEIGHT.blocks, PIXEL_SIZE)
        drawBackground()
        drawScore()
        limitFps = 1
    }

    private fun drawBackground() {
        fillRect(origin to origin + (1 to HEIGHT).blocks, Color.GRAY)
        fillRect((origin + (1 to 0).blocks) to origin + (8 to HEIGHT).blocks, background)
        fillRect((origin + (8 to 0).blocks) to origin + (9 to HEIGHT).blocks, Color.GRAY)
    }

    private fun drawScore() {
        fillRect(scoreArea, Color.BLACK)
        val middle = scoreArea.width / 2

        val rightEdge = middle + (6 * 8) / 2

        fun drawRightAligned(y: Int, s: String) =
            drawString(scorePos + (rightEdge - s.length * 8 to y), s)

        drawString(scorePos + (middle - (6 * 8) / 2 to 5), " SCORE", text)
        drawRightAligned(15, "$rocksDropped")

        drawString(scorePos + (middle - (6 * 8) / 2 to 30), "HEIGHT", text)
        drawRightAligned(40, "${heightSkipped + height}")

        drawString(scorePos + (middle - (6 * 8) / 2 to 55), " LINES", text)
        drawRightAligned(65, "${linesSkipped + lines.size}")

        val nextCommand = commands[commandIndex]
        drawString(scorePos + (middle - 8 to 90), "$nextCommand", text, scale = 2)

        val top = scorePos + (BLOCK_SIZE / 2 to 10.blocks)
        fillRect(top - (2 to 2), 4.blocks + 4, 4.blocks + 4, background)
        val nextRock = rocks[rockIndex]
        val adjust = top + (2 to 2).blocks - (nextRock.first().size to nextRock.size).blocks / 2
        nextRock.forAreaIndexed { p, c ->
            if (c == '#') {
                fillRect(adjust + p.blocks, BLOCK_SIZE, BLOCK_SIZE, colors[(rockIndex + 1) % rocks.size])
                drawRect(adjust + p.blocks + (1 to 1), BLOCK_SIZE - 2, BLOCK_SIZE - 2, Color.GRAY)
            }
        }
    }

    private fun drawPlayField() {
        fillRect((origin + (1 to 0).blocks) to origin + (8 to HEIGHT).blocks, background)
        val mspace = space.toMutableMap()
        currentRock?.insert(mspace, rockPos, '0' + rockIndex)

        var y = (HEIGHT - 1).blocks
        for (row in view) {
            for (col in 1..7) {
                val r = mspace[col to row]?.digitToIntOrNull()
                if (r in colors.indices) {
                    fillRect(col.blocks, y, BLOCK_SIZE, BLOCK_SIZE, colors[r!!])
                    drawRect(col.blocks + 1, y + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2, Color.GRAY)
                }
            }
            y -= BLOCK_SIZE
        }
    }

    var view = 1..HEIGHT
    val space: MutableMapGrid<Char> = listOf(bottom).toMapGrid().toMutableMap()
    var height = 0
    var heightSkipped = 0L
    var lines = mutableSetOf<Int>()
    var linesSkipped = 0L

    val commands = AoC.getPuzzleInput(17, Event(2022)).first().toList()
    var commandIndex = 0
    var rockIndex = 0

    var currentRock: Grid<Char>? = null
    var rockPos = origin
    var rocksDropped = 0L
    val rocksToDrop = 1000000000000

    var stop1 = 0

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        if (frame==0L) Thread.sleep(5000)
        var rock = currentRock
        if (rock == null) {
            if (rocksDropped == 2022L) {
                stop1++
                when (stop1) {
                    1 -> {
                        textBox("2022 pieces done!")
                        hold(3000)
                        return
                    }

                    2 -> {
                        textBox("2022 pieces done!\nBut wait... a little bit...")
                        hold(3000)
                        return
                    }

                    3 -> {
                        textBox("2022 pieces done!\nBut wait... a little bit...\nWe need\n$rocksToDrop pieces!")
                        hold(3000)
                        return
                    }

                    4 -> {
                        textBox("But wait... a little bit...\nWe need\n$rocksToDrop pieces!\nLet's go!")
                        hold(3000)
                        return
                    }

                    else -> drawBackground()
                }
            }

            if (rocksDropped == rocksToDrop) {
                drawBackground()
                drawPlayField()
                drawScore()
                stop()
                return
            }
            rock = rocks[rockIndex++]
            rockIndex %= rocks.size

            val p = pattern(6)
            val m = mem.getOrPut(p) { mutableMapOf() }
            if (rocksDropped > 8500 && (rockIndex to commandIndex) in m) {
                val last = m[rockIndex to commandIndex]!!
                val rockDiff = rocksDropped - last.first
                val heightDiff = height - last.second
                val linesDiff = lines.size - last.third
                val skipping = (rocksToDrop - rocksDropped) / rockDiff
                rocksDropped += skipping * rockDiff
                heightSkipped += skipping * heightDiff
                linesSkipped += skipping * linesDiff
                mem.clear()
            } else m[rockIndex to commandIndex] = Triple(rocksDropped, height, lines.size)

            rockPos = 3 to height + 3 + rock.size
            (height + 1..rockPos.y).forEach { row ->
                empty.forEachIndexed { i, c -> space[i to row] = c }
            }
            currentRock = rock
            rocksDropped++

            if (view.last < rockPos.y) {
                view = rockPos.y - HEIGHT..rockPos.y
            }

            space.keys.filter { it.y < height - 100 }.forEach { space.remove(it) }

        } else {
            val cmd = commands[commandIndex++]
            commandIndex %= commands.size

            val np = if (cmd == '<') rockPos.left() else rockPos.right()
            if (rock.fits(space, np))
                rockPos = np

            if (rock.fits(space, rockPos.fall()))
                rockPos = rockPos.fall()
            else {
                rock.insert(space, rockPos, '0' + rockIndex)
                val l = ((rockPos.y downTo (rockPos.y - 4).coerceAtLeast(1)).filter { r ->
                        (1..7).all { c -> space[c to r] != '.' }
                    })
                lines += l
                height = height.coerceAtLeast(rockPos.y)
                currentRock = null
            }
        }

        drawPlayField()
        drawScore()

        if (rocksDropped >= 3000) {
            if (rocksDropped == 3000L)
                timer = elapsedTime
            if (rocksDropped == 5000L)
                timer = elapsedTime - timer

            if (rocksDropped in 3000L..5000L)
                textBox("This can take a while...")
            else if (rocksDropped in 5000L..7000L) {
                val need = (((rocksToDrop-rocksDropped)/2000)*elapsedTime).milliseconds
                textBox("This can take a while...\nE.T.A. in ${need.inWholeDays/365} years...")
            } else if (rocksDropped in 7000 .. 7500) {
                textBox("Fast forwarding....\n>>>")
            } else if (rocksDropped in 7500 ..8000)
                textBox("Fast forwarding....\n>>>\nAlmost there!")
            else
                textBox("Fast forwarding....\n>>>\nAlmost there!\nHang in there!")
        }

        when {
            rocksToDrop - rocksDropped < 10 -> limitFps = 3 + (rocksToDrop - rocksDropped).toInt()
            rocksDropped > 2033 -> limitFps = 1000
            2033 - rocksDropped < 10 -> limitFps = 3 + (rocksDropped - 2022).toInt()
            2022 - rocksDropped < 10 -> limitFps = 3 + (2022 - rocksDropped).toInt()
            rocksDropped > 100 -> limitFps = 500
            rocksDropped > 10 -> limitFps = 14 + (height - 10)
            else -> limitFps = rocksDropped.toInt()
        }
    }

    var timer : Long = 0L

    fun textBox(s: String) {
        fillRect(BLOCK_SIZE / 2 to 10.blocks, screenWidth - BLOCK_SIZE, 4.blocks, Color.BLACK)
        drawStringProp(BLOCK_SIZE to 10.blocks + BLOCK_SIZE / 2, s, text)
    }

    fun pattern(size: Int = 5) = (height downTo (height - (size - 1))).flatMap { r ->
        (1..7).map { c -> space[c to r] }
    }

    val mem = mutableMapOf<Any, MutableMap<Pair<Int, Int>, Triple<Long, Int, Int>>>()

    companion object {

        val rocks: List<Grid<Char>> =
            ROCKS.split("\n\n").map { it.split("\n").map { it.toList() } }
        val colors = listOf(
            Color.CYAN,
            Color.MAGENTA,
            Color.RED,
            Color.BLUE,
            Color.GREEN
        ).shuffled() // rocks.map { randomBrightColor() }
        val empty = "+.......+".toList()
        val bottom = "+-------+".toList()

        val fieldPos = origin + (1 to 0).blocks
        val scorePos = origin + (7 + 2 to 0).blocks
        val scoreArea = scorePos to (WIDTH.blocks to HEIGHT.blocks)

        private val Int.blocks get() = this * BLOCK_SIZE
        private val Point.blocks get() = this * BLOCK_SIZE

        private fun Point.fall() = up()

        private fun Grid<Char>.fits(space: Map<Point, Char>, pos: Point): Boolean =
            area.allPoints().none { p ->
                ((this[p] == '#') && (space[pos + (p.x to -p.y)] != '.'))
            }

        private fun Grid<Char>.insert(space: MutableMap<Point, Char>, pos: Point, c: Char = '#') {
            area.forEach { p ->
                if (this[p] == '#') {
                    space[pos + (p.x to -p.y)] = c
                }
            }
        }

    }

}