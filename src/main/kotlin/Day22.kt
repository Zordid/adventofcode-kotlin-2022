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
        var h = Direction4.RIGHT
        var p = a.allPoints().first { m[it] == OPEN }

        val walk = c.extractAllIntegers()
        val turn = c.filter { !it.isDigit() }.toList()
        val op = walk.zip(turn)
        alog { op }
        alog { p }


        for ((w, t) in op) {
            log { m.formatted { pair, c ->
                if (pair==p) {
                    h.toString().first().toString()
                } else c.toString()
            } }
            log { "Going $w times $h" }
            log {  }
            for (j in 1..w) {
                var np = p + h
                if (np !in a || m[np] == OUT) np = when (h) {
                    Direction4.RIGHT -> areaOf(0 to p.y, p).allPoints().toList()
                    Direction4.LEFT -> areaOf(p, a.right to p.y).allPoints().toList().reversed()
                    Direction4.UP -> areaOf(p.x to a.bottom, p).allPoints().toList().reversed()
                    else -> areaOf(p.x to 0, p).allPoints().toList()
                }.first { m[it] != OUT }

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
        val f = when(h) {
            Direction4.RIGHT -> 0
            Direction4.DOWN -> 1
            Direction4.LEFT -> 2
            else -> 3
        }
        alog { row}
        alog { col}
        alog { f}
        return 1000*row + 4* col + f
    }

    override fun part2(): Any? {
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
        """.trimMargin().split("\n").map { it.toList() }.fixed(' ')

        fun topLeft(f: Char) = cm.searchIndices(f).first() * cs

        val trace = m.toMutableGrid()

        fun Direction4.arrow() = when(this) {
            UP -> '^'
            DOWN -> 'v'
            LEFT -> '<'
            else -> '>'
        }

        for ((w, t) in op) {
            val cc = p.x / cs
            val cr = p.y / cs
            val before = cm[cc to cr]
            val pOnFace = p.x % cs to p.y % cs
            check(before in 'A'..'F') { "$p $cc $cr Wrong face: '$before'" }
            check(pOnFace in areaOf(origin, (cs - 1 to cs - 1)))

            alog { "We are on face $before on pos $pOnFace facing $h" }
            alog {
                trace.formatted { pair, c ->
                    if (pair == p) {
                        h.arrow().toString()
                    } else c.toString()
                }
            }

            alog { "Will now go $w times $h, then turning $t" }
            alog {}

            for (j in 1..w) {
                var np = p + h
                var nh = h
                if (np !in a || m[np] == OUT) {
                    when (before) {
                        'A' -> {
                            when (h) {
                                UP -> {
                                    np = topLeft('F') + (0 to pOnFace.x)
                                    nh = RIGHT
                                }

                                LEFT -> {
                                    np = topLeft('D') + (0 to cs - 1 - pOnFace.y)
                                    nh = RIGHT
                                }

                                else -> error("A $h")
                            }
                        }

                        'B' -> {
                            when (h) {
                                RIGHT -> {
                                    np = topLeft('E') + (cs - 1 to cs - 1 - pOnFace.y)
                                    nh = LEFT
                                }

                                UP -> {
                                    np = topLeft('F') + (pOnFace.x to cs - 1)
                                    nh = UP
                                }

                                DOWN -> {
                                    np = topLeft('C') + (cs - 1 to pOnFace.x)
                                    nh = LEFT
                                }

                                else -> error("B $h")
                            }
                        }

                        'C' -> {
                            when (h) {
                                RIGHT -> {
                                    np = topLeft('B') + (pOnFace.y to cs - 1)
                                    nh = UP
                                }

                                LEFT -> {
                                    np = topLeft('D') + (pOnFace.y to 0)
                                    nh = DOWN
                                }

                                else -> error("C $h")
                            }
                        }


                        'D' -> {
                            when (h) {
                                UP -> {
                                    np = topLeft('C') + (0 to pOnFace.x)
                                    nh = RIGHT
                                }

                                LEFT -> {
                                    np = topLeft('A') + (0 to cs - 1 - pOnFace.y)
                                    nh = RIGHT
                                }

                                else -> error("D $h")
                            }
                        }

                        'E' -> {
                            when (h) {
                                RIGHT -> {
                                    np = topLeft('B') + (cs - 1 to cs - 1 - pOnFace.y)
                                    nh = LEFT
                                }

                                DOWN -> {
                                    np = topLeft('F') + (cs - 1 to pOnFace.x)
                                    nh = LEFT
                                }

                                else -> error("E $h")
                            }
                        }


                        'F' -> {
                            when (h) {
                                RIGHT -> {
                                    np = topLeft('E') + (pOnFace.y to cs - 1)
                                    nh = UP
                                }

                                DOWN -> {
                                    np = topLeft('B') + (pOnFace.x to 0)
                                    nh = DOWN
                                }

                                LEFT -> {
                                    np = topLeft('A') + (pOnFace.y to 0)
                                    nh = DOWN
                                }

                                else -> error("F $h")
                            }
                        }
                    }
                    if(m[np]==OPEN)
                        h = nh

                }
                if (m[np] == WALL) break
                trace[np] = h.arrow()
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

10R5L5R10L4R5L5""".trimIndent() part1 6032

    }
}