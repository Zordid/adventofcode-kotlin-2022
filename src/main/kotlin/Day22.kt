import utils.*
import utils.Direction4.Companion.DOWN
import utils.Direction4.Companion.LEFT
import utils.Direction4.Companion.RIGHT
import utils.Direction4.Companion.UP

const val OPEN = '.'
const val WALL = '#'
const val OUT = ' '

class Day22 : Day(22, 2022) {

    val p = inputAsGroups

    val m = p.first().map(String::toList).fixed(' ').show()
    val c = p.last().first() + " ".show()


    override fun part1(): Any? {
        alog { m.formatted() }

        val a = m.area
        var h = RIGHT
        var p = a.allPoints().first { m[it] == OPEN }

        val walk = c.extractAllIntegers()
        val turn = c.filter { !it.isDigit() }.toList()
        val op = walk.zip(turn)
        alog { op }
        alog { p }

        val cs = 50
        val cm = """
            | AB
            | C
            |DE
            |F
        """.trimMargin().split("\n").map { it.toList() }

        fun topLeft(f: Char) = cm.searchIndices(f).first() * cs

        for ((w, t) in op) {
            alog {
                m.formatted { pair, c ->
                    if (pair == p) {
                        h.toString().first().toString()
                    } else c.toString()
                }
            }
            alog { "Going $w times $h" }
            log { }
            for (j in 1..w) {
                var np = p + h
                if (np !in a || m[np] == OUT) {
                    val cr = p.y / cs
                    val cc = p.x / cs
                    val before = cm[cr to cc]
                    val pOnFace = p.x % cs to p.y % cs
                    check(before in 'A'..'F') { "$p $cc $cr Wrong face: '$before'" }
                    check(pOnFace in areaOf(origin, (cs - 1 to cs - 1)))
                    when (before) {
                        'A' -> {
                            when (h) {
                                UP -> {
                                    np = topLeft('F') + (0 to pOnFace.x)
                                    h = RIGHT
                                }

                                LEFT -> {
                                    np = topLeft('D') + (0 to cs - 1 - pOnFace.y)
                                    h = RIGHT
                                }

                                else -> error("A $h")
                            }
                        }

                        'B' -> {
                            when (h) {
                                RIGHT -> {
                                    np = topLeft('E') + (0 to cs - 1 - pOnFace.y)
                                    h = RIGHT
                                }

                                UP -> {
                                    np = topLeft('F') + (pOnFace.x to cs - 1)
                                }

                                DOWN -> {
                                    np = topLeft('C') + (cs - 1 to pOnFace.x)
                                    h = LEFT
                                }

                                else -> error("B $h")
                            }
                        }

                        'C' -> {
                            when (h) {
                                RIGHT -> {
                                    np = topLeft('B') + (pOnFace.y to cs - 1)
                                    h = UP
                                }

                                LEFT -> {
                                    np = topLeft('D') + (pOnFace.y to 0)
                                    h = DOWN
                                }

                                else -> error("C $h")
                            }
                        }

                        'E' -> {
                            when (h) {
                                RIGHT -> {
                                    np = topLeft('B') + (cs - 1 to cs - 1 - pOnFace.y)
                                    h = LEFT
                                }

                                DOWN -> {
                                    np = topLeft('F') + (cs - 1 to pOnFace.x)
                                    h = LEFT
                                }

                                else -> error("E $h")
                            }
                        }

                        'D' -> {
                            when (h) {
                                UP -> {
                                    np = topLeft('C') + (0 to pOnFace.x)
                                    h = RIGHT
                                }

                                LEFT -> {
                                    np = topLeft('A') + (0 to cs - 1 - pOnFace.y)
                                    h = RIGHT
                                }

                                else -> error("D $h")
                            }
                        }

                        'F' -> {
                            when (h) {
                                RIGHT -> {
                                    np = topLeft('E') + (pOnFace.y to cs - 1)
                                    h = UP
                                }

                                DOWN -> {
                                    np = topLeft('B') + (pOnFace.x to 0)
                                    h = DOWN
                                }

                                LEFT -> {
                                    np = topLeft('A') + (pOnFace.y to 0)
                                    h = DOWN
                                }

                                else -> error("F $h")
                            }
                        }
                    }


                }
                if (m[np] == WALL) break
                p = np
            }
            h = when (t) {
                'L' -> h.left
                'R' -> h.right
                else -> h
            }
        }
        val row = p.y + 1
        val col = p.x + 1
        val f = when (h) {
            RIGHT -> 0
            Direction4.DOWN -> 1
            LEFT -> 2
            else -> 3
        }
        alog { row }
        alog { col }
        alog { f }
        return 1000 * row + 4 * col + f
    }
}

fun main() {
    solve<Day22>(true) {

        """
        ...#
        .#..
        #...
        ....
...#.......#
........#...
..#....#....
..........#.
        ...#....
        .....#..
        .#......
        ......#.

10R5L5R10L4R5L5""".trimIndent()

    }
}