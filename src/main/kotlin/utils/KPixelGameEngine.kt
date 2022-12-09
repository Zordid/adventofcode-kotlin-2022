@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package utils

import utils.KPixelGameEngine.Pattern.Companion.DEFAULT_PATTERN
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * [KPixelGameEngine], a Kotlin port of OLC's PixelGameEngine
 *
 * An absolutely simple and rudimentary engine to quickly draw rough, pixelized things on screen. Good for demos,
 * little games, mazes, etc. Inspired by olcPixelGameEngine for C++ from OLC.
 * https://github.com/OneLoneCoder/olcPixelGameEngine
 * See contained LICENSE.md
 *
 * kPGE displays a Swing window of defined size and runs in an endless game loop, allowing your code to act
 * directly before start and within each iteration of the game loop.
 *
 * Usage:
 * Derive from this abstract class and overwrite the methods [onCreate] and [onUpdate].
 *
 * Version history:
 * V2.0   - 17/11/2022 improve repainting only the dirty area
 * V2.1   - 18/11/2022 add triangle functions, add frame sequence builder, fixed refresh
 * V2.2   - 19/11/2022 rework pattern and allow all line drawing methods to work with a single pattern
 * V2.3   - 20/11/2022 update circle methods
 * V2.4   - 23/11/2022 optionally close polylines
 * V2.4.1 - 05/12/2022 add rudimentary text drawing method
 * V2.4.2 - 09/12/2022 title generation changed, added proportional font drawing
 *
 */
abstract class KPixelGameEngine(appName: String = "KPixelGameEngine") {

    private inner class GamePanel(val pixelWidth: Int, val pixelHeight: Int) : JPanel() {

        fun refresh() = repaint()

        fun refresh(xL: Int, yL: Int, xH: Int, yH: Int) {
            if (xL <= xH)
                repaint(xL * pixelWidth, yL * pixelHeight, (xH - xL + 1) * pixelWidth, (yH - yL + 1) * pixelHeight)
        }

        override fun paint(g: Graphics) {
            super.paint(g)
            val stableCopy = displayBuffer.copyOf()

            val endX =
                ((g.clipBounds.x + g.clipBounds.width) / pixelWidth).coerceAtMost(screenWidth)
            val endY =
                ((g.clipBounds.y + g.clipBounds.height) / pixelHeight).coerceAtMost(screenHeight)

            val startX = g.clipBounds.x / pixelWidth
            var y = g.clipBounds.y / pixelHeight
            while (y < endY) {
                var x = startX
                var p = y * screenWidth + x
                while (x < endX) {
                    val color = stableCopy[p++]
                    var length = 1
                    while (x + length < endX && stableCopy[p] == color) {
                        p++
                        length++
                    }
                    if (color != Color.BLACK) {
                        g.color = color
                        g.fillRect(x * pixelWidth, y * pixelHeight, pixelWidth * length, pixelHeight)
                    }
                    x += length
                }
                y++
            }
        }

    }

    private var appName = ""
    var appInfo: Any? = null
        set(value) {
            if (value != field) {
                title = "$appName - $value"
            }
            field = value
        }

    private var title = appName

    var limitFps: Int = Int.MAX_VALUE
        set(value) {
            field = value
            millisPerFrame = when {
                value < 1000 -> (1000.0 / value).roundToLong()
                else -> 0
            }
        }
    private var millisPerFrame: Long = 0
    private lateinit var frame: JFrame
    var screenWidth = 0
        private set
    var screenHeight = 0
        private set

    private lateinit var displayBuffer: Array<Color>
    private lateinit var buffer: Array<Color>
    private lateinit var panel: GamePanel

    private var dirtyXLow = Int.MAX_VALUE
    private var dirtyYLow = Int.MAX_VALUE
    private var dirtyXHigh = Int.MIN_VALUE
    private var dirtyYHigh = Int.MIN_VALUE

    init {
        limitFps = 50
    }

    private val fontSheet by lazy { createFontSheet() }
    var nTabSizeInSpaces = 8

    private fun resetDirty() {
        dirtyXLow = Int.MAX_VALUE
        dirtyXHigh = Int.MIN_VALUE
        dirtyYLow = Int.MAX_VALUE
        dirtyYHigh = Int.MIN_VALUE
    }

    fun construct(
        screenWidth: Int,
        screenHeight: Int,
        pixelWidth: Int = 1,
        pixelHeight: Int = pixelWidth,
    ) {
        require(screenWidth > 0 && screenHeight > 0) { "Unsupported dimensions: $screenWidth x $screenHeight" }
        require(pixelWidth > 0 && pixelHeight > 0) { "Unsupported pixel dimensions: $pixelWidth x $pixelHeight" }

        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
        buffer = Array(screenWidth * screenHeight) { Color.BLACK }
        displayBuffer = buffer

        panel = GamePanel(pixelWidth, pixelHeight)
        panel.background = Color.BLACK
        frame = JFrame()
        with(frame) {
            updateTitle("initialized")
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            isResizable = false
            pack()
            size = with(insets) {
                Dimension(
                    screenWidth * pixelWidth + left + right,
                    screenHeight * pixelHeight + top + bottom
                )
            }
            panel.alignmentX = JComponent.CENTER_ALIGNMENT
            panel.alignmentY = JComponent.CENTER_ALIGNMENT
            add(panel)
            setLocationRelativeTo(null)
            isVisible = true
        }
    }

    private var halted = false
    private var hold = 0L

    open fun isActive() = true

    fun start() {
        onCreate()
        panel.repaint()
        val startTime = System.currentTimeMillis()
        var frame = 0L

        var holdCurrentFrame = 0L

        while (!halted && isActive()) {
            var time = measureTimeMillis {
                resetDirty()
                buffer = buffer.copyOf()
                onUpdate(System.currentTimeMillis() - startTime, frame++)
            }
            val fillTime = (millisPerFrame - time).coerceAtLeast(holdCurrentFrame)
            if (fillTime > 0) {
                holdCurrentFrame = 0
                Thread.sleep(fillTime)
                time += fillTime
            }
            displayBuffer = buffer
            panel.refresh(dirtyXLow, dirtyYLow, dirtyXHigh, dirtyYHigh)
            updateTitle(1000.0 / time)
            if (hold > 0) {
                holdCurrentFrame = hold
                hold = 0
            }
        }
        onStop(System.currentTimeMillis() - startTime, frame)
        panel.refresh()
        updateTitle("stopped")
        while (true) Thread.sleep(10_000)
    }

    /**
     * Draws a pixel on the screen in the defined color.
     *
     * @param pos the coordinates of the pixel to draw
     * @param color the color to draw
     */
    fun draw(pos: P, color: Color = Color.WHITE) =
        draw(pos.first, pos.second, color)

    /**
     * Draws a pixel on the screen in the defined color.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param color the color to draw
     */
    fun draw(x: Int, y: Int, color: Color = Color.WHITE) {
        val pos = y * screenWidth + x
        if (x in 0 until screenWidth && y in 0 until screenHeight && buffer[pos] != color) {
            buffer[pos] = color
            if (dirtyXLow > x) dirtyXLow = x
            if (dirtyXHigh < x) dirtyXHigh = x
            if (dirtyYLow > y) dirtyYLow = y
            if (dirtyYHigh < y) dirtyYHigh = y
        }
    }

    /**
     * Draws a polygon defined by its vertices.
     *
     * @param vertices all vertices
     * @param close defines the polygon to be closed, connecting the last and the first vertex
     * @param color the color to use
     * @param pattern the pattern to use
     * @return the reusable shifted pattern for drawing more lines
     */
    fun drawPolyLine(
        vararg vertices: P,
        close: Boolean = false,
        color: Color = Color.WHITE,
        pattern: Pattern = DEFAULT_PATTERN,
    ): Pattern {
        vertices.isNotEmpty() || return pattern
        var p = pattern
        var a = vertices[0]
        for (i in 1 until vertices.size) {
            val b = vertices[i]
            p = drawLine(a, b, color, p).rotate()
            a = b
        }
        return if (close)
            drawLine(a, vertices[0], color, p)
        else
            p.rotateBackward()
    }

    /**
     * Draws a polygon defined by its vertices.
     *
     * @param coordinates all vertices' coordinates x and y, alternating
     * @param close defines the polygon to be closed, connecting the last and the first vertex
     * @param color the color to use
     * @param pattern the pattern to use
     * @return the reusable shifted pattern for drawing more lines
     */
    fun drawPolyLine(
        vararg coordinates: Int,
        close: Boolean = false,
        color: Color = Color.WHITE,
        pattern: Pattern = DEFAULT_PATTERN,
    ): Pattern {
        require(coordinates.size % 2 == 0)
        coordinates.isNotEmpty() || return pattern
        var p = pattern
        var xa = coordinates[0]
        var ya = coordinates[1]
        for (i in 2 until coordinates.size step 2) {
            val xb = coordinates[i]
            val yb = coordinates[i + 1]
            p = drawLine(xa, ya, xb, yb, color, p).rotate()
            xa = xb
            ya = yb
        }
        return if (close)
            drawLine(xa, ya, coordinates[0], coordinates[1], color, p)
        else
            p.rotateBackward()
    }

    /**
     * Draws a line on the screen in the defined color using the given pattern.
     *
     * @param from start coordinates
     * @param to end coordinates
     * @param color the color to use
     * @param pattern the pattern to use
     * @return the reusable shifted pattern for drawing more lines
     */
    fun drawLine(from: P, to: P, color: Color = Color.WHITE, pattern: Pattern = DEFAULT_PATTERN) =
        drawLine(from.first, from.second, to.first, to.second, color, pattern)

    /**
     * Draws a line on the screen in the defined color using the given pattern.
     *
     * @param x1 start x coordinate
     * @param y1 start y coordinate
     * @param x2 end x coordinate
     * @param y2 end y coordinate
     * @param color the color to use
     * @param pattern the pattern to use
     * @return the reusable shifted pattern for drawing more lines
     */
    fun drawLine(
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        color: Color = Color.WHITE,
        pattern: Pattern = DEFAULT_PATTERN,
    ): Pattern {
        var p = pattern.mask

        fun rol(): Boolean {
            return (p and 1L == 1L).also { p = (p ushr 1) or (p shl 63) }
        }

        val dx = x2 - x1
        val dy = y2 - y1


        if (dx == 0) { // vertical line
            if (y1 <= y2) {
                for (y in y1.coerceAtLeast(0)..y2.coerceAtMost(screenHeight - 1))
                    if (rol()) draw(x1, y, color)
            } else {
                for (y in y1.coerceAtMost(screenHeight - 1) downTo y2.coerceAtLeast(0))
                    if (rol()) draw(x1, y, color)
            }
            return Pattern(p)
        }

        if (dy == 0) { // horizontal line
            if (x1 <= x2) {
                for (x in x1.coerceAtLeast(0)..x2.coerceAtMost(screenWidth - 1))
                    if (rol()) draw(x, y1, color)
            } else {
                for (x in x1.coerceAtMost(screenWidth - 1) downTo x2.coerceAtLeast(0))
                    if (rol()) draw(x, y1, color)
            }
            return Pattern(p)
        }

        val dx1 = dx.absoluteValue
        val dy1 = dy.absoluteValue
        var px = 2 * dy1 - dx1
        var py = 2 * dx1 - dy1

        if (dy1 <= dx1) {
            var x: Int
            var y: Int
            val xe: Int
            if (dx >= 0) {
                x = x1
                y = y1
                xe = x2
            } else {
                x = x2
                y = y2
                xe = x1
            }

            if (rol()) draw(x, y, color)
            while (x < xe) {
                x += 1
                if (px < 0)
                    px += 2 * dy1
                else {
                    if ((dx < 0 && dy < 0) || (dx > 0 && dy > 0)) y += 1 else y -= 1
                    px += 2 * (dy1 - dx1)
                }
                if (rol()) draw(x, y, color)
            }
        } else {
            var x: Int
            var y: Int
            val ye: Int
            if (dy >= 0) {
                x = x1
                y = y1
                ye = y2
            } else {
                x = x2
                y = y2
                ye = y1
            }

            if (rol()) draw(x, y, color)
            while (y < ye) {
                y += 1
                if (py < 0)
                    py += 2 * dx1
                else {
                    if ((dx < 0 && dy < 0) || (dx > 0 && dy > 0)) x += 1 else x -= 1
                    py += 2 * (dx1 - dy1)
                }
                if (rol()) draw(x, y, color)
            }
        }
        return Pattern(p)
    }

    fun drawRect(area: Pair<P, P>, color: Color = Color.WHITE, pattern: Pattern = DEFAULT_PATTERN) {
        drawRect(
            area.first.first, area.first.second,
            area.second.first - area.first.first + 1, area.second.second - area.first.second + 1,
            color, pattern
        )
    }

    /**
     * Draws a rectangle on the screen in the defined color using the given pattern.
     *
     * @param x the top left corner's x coordinate
     * @param y the top left corner's y coordinate
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @param color the color to use
     * @param pattern the pattern to use
     */
    fun drawRect(
        x: Int, y: Int, width: Int, height: Int,
        color: Color = Color.WHITE,
        pattern: Pattern = DEFAULT_PATTERN,
    ): Pattern {
        var p = pattern
        if (width > 1 && height > 1) {
            p = drawLine(x, y, x + width - 2, y, color, p)
            p = drawLine(x + width - 1, y, x + width - 1, y + height - 2, color, p)
            p = drawLine(x + width - 1, y + height - 1, x + 1, y + height - 1, color, p)
            p = drawLine(x, y + height - 1, x, y + 1, color, p)
        } else if (width == 1 && height > 0) {
            p = drawLine(x, y, x, y + height - 1, color, p)
        } else if (width > 0 && height == 1) {
            p = drawLine(x, y, x + width - 1, y, color, p)
        }
        return p
    }

    fun fillRect(area: Pair<P, P>, color: Color = Color.WHITE) {
        fillRect(
            area.first.first, area.first.second,
            area.second.first - area.first.first + 1, area.second.second - area.first.second + 1,
            color
        )
    }

    /**
     * Fills a rectangle on the screen in the defined color using the given pattern.
     *
     * @param x the top left corner's x coordinate
     * @param y the top left corner's y coordinate
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @param color the color to use for fill
     */
    fun fillRect(x: Int, y: Int, width: Int, height: Int, color: Color = Color.WHITE) {
        val x1 = x.coerceIn(0, screenWidth - 1)
        val y1 = y.coerceIn(0, screenHeight - 1)
        val x2 = (x + width - 1).coerceIn(0, screenWidth - 1)
        val y2 = (y + height - 1).coerceIn(0, screenHeight - 1)

        for (i in x1..x2)
            for (j in y1..y2)
                draw(i, j, color)
    }

    /**
     * Draws a triangle on the screen in the defined color using the given pattern.
     *
     * @param p1 first vertex's coordinates
     * @param p2 first vertex's coordinates
     * @param p3 first vertex's coordinates
     * @param color the color to use
     * @param pattern the pattern to use
     * @return the reusable shifted pattern for drawing more lines
     */
    fun drawTriangle(p1: P, p2: P, p3: P, color: Color = Color.WHITE, pattern: Pattern = DEFAULT_PATTERN): Pattern =
        drawPolyLine(p1, p2, p3, close = true, color = color, pattern = pattern)

    /**
     * Draws a triangle on the screen in the defined color using the given pattern.
     *
     * @param x1 first vertex's x coordinate
     * @param y1 first vertex's y coordinate
     * @param x2 second vertex's x coordinate
     * @param y2 second vertex's y coordinate
     * @param x3 third vertex's x coordinate
     * @param y3 third vertex's y coordinate
     * @param color the color to use
     * @param pattern the pattern to use
     * @return the reusable shifted pattern for drawing more lines
     */
    fun drawTriangle(
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        x3: Int,
        y3: Int,
        color: Color = Color.WHITE,
        pattern: Pattern = DEFAULT_PATTERN,
    ): Pattern =
        drawPolyLine(x1, y1, x2, y2, x3, y3, close = true, color = color, pattern = pattern)

    /**
     * Draws a circle on the screen in the defined color using the given pattern.
     *
     * @param center the center's coordinates
     * @param radius the radius of the circle
     * @param color the color to use
     * @param pattern the pattern to use
     */
    fun drawCircle(center: P, radius: Int, color: Color = Color.WHITE, pattern: Int = 0xFF) =
        drawCircle(center.first, center.second, radius, color, pattern)

    /**
     * Draws a circle on the screen in the defined color using the given pattern.
     *
     * @param x the center's x coordinate
     * @param y the center's y coordinate
     * @param radius the radius of the circle
     * @param color the color to use
     * @param pattern the pattern to use
     */
    fun drawCircle(x: Int, y: Int, radius: Int, color: Color = Color.WHITE, pattern: Int = 0xFF) {
        if (radius < 0 || x < -radius || y < -radius || x - screenWidth > radius || y - screenHeight > radius)
            return

        var x0 = 0
        var y0 = radius
        var d = 3 - 2 * radius
        if (radius > 0)
            while (y0 >= x0) {
                if (pattern and 0x01 != 0) draw(x + x0, y - y0, color)
                if (pattern and 0x04 != 0) draw(x + y0, y + x0, color)
                if (pattern and 0x10 != 0) draw(x - x0, y + y0, color)
                if (pattern and 0x40 != 0) draw(x - y0, y - x0, color)
                if (x0 != 0 && x0 != y0) {
                    if (pattern and 0x02 != 0) draw(x + y0, y - x0, color)
                    if (pattern and 0x08 != 0) draw(x + x0, y + y0, color)
                    if (pattern and 0x20 != 0) draw(x - y0, y + x0, color)
                    if (pattern and 0x80 != 0) draw(x - x0, y - y0, color)
                }
                d += if (d < 0)
                    4 * x0++ + 6
                else
                    4 * (x0++ - y0--) + 10
            }
        else
            draw(x, y, color)
    }

    /**
     * Fills a circle on the screen in the defined color.
     *
     * @param center the center's coordinates
     * @param radius the radius of the circle
     * @param color the color to use
     */
    fun fillCircle(center: P, radius: Int, color: Color = Color.WHITE) =
        fillCircle(center.first, center.second, radius, color)

    /**
     * Fills a circle on the screen in the defined color.
     *
     * @param x the center's x coordinate
     * @param y the center's y coordinate
     * @param radius the radius of the circle
     * @param color the color to use
     */
    fun fillCircle(x: Int, y: Int, radius: Int, color: Color = Color.WHITE) {
        if (radius < 0 || x < -radius || y < -radius || x - screenWidth > radius || y - screenHeight > radius)
            return

        if (radius > 0) {
            var x0 = 0
            var y0 = radius
            var d = 3 - 2 * radius

            fun drawLine(sx: Int, ex: Int, y: Int) {
                for (i in sx..ex) draw(i, y, color)
            }

            // draw scan-lines instead of edges
            while (y0 >= x0) {
                drawLine(x - y0, x + y0, y - x0)
                if (x0 > 0)
                    drawLine(x - y0, x + y0, y + x0)

                d += if (d < 0) {
                    4 * x0++ + 6
                } else {
                    if (x0 != y0) {
                        drawLine(x - x0, x + x0, y - y0)
                        drawLine(x - x0, x + x0, y + y0)
                    }
                    4 * (x0++ - y0--) + 10
                }
            }
        } else
            draw(x, y, color)
    }


    /**
     * Draws text on screen in the defined color.
     *
     * @param p the text's x top left coordinate
     * @param text the text to draw - only 96 printable characters are allowed
     * @param color the color to draw in
     * @param scale scale factor
     */
    fun drawString(p: P, text: String, color: Color = Color.WHITE, scale: Int = 1) =
        drawString(p.first, p.second, text, color, scale)

    /**
     * Draws text on screen in the defined color.
     *
     * @param x the text's top left x coordinate
     * @param y the text's top left y coordinate
     * @param text the text to draw - only 96 printable characters are allowed
     * @param color the color to draw in
     * @param scale scale factor
     */
    fun drawString(x: Int, y: Int, text: String, color: Color = Color.WHITE, scale: Int = 1) {
        var sx = 0
        var sy = 0
        for (c in text) {
            when (c) {
                '\n' -> {
                    sx = 0
                    sy += 8 * scale
                }

                '\t' -> {
                    sx += 8 * nTabSizeInSpaces * scale
                }

                else -> {
                    val ox = (c.code - 32) % 16
                    val oy = (c.code - 32) / 16

                    if (scale > 1) {
                        for (i in 0 until 8)
                            for (j in 0 until 8)
                                if (fontSheet[i + ox * 8 + (j + oy * 8) * 128])
                                    for (`is` in 0 until scale)
                                        for (js in 0 until scale)
                                            draw(x + sx + (i * scale) + `is`, y + sy + (j * scale) + js, color)
                    } else {
                        for (i in 0 until 8)
                            for (j in 0 until 8)
                                if (fontSheet[i + ox * 8 + (j + oy * 8) * 128])
                                    draw(x + sx + i, y + sy + j, color)
                    }
                    sx += 8 * scale
                }
            }
        }
    }

    /**
     * Calculates the area required for the given text using proportional drawing.
     *
     * @param text the text to measure
     * @param scale the scale factor for text drawing
     * @return width and height in pixel
     */
    fun getTextSizeProp(text: String, scale: Int = 1): P {
        var sx = 0
        var lines = 1
        var maxWidth = 0
        for (c in text) {
            when (c) {
                '\n' -> {
                    maxWidth = maxWidth.coerceAtLeast(sx)
                    lines++; sx = 0
                }

                '\t' -> sx += nTabSizeInSpaces * 8
                else -> sx += vFontSpacing[c.code - 32].second
            }
        }
        return maxWidth.coerceAtLeast(sx) * scale to (lines * 8 * scale)
    }

    /**
     * Draws text on screen in the defined color using proportional spacing.
     *
     * @param p the text's top left coordinate
     * @param text the text to draw - only 96 printable characters are allowed
     * @param color the color to draw in
     * @param scale scale factor
     * @return the width of the text drawn
     */
    fun drawStringProp(p: Point, text: String, color: Color = Color.WHITE, scale: Int = 1) =
        drawStringProp(p.x, p.y, text, color, scale)

    /**
     * Draws text on screen in the defined color using proportional spacing.
     *
     * @param x the text's top left x coordinate
     * @param y the text's top left y coordinate
     * @param text the text to draw - only 96 printable characters are allowed
     * @param color the color to draw in
     * @param scale scale factor
     * @return the width of the text drawn
     */
    fun drawStringProp(x: Int, y: Int, text: String, color: Color = Color.WHITE, scale: Int = 1): Int {
        var maxWidth = 0
        var sx = 0
        var sy = 0
        for (c in text) {
            when (c) {
                '\n' -> {
                    sx = 0
                    sy += 8 * scale
                }

                '\t' -> {
                    sx += 8 * nTabSizeInSpaces * scale
                }

                else -> {
                    val ox = (c.code - 32) % 16
                    val oy = (c.code - 32) / 16

                    val cWidth = vFontSpacing[c.code - 32].second
                    if (scale > 1) {
                        for (i in 0 until cWidth)
                            for (j in 0 until 8)
                                if (fontSheet[i + ox * 8 + vFontSpacing[c.code - 32].first + (j + oy * 8) * 128])
                                    for (`is` in 0 until scale)
                                        for (js in 0 until scale)
                                            draw(x + sx + (i * scale) + `is`, y + sy + (j * scale) + js, color)
                    } else {
                        for (i in 0 until cWidth)
                            for (j in 0 until 8)
                                if (fontSheet[i + ox * 8 + vFontSpacing[c.code - 32].first + (j + oy * 8) * 128])
                                    draw(x + sx + i, y + sy + j, color)
                    }
                    sx += cWidth * scale
                    maxWidth = maxWidth.coerceAtLeast(sx)
                }
            }
        }
        return maxWidth
    }

    /**
     * Clears the buffer using the given color.
     *
     * @param color the color to clear with
     */
    fun clear(color: Color = Color.BLACK) {
        dirtyXLow = 0
        dirtyYLow = 0
        dirtyXHigh = screenWidth - 1
        dirtyYHigh = screenHeight - 1
        buffer.fill(color)
    }

    /**
     * Sleeps for [millis] milliseconds. Can be used to slow down the animation in [onUpdate].
     */
    fun sleep(millis: Long) = Thread.sleep(millis)

    /**
     * After updating the screen, the current frame will be shown at least the given amount of time.
     */
    fun hold(millis: Long) {
        hold = millis
    }

    /**
     * Stops the frame updates. [onUpdate] will not be called anymore.
     */
    fun stop() {
        halted = true
    }

    /**
     * Will be called from the game engine right before the endless game loop. Can be used to initialize things.
     */
    open fun onCreate() {
        // nop
    }

    /**
     * Will be called once per game loop to update the screen. Use the supplied methods to interact with the screen.
     * @see draw
     * @see drawLine
     * @see drawRect
     * @see fillRect
     * @see drawCircle
     * @see fillCircle
     * @see sleep
     */
    open fun onUpdate(elapsedTime: Long, frame: Long) {
        stop()
    }

    /**
     * Called once right before stopping
     */
    open fun onStop(elapsedTime: Long, frame: Long) {
        // nop
    }

    private fun updateTitle(fps: Double) {
        frame.title = "$title - ${"%.1f".format(fps)} fps"
    }

    private fun updateTitle(state: String) {
        frame.title = "$title - $state"
    }

    fun FrameIterator.use() {
        val hasNext = hasNext()
        if (!hasNext) stop() else {
            next()
        }
    }

    companion object {
        fun gradientColor(from: Color, to: Color, percent: Float): Color {
            val resultRed: Float = from.red + percent * (to.red - from.red)
            val resultGreen: Float = from.green + percent * (to.green - from.green)
            val resultBlue: Float = from.blue + percent * (to.blue - from.blue)
            return Color(resultRed.roundToInt(), resultGreen.roundToInt(), resultBlue.roundToInt())
        }

        fun createGradient(from: Color, to: Color = Color.BLACK, steps: Int): List<Color> =
            (0 until steps).map { gradientColor(from, to, it / (steps - 1).toFloat()) }


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

        suspend fun FrameScope.frame() = yield(Unit)

        fun frameSequence(block: suspend FrameScope.() -> Unit): FrameIterator = sequence(block).iterator()

        val vFontSpacing = byteArrayOf(
            0x03, 0x25, 0x16, 0x08, 0x07, 0x08, 0x08, 0x04, 0x15, 0x15, 0x08, 0x07, 0x15, 0x07, 0x24, 0x08,
            0x08, 0x17, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x24, 0x15, 0x06, 0x07, 0x16, 0x17,
            0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x17, 0x08, 0x08, 0x17, 0x08, 0x08, 0x08,
            0x08, 0x08, 0x08, 0x08, 0x17, 0x08, 0x08, 0x08, 0x08, 0x17, 0x08, 0x15, 0x08, 0x15, 0x08, 0x08,
            0x24, 0x18, 0x17, 0x17, 0x17, 0x17, 0x17, 0x17, 0x17, 0x33, 0x17, 0x17, 0x33, 0x18, 0x17, 0x17,
            0x17, 0x17, 0x17, 0x17, 0x07, 0x17, 0x17, 0x18, 0x18, 0x17, 0x17, 0x07, 0x33, 0x07, 0x08, 0x00,
        ).map { (it.toInt() shr 4) to (it.toInt() and 15) }

        private fun createFontSheet(): BooleanArray {
            val data = buildString {
                append("?Q`0001oOch0o01o@F40o0<AGD4090LAGD<090@A7ch0?00O7Q`0600>00000000")
                append("O000000nOT0063Qo4d8>?7a14Gno94AA4gno94AaOT0>o3`oO400o7QN00000400")
                append("Of80001oOg<7O7moBGT7O7lABET024@aBEd714AiOdl717a_=TH013Q>00000000")
                append("720D000V?V5oB3Q_HdUoE7a9@DdDE4A9@DmoE4A;Hg]oM4Aj8S4D84@`00000000")
                append("OaPT1000Oa`^13P1@AI[?g`1@A=[OdAoHgljA4Ao?WlBA7l1710007l100000000")
                append("ObM6000oOfMV?3QoBDD`O7a0BDDH@5A0BDD<@5A0BGeVO5ao@CQR?5Po00000000")
                append("Oc``000?Ogij70PO2D]??0Ph2DUM@7i`2DTg@7lh2GUj?0TO0C1870T?00000000")
                append("70<4001o?P<7?1QoHg43O;`h@GT0@:@LB@d0>:@hN@L0@?aoN@<0O7ao0000?000")
                append("OcH0001SOglLA7mg24TnK7ln24US>0PL24U140PnOgl0>7QgOcH0K71S0000A000")
                append("00H00000@Dm1S007@DUSg00?OdTnH7YhOfTL<7Yh@Cl0700?@Ah0300700000000")
                append("<008001QL00ZA41a@6HnI<1i@FHLM81M@@0LG81?O`0nC?Y7?`0ZA7Y300080000")
                append("O`082000Oh0827mo6>Hn?Wmo?6HnMb11MP08@C11H`08@FP0@@0004@000000000")
                append("00P00001Oab00003OcKP0006@6=PMgl<@440MglH@000000`@000001P00000000")
                append("Ob@8@@00Ob@8@Ga13R@8Mga172@8?PAo3R@827QoOb@820@0O`0007`0000007P0")
                append("O`000P08Od400g`<3V=P0G`673IP0`@3>1`00P@6O`P00g`<O`000GP800000000")
                append("?P9PL020O`<`N3R0@E4HC7b0@ET<ATB0@@l6C4B0O`H3N7b0?P01L3R000000020")
            }

            val sheet = BooleanArray(128 * 48)
            var px = 0
            var py = 0
            for (b in 0 until 1024 step 4) {
                val sym1 = data[b + 0].code - 48
                val sym2 = data[b + 1].code - 48
                val sym3 = data[b + 2].code - 48
                val sym4 = data[b + 3].code - 48
                val r = (sym1 shl 18) or (sym2 shl 12) or (sym3 shl 6) or sym4

                for (i in 0 until 24) {
                    val k = r and (1 shl i) != 0
                    if (k) sheet[px + py * 128] = true
                    if (++py == 48) {
                        px++
                        py = 0
                    }
                }
            }
            return sheet
        }

    }

    @JvmInline
    value class Pattern(val mask: Long) {
        constructor(b: Byte) : this(
            (b.toLong() or
                    (b.toLong() shl 8) or
                    (b.toLong() shl 16) or
                    (b.toLong() shl 24) or
                    (b.toLong() shl 32) or
                    (b.toLong() shl 40) or
                    (b.toLong() shl 48) or
                    (b.toLong() shl 56))
        )

        constructor(i: Int) : this((i.toLong() shl 32) or i.toLong())

        override fun toString() = mask.toULong().toString(2).padStart(64, '0')

        companion object {
            val SOLID = Pattern(-1L)
            val DOTTED = Pattern(0b01010101.toByte())
            val INCREASING = Pattern(0b00001111_11111011_11111101_11111101_11111011_11110111_11011110_11101101)
            val DEFAULT_PATTERN = SOLID
        }

        fun rotate() = Pattern((mask shl 1) or (mask ushr 63))
        fun rotateBackward() = Pattern((mask ushr 1) or (mask shl 63))
    }

}

private typealias P = Pair<Int, Int>

typealias FrameScope = SequenceScope<Unit>
typealias FrameIterator = Iterator<Unit>