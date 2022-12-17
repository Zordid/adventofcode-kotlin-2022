import com.github.ajalt.mordant.animation.progressAnimation
import utils.*
import kotlin.collections.set

typealias PointL = Pair<Long, Long>

class Day17 : Day(17, 2022) {

    val origin = 0L to 0L


    val cmds = inputAsString.toList()

    val rocks = ROCKS.split("\n\n").map { it.split("\n").map { it.toList() } }

    override fun part2(): Any? {


        val space = mutableMapOf<PointL, Char>()
        space[origin] = '+'
        repeat(7) { space[origin.right(it + 1)] = '-' }
        space[origin.right(8)] = '+'


        var firstDetectionAt: Long? = null
        var fdc: Long? = null
        var skipped =0L

        var topMost = 0L

        val p = aocTerminal.progressAnimation {
            percentage()
            completed()
            speed()
            timeRemaining()
        }

        var command = 0
        var ri = 0

        var block = -1L

        val goal = 1000000000000
        while(++block < goal) {
            if (block % 1000L==0L)
            p.update(block, 1000000000000)
            val r = rocks[ri++]
            ri%=rocks.size
            val h = r.size

//            log { "Top most $topMost" }
            repeat(3 + h) {
                val line = origin.up(-topMost+it+1)
//                log { "line $line" }
                space[line] = '|'
                repeat(7) { l ->
                    space[line.right(l + 1)] = '.'
                }
                space[line.right(8)] = '|'
            }
//            log { space.formatted() }

            var elPos = 3L to topMost - 3 - h
            require(space.insertOk(r, elPos)) { "Cannot insert at $elPos"}
            while(true) {
                val cmd = cmds[command++]
                command %= cmds.size

                val np = when(cmd) {
                    '<' -> elPos.left()
                    '>' -> elPos.right()
                    else -> error(cmd.toString())
                }
                if (space.insertOk(r, np))
                    elPos = np

                if (space.insertOk(r, elPos.down()))
                    elPos = elPos.down()
                else {
                    space.insert(r, elPos)
                    topMost = topMost.coerceAtMost(elPos.y)
                    break
                }
            }

//            if (block < 10)
//                log { space.formatted() }

            val cf = (topMost .. 0).firstOrNull { ft ->
                (1..7).all { space[it.toLong() to ft] == '#' }

            }
            if (cf!=null && cf == topMost) {
                println("Completely full line detected at command $command, rock $ri")
                println("$topMost - full at $cf")

                if (firstDetectionAt == null) {
                    firstDetectionAt = -topMost
                    fdc = block
                } else {
                    val diff = (-topMost)-firstDetectionAt
                    val blockDiff = block-fdc!!
                    while (block + blockDiff < goal) {
                        block += blockDiff
                        skipped += diff
                    }
                }

            }

            val full = (topMost .. 0).firstOrNull { ft ->
                (1..7).all { space[it.toLong() to ft] == '#' || space[it.toLong() to ft-1] == '#'}

            }
            if (full!=null) {
//                log{ "$block Found full line at $full \n ${space.formatted()}" }
                space.keys.filter { it.y > full }.forEach { space.remove(it) }
//                log { space.formatted() }
//                log { topMost }
            }



        }

        return -topMost + skipped
    }
}

private fun PointL.right(i: Int=1) = this.first+i to this.second
private fun PointL.left(i: Int=1) = this.first-i  to this.second
private fun PointL.up(i: Long=1L) = this.first to this.second-i
private fun PointL.down(i: Int=1) = this.first to this.second+i
val PointL.x get() = first
val PointL.y get() = second

operator fun PointL.plus(other: Point) = first+other.first to second+other.second

private fun MutableMap<PointL, Char>.insertOk(r: List<List<Char>>, i: PointL): Boolean {
//    log { "trying inserting at $i\n" + r.formatted() }
    val ra = r.area
    ra.forEach { p ->
        if ((r[p] == '#') && (this[i + p] != '.')) return false
    }
    return true
}

private fun MutableMap<PointL, Char>.insert(r: List<List<Char>>, i: PointL, c: Char = '#') {
//    log { "inserting at $i\n" + r.formatted() }
    val ra = r.area
    ra.forEach { p ->
        if (r[p] == '#') {
            this[i + p] =  c
        }
    }
}

fun main() {
    solve<Day17>(true) {

        """>>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>""" part1 3068 // part2 1514285714288

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