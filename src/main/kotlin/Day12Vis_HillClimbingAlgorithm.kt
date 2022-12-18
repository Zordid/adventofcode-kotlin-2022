import utils.*
import java.awt.Color
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

fun main() {
    Day12Vis().start()
}

const val FPS = 60

class Day12Vis : KPixelGameEngine("AoC in Kotlin 2022 Day 12: Hill Climbing Algorithm") {

    val day = Day12()
    val heights = Grid2D.of(day.heights)
    val dimensions = day.heights.area

    val allowedMoves = { here: Point ->
        here.directNeighbors(dimensions).filter { heights[it] - heights[here] <= 1 }
    }
    val start = day.start
    val dest = day.dest

    val maxHeight = day.heights.flatten().max()
    val colors = createGradient(Color(0, 128, 0), Color.GREEN, maxHeight + 1)
    val colorsVisited = createGradient(Color(128, 0, 0), Color.RED, maxHeight + 1)

    val e = SearchEngineWithNodes(allowedMoves)
    val hist: List<Triple<Int, Collection<Pair<Int, Int>>, Collection<Pair<Int, Int>>>>

    val path: List<Point>

    val aYOffset = dimensions.height + 16
    val aOffset = 0 to aYOffset

    val aStarResult: SearchResult<Point>

    val aStarTrack = buildList {
        var maxLengthFound = 0
        aStarResult = AStarSearch(start,
            neighborNodes = allowedMoves,
            cost = { from, to -> 100 + (heights[from] - heights[to]).absoluteValue },
            costEstimation = { a, b -> (a manhattanDistanceTo b) * 100 },
            onExpand = {
                val path = it.path
                if (path.size > maxLengthFound) {
                    add(path to it.distance.keys.toList())
                    maxLengthFound = path.size
                }
            }
        ).search(dest)
    }

    val diff: Set<Point>

    init {
        hist = buildList {
            e.debugHandler = { level, nodesOnLevel, nodesVisited ->
                add(Triple(level, nodesOnLevel, nodesVisited.toList()))
                SearchControl.CONTINUE
            }
            path = e.bfsSearch(start) { it == dest }.path()
            println(path)
        }

        (aStarTrack.last().second.toSet() - hist.last().third).let {
            println("difference = $it")
            diff = it
        }
    }

    override fun onCreate() {
        construct(dimensions.width, (dimensions.height + 16) * 2, 6)
        limitFps = FPS
    }

    var current = 0
    var currentAst = 0

    val presentationAnimation = animation(3.seconds, FPS) {
        val h by animate(0, maxHeight)
        drawElevationMap(origin, h)
        drawElevationMap(aOffset, h)
    }

    val text1 = dimensions.height
    val text2 = text1 + 8

    var bfs = true
    var ast = true

    var maxAStar = 0

    override fun onUpdate(elapsedTime: Long, frame: Long) {
        presentationAnimation.update()
        if (presentationAnimation.ended) {
            blink(frame, start, dest)
            blink(frame, start + aOffset, dest + aOffset)
        }

        if (bfs) inFrame(frame, presentationAnimation.frames.toInt()..Int.MAX_VALUE) {
            fillRect(0, text1, screenWidth, 16, Color.BLACK)
            drawStringProp(0, text1, "Using BFS...")
            if (current < hist.size) {
                val (level, nodesOn, visited) = hist[current++]
                val p = visited + nodesOn
                drawStringProp(0, text2, "${p.size} visited  ")
                p.withIn(dimensions).forEach { draw(it, colorsVisited[heights[it]]) }
                nodesOn.withIn(dimensions).forEach { on -> draw(on, Color.YELLOW) }
            } else {
                val (level, nodesOn, visited) = hist.last()
                val p = visited + nodesOn
                drawStringProp(0, text2, "${p.size} visited / ${path.size - 1} steps ")
                p.withIn(dimensions).forEach { draw(it, colorsVisited[heights[it]]) }
                path.withIn(dimensions).forEach { draw(it, Color.YELLOW) }
            }
        }

        if (ast)
            inFrame(frame, (presentationAnimation.frames).toInt()..Int.MAX_VALUE) {
                fillRect(0, text1 + aYOffset, screenWidth, 16, Color.BLACK)
                if (currentAst < aStarTrack.size) {
                    drawStringProp(0, text1 + aYOffset, "Using A*...")
                    val (trace, visited) = aStarTrack[currentAst++]
                    drawStringProp(0, text2 + aYOffset, "${visited.size} visited  ")
                    maxAStar = trace.size
                    visited.withIn(dimensions).forEach { draw(it + aOffset, colorsVisited[heights[it]]) }
                    trace.withIn(dimensions).forEach { on -> draw(on + aOffset, Color.CYAN) }
                } else {
                    val solution = aStarResult.path
                    val visited = aStarResult.prev.keys

                    var up = 0
                    var down = 0
                    solution.zipWithNext().forEach { (from, to) ->
                        up += (heights[to] - heights[from]).coerceAtLeast(0)
                        down += (heights[from] - heights[to]).coerceAtLeast(0)
                    }

                    drawStringProp(0, text1 + aYOffset, "Up $up, down $down")
                    drawStringProp(0, text2 + aYOffset, "${visited.size} visited / ${solution.size - 1} steps")
                    visited.withIn(dimensions).forEach { draw(it + aOffset, colorsVisited[heights[it]]) }
                    solution.withIn(dimensions).forEach { on -> draw(on + aOffset, Color.CYAN) }
                    blink(frame, *diff.map { it + aOffset }.toTypedArray())
                }
            }
    }

    private fun blink(frame: Long, vararg points: Point) {
        val c = when (frame % 3) {
            0L -> Color.WHITE
            1L -> Color.YELLOW
            else -> Color.CYAN
        }
        points.forEach { draw(it, c) }
    }

    private fun drawElevationMap(offset: Point = origin, maxHeight: Int = Int.MAX_VALUE) {
        dimensions.forEach {
            draw(it + offset, colors[heights[it].coerceAtMost(maxHeight)])
        }
    }

    inline fun inFrame(frame: Long, frameRange: IntRange, block: (Long) -> Unit) {
        if (frame in frameRange) block(frame - frameRange.first)
    }

}

