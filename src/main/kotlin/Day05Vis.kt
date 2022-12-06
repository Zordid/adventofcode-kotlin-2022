import utils.*
import java.awt.Color
import kotlin.math.absoluteValue
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * CAUTION! This is absolutely *horrible* code! Don't you ever do this! ;-)
 */

class Day05Vis(private val day05: Day05) : KPixelGameEngine() {

    companion object {
        const val crateHeight = 9
        const val crateWidth = 9
        const val space = 2
        const val WIDTH = 9 * (crateWidth + space) + 70
        const val HEIGHT = 41 * (crateHeight + space)

        val colors = ('A'..'Z').associateWith { randomDullColor() }

        fun randomDullColor() =
            (0..2).map { Random.nextInt(255) }.let { (r, g, b) ->
                with(Color(r, g, b).blendWith(Color.BLACK, 0.3)) {
                    val hsb = Color.RGBtoHSB(red, green, blue, null)
                    hsb[1] = hsb[1] * 9 / 10
                    Color.getHSBColor(hsb[0], hsb[1], hsb[2])
                }
            }

        fun Color.blendWith(other: Color, ratio: Double): Color {
            val iRatio = 1.0f - ratio.coerceIn(0.0, 1.0)

            val i1 = rgb
            val i2 = other.rgb

            val a1 = i1 shr 24 and 0xff
            val r1 = i1 and 0xff0000 shr 16
            val g1 = i1 and 0xff00 shr 8
            val b1 = i1 and 0xff

            val a2 = i2 shr 24 and 0xff
            val r2 = i2 and 0xff0000 shr 16
            val g2 = i2 and 0xff00 shr 8
            val b2 = i2 and 0xff

            val a = (a1 * iRatio + a2 * ratio).toInt()
            val r = (r1 * iRatio + r2 * ratio).toInt()
            val g = (g1 * iRatio + g2 * ratio).toInt()
            val b = (b1 * iRatio + b2 * ratio).toInt()

            return Color(a shl 24 or (r shl 16) or (g shl 8) or b)
        }
    }

    private var state = day05.initialStacks
    private val instructions = day05.instructions.toMutableList()

    private var currentAnimation: Animation? = null
    private var currentInstruction = ""

    private var lastRopePos: Pair<Int, Int>? = null
    private var doneInstructions = 0

    private val widthNeeded = 9 * crateWidth + 8 * space
    private val xOffset = WIDTH / 2 - widthNeeded / 2

    override fun onCreate() {
        construct(WIDTH, HEIGHT, 2, 2, "2022 / 5")
        limitFps = 600
    }

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        drawStaticBackground()

        if (currentAnimation == null || currentAnimation?.ended == true) {
            if (instructions.isNotEmpty()) {
                val i = instructions.first()
                val (q, from, to) = i
                val nextState = day05.crateMover(state, instructions = instructions.take(1), day05.enBlock)
                val intermediateState = state.mapIndexed { index, s ->
                    when (index) {
                        from -> nextState[from]
                        else -> s
                    }
                }
                val crates = state[from].takeLast(q).reversed()
                val cratesHeight = crates.size * (crateHeight + space)
                val fromHeight = state[from].size
                val toHeight = state[to].size
                val jumpOver = screenHeight - 1 -
                        (if (from <= to) (from + 1..to) else (to until from))
                            .maxOf { state[it].size }
                            .coerceAtLeast(state[from].size - crates.size) * (crateHeight + space) - cratesHeight - space

                val x = xOffset + ((from - 1) * (crateWidth + space))
                val xRope = x + crateWidth / 2
                val pickupY = (screenHeight - 1) - (fromHeight * (crateHeight + space))

                val xTo = xOffset + ((to - 1) * (crateWidth + space))
                val xRopeTo = xTo + crateWidth / 2
                val dropY = (screenHeight - 1) - (toHeight * (crateHeight + space)) - cratesHeight

                val distance = (lastRopePos
                    ?: (xRope to 0)).let { (xx, yy) -> sqrt(((xx - xRope) * (xx - xRope) + (yy - pickupY) * (yy - pickupY)).toDouble()) }


                val speed = when {
                    doneInstructions in 0..3 -> 6.milliseconds
                    doneInstructions in 4..6 -> 3.milliseconds
                    instructions.size < 3 -> 6.milliseconds
                    instructions.size < 6 -> 3.milliseconds
                    else -> 0.microseconds
                }

                currentAnimation = animation(speed * distance) {
                    // rope
                    if (lastRopePos == null) {
                        val yRope by animate(0, pickupY)
                        drawRope(xRope, yRope)
                    } else lastRopePos?.let {
                        val xRope by animate(it.first, xRope)
                        val yRope by animate(it.second, pickupY)
                        drawRope(xRope, yRope)
                    }
                } then animation(speed * (pickupY - jumpOver).absoluteValue) {
                    // pull up
                    onStart { state = intermediateState }
                    val y by animate(pickupY, jumpOver)
                    drawRope(xRope, y)
                    crates.forEachIndexed { idx, c ->
                        drawCrate(x, y + (idx * (crateHeight + space)), c)
                    }
                    drawRect(x, y, crateWidth, cratesHeight - space)
                } then animation(speed * (x - xTo).absoluteValue) {
                    // move vertically
                    val x by animate(x, xTo)
                    drawRope(x + crateWidth / 2, jumpOver)
                    crates.forEachIndexed { idx, c ->
                        drawCrate(x, jumpOver + (idx * (crateHeight + space)), c)
                    }
                    drawRect(x, jumpOver, crateWidth, cratesHeight - space)
                } then animation(speed * (dropY - jumpOver).absoluteValue) {
                    // drop
                    val y by animate(jumpOver, dropY)
                    drawRope(xRopeTo, y)
                    crates.forEachIndexed { idx, c ->
                        drawCrate(xTo, y + (idx * (crateHeight + space)), c)
                    }
                    drawRect(xTo, y, crateWidth, cratesHeight - space)
                } then action {
                    state = nextState
                    lastRopePos = xRopeTo to dropY
                    doneInstructions++
                }

                currentInstruction =
                    instructions.removeAt(0).let { (q, f, t) ->
                        "#${doneInstructions+1}/${day05.instructions.size}:\nmove $q from $f to $t"
                    }

                // first animation waits 3 seconds... ;-)
                if (doneInstructions == 0) {
                    val saved = currentInstruction
                    currentAnimation = currentAnimation?.let {
                        animation(3.seconds) {
                            currentInstruction = "Initializing..."
                            onLastFrame {
                                currentInstruction = saved
                            }
                        } then it
                    }
                }

            } else {
                val flying = state.drop(1).map {
                    screenHeight - 1 - it.size * (crateHeight + space) to it.lastOrNull()
                }
                state = state.map { it.dropLast(1) }
                var animators: List<AnimatedInt>? = null
                currentAnimation = animation(1.seconds) {
                    onStart {
                        animators = flying.map { AnimatedInt(it.first, screenHeight / 2, this) }
                    }

                    flying.forEachIndexed { idx, crate ->
                        crate.second?.let {
                            val y = animators!![idx].value
                            drawCrate(xOffset + idx * (crateWidth + space), y, it)
                        }
                    }

                    onLastFrame {
                        stop()
                    }
                }
            }
        }
        currentAnimation?.update()
    }

    private fun drawStaticBackground() {
        clear()
        drawString(2, 2, currentInstruction)
        drawString(2, 2, "    CrateMover  9001".asIterable().joinToString("") { "$it\n" }, Color.GRAY)

        state.drop(1).forEachIndexed { index, stack ->
            val x = xOffset + (index * (crateWidth + space))
            val y = screenHeight - 1
            stack.forEachIndexed { idx, c ->
                drawCrate(x, y - ((idx + 1) * (crateHeight + space)), c)
            }
        }
    }

    private fun drawCrate(x: Int, y: Int, c: Char) {
        fillRect(x, y, crateWidth, crateHeight, colors[c]!!)
        drawString(x + 1, y + 1, c.toString())
    }

    private fun drawRope(x: Int, y: Int, color: Color = Color.WHITE) {
        drawLine(x, 0, x, y, color)
        drawLine(x - crateWidth / 2, y, x + crateWidth / 2, y, color)
    }

}

fun main() {
    Day05Vis(Day05()).start()
}