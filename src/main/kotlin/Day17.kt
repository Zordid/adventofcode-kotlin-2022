import utils.*
import kotlin.collections.set

class Day17 : Day(17, 2022, "Pyroclastic Flow") {

    val cmds = inputAsString.toList()
    val rocks = ROCKS.split("\n\n").map { it.split("\n").map { it.toList() } }

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

            val rock = rocks[rockIndex]
            var rockPos = 3 to height + 3 + rock.size
            (height + 1..rockPos.y).forEach { row ->
                empty.forEachIndexed { i, c -> space[i to row] = c }
            }

            while (true) {
                val cmd = cmds[commandIndex++]
                commandIndex %= cmds.size

                val np = if (cmd == '<') rockPos.left() else rockPos.right()
                if (space.insertOk(rock, np))
                    rockPos = np

                if (space.insertOk(rock, rockPos.up()))
                    rockPos = rockPos.up()
                else {
                    space.insert(rock, rockPos)
                    height = height.coerceAtLeast(rockPos.y)
                    break
                }
            }
        }

        return skippedHeight + height
    }

    override fun part1() = playTetris(2022)
    override fun part2() = playTetris(1000000000000)

}


private fun MutableMap<Point, Char>.insertOk(r: List<List<Char>>, i: Point): Boolean {
//    log { "trying inserting at $i\n" + r.formatted() }
    val ra = r.area
    ra.forEach { p ->
        if ((r[p] == '#') && (this[i + (p.x to -p.y)] != '.')) return false
    }
    return true
}


private fun MutableMap<Point, Char>.insert(r: List<List<Char>>, i: Point, c: Char = '#') {
//    log { "inserting at $i\n" + r.formatted() }
    val ra = r.area
    ra.forEach { p ->
        if (r[p] == '#') {
            this[i + (p.x to -p.y)] = c
        }
    }
}

fun main() {
    solve<Day17>(true) {
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