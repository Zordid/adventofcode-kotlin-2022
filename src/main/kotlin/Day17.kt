import utils.*
import kotlin.collections.set

class Day17 : Day(17, 2022, "Pyroclastic Flow") {

    val cmds = inputAsString.toList()
    val rocks = ROCKS.split("\n\n").map { it.split("\n").map { it.toList() } }

    override fun part1() = playTetris(2022)
    override fun part2() = playTetris(1000000000000)

    fun playTetris(rocksToDrop: Long): Long {
        val bottom = "+-------+".toList()
        val empty = "+.......+".toList()
        val space = listOf(bottom).toMapGrid().toMutableMap()

        var commandIndex = 0
        var rocksDropped = -1L
        var height = 0
        var skippedHeight = 0L

        fun pattern(size: Int = 5) = (height downTo (height - (size - 1))).flatMap { r ->
            (1..7).map { c -> space[c to r] }
        }

        val mem = mutableMapOf<Any, MutableMap<Pair<Int, Int>, Pair<Long, Int>>>()

        while (++rocksDropped < rocksToDrop) {
            val rockIndex = (rocksDropped % rocks.size).toInt()
            val rock = rocks[rockIndex]

//            val necessary = rock.test(space, height)
//            log { necessary }
            val p = pattern(6)
            val m = mem.getOrPut(p) { mutableMapOf() }
            if ((rockIndex to commandIndex) in m) {
                val last = m[rockIndex to commandIndex]!!
                val rockDiff = rocksDropped - last.first
                val heightDiff = height - last.second
                alog { "I remember this pattern $p" }
                alog { "loop length is $rockDiff, left rocks: ${rocksToDrop - rocksDropped}" }
                val skipping = (rocksToDrop - rocksDropped) / rockDiff
                rocksDropped += skipping * rockDiff
                skippedHeight += skipping * heightDiff
                mem.clear()
            } else m[rockIndex to commandIndex] = rocksDropped to height

            var rockPos = 3 to height + 3 + rock.size
            (height + 1..rockPos.y).forEach { row ->
                empty.forEachIndexed { i, c -> space[i to row] = c }
            }

            while (true) {
                val cmd = cmds[commandIndex++]
                commandIndex %= cmds.size

                val np = if (cmd == '<') rockPos.left() else rockPos.right()
                if (rock.fits(space, np))
                    rockPos = np

                if (rock.fits(space, rockPos.fall()))
                    rockPos = rockPos.fall()
                else {
                    rock.insert(space, rockPos)
                    height = height.coerceAtLeast(rockPos.y)
                    break
                }
            }
        }

        return skippedHeight + height
    }

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

    private fun Grid<Char>.test(space: MapGrid<Char>, dropHeight: Int): Int {
        fun neighbor(p: Point) =
            listOf(p.fall(), p.left(), p.right()).filter { this.fits(space, it) }
        return SearchEngineWithNodes(::neighbor).completeAcyclicTraverse(3 to dropHeight + this.size).minOf {
            it.nodesOnLevel.minOf { it.y }
        }-this.size
    }

}

fun main() {
    solve<Day17> {
        """>>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>""" part1 3068 part2 1514285714288
    }
}

const val ROCKS = """####

.#.
###
.#.

..#
..#
###

#
#
#
#

##
##"""