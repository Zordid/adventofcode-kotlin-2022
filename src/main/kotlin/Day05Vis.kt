import utils.PixelGameEngine
import java.awt.Color
import kotlin.random.Random

class Day05Vis(val day05: Day05) : PixelGameEngine() {

    var state = day05.initialStacks
    val instructions = day05.instructions.toMutableList()

    val colors = ('A'..'Z').associate {
        it to dullColor()
    }

    fun dullColor() =
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

    val crateHeight = 9
    val crateWidth = 9
    val space = 2

    override fun onCreate() {
        construct(9 * (crateWidth + space), 41 * (crateHeight + space), 2, 2, "2022 / 5")
        limitFps = 30
        println(state.flatten().distinct().size)
        println(instructions.runningFold(state) { stacks, (q, from, to) ->
            stacks.mapIndexed { index, stack ->
                when (index) {
                    from -> stack.dropLast(q)
                    to -> stack + stacks[from].takeLast(q).reversed()
                    else -> stack
                }
            }
        }.maxOf {
            it.maxOf { it.size }
        })
    }

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        clear()
        state.drop(1).forEachIndexed { index, stack ->
            val x = space + (index * (crateWidth + space))
            val y = screenHeight - 1
            stack.forEachIndexed { idx, c ->
                fillRect(
                    x,
                    y - ((idx + 1) * (crateHeight + space)),
                    crateWidth,
                    crateHeight,
                    colors[stack[idx]]!!
                )
                drawString(x + 1, y - ((idx + 1) * (crateHeight + space)) + 1, c.toString())
            }
        }
        state = day05.crateMover(state, instructions = instructions.take(1), day05.enBlock)
        if (instructions.isNotEmpty()) instructions.removeAt(0) else stop()
    }
}

fun main() {
    Day05Vis(Day05()).start()
}