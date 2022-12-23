import utils.*
import utils.Direction4.Companion.DOWN
import utils.Direction4.Companion.LEFT
import utils.Direction4.Companion.RIGHT
import utils.Direction4.Companion.UP
import utils.dim3d.*

const val OPEN = '.'
const val WALL = '#'
const val OUT = ' '

class Day22 : Day(22, 2022, "Monkey Map") {

    val p = inputAsGroups

    val m = p.first().map(String::toList).fixed(' ').show()
    val area = m.area
    var startPos = area.allPoints().first { m[it] == OPEN }

    val op = p.last().single().let {
        val walk = it.sequenceContainedIntegers()
        val turn = "$it ".filterNot(Char::isDigit).asSequence()
        walk.zip(turn).toList()
    }.show("operations")

    override fun part1(): Int {
        log { m.formatted() }

        var p = startPos
        var h = Direction4.RIGHT

        alog { p }


        for ((w, t) in op) {
            log {
                m.formatted { pair, c ->
                    if (pair == p) {
                        h.toString().first().toString()
                    } else c.toString()
                }
            }
            log { "Going $w times $h" }
            log { }
            for (j in 1..w) {
                var np = p + h
                if (np !in area || m[np] == OUT) np = when (h) {
                    Direction4.RIGHT -> areaOf(0 to p.y, p).allPoints().toList()
                    Direction4.LEFT -> areaOf(p, area.right to p.y).allPoints().toList().reversed()
                    Direction4.UP -> areaOf(p.x to area.bottom, p).allPoints().toList().reversed()
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
        val f = when (h) {
            Direction4.RIGHT -> 0
            Direction4.DOWN -> 1
            Direction4.LEFT -> 2
            else -> 3
        }
        alog { row }
        alog { col }
        alog { f }
        return 1000 * row + 4 * col + f
    }

    class CubeOrigami(val paper: Grid<Char>) : Grid<Char> by paper {
        val paperArea = paper.area
        val faceDimension = gcd(paper.width, paper.height)
        val folding = run {
            var c = 'A'
            MutableGrid(paper.width / faceDimension, paper.height / faceDimension) {
                if (paper[it * faceDimension] != OUT) c++
                else ' '
            }.also { check(c == 'G') { "Not exactly 6 sides could be detected in the paper" } }
        } as Grid<Char>
        val faces = buildMap<Char, CubeFace> {
            this['A'] = CubeFace()
            this[' '] = CubeFace()
            val q = ArrayDeque(listOf('A'))
            while (q.isNotEmpty()) {
                val c = q.removeFirst()
                val i = folding.indexOfOrNull(c)!!
                val v = this[c]!!
                log { "$c vec $v" }
                folding.getOrNull(i.down())?.let {
                    if (it !in this) {
                        this[it] = v.down()
                        q.add(it)
                    }
                }
                folding.getOrNull(i.up())?.let {
                    if (it !in this) {
                        this[it] = v.up()
                        q.add(it)
                    }
                }
                folding.getOrNull(i.right())?.let {
                    if (it !in this) {
                        this[it] = v.right()
                        q.add(it)
                    }
                }
                folding.getOrNull(i.left())?.let {
                    if (it !in this) {
                        this[it] = v.left()
                        q.add(it)
                    }
                }
            }
            remove(' ')

            ('A'..'F').forEach { s ->
                alog { "$s opposite of ${this@buildMap.entries.single { it.value.faceVector == -this@buildMap[s]!!.faceVector }}" }
            }
        }
        val startingPositionOnPaper = paper.searchIndices { it != OUT }.first()

        fun walkOnPaper(from: Point, heading: Direction4): Pair<Point, Direction4> {
            val naive = from + heading
            if (naive in paperArea && paper[naive] != OUT) return naive to heading

            val cc = from.x / faceDimension
            val cr = from.y / faceDimension
            val before = folding[cc to cr]
            val onFace = from % faceDimension
            val face = faces[before]!!
            val toFace = when (heading) {
                UP -> face.up()
                DOWN -> face.down()
                LEFT -> face.left()
                else -> face.right()
            }
            alog { "Currently on $before - matching face will be $toFace" }
            val (after, real) = faces.entries.single { it.value.faceVector == toFace.faceVector }

            alog { "Walk from $before to $after! " }

            alog { "Need up to be ${toFace.up} but found ${real.up}" }
            alog { "Need right to be ${toFace.right} but found ${real.right}" }

            var rot = 0
            var rotUp = real.up
            var rotHeading = heading
            while (rotUp != toFace.up) {
                rotUp = rotUp.rotateAround(real.faceVector)
                rotHeading = rotHeading.left
                rot++
            }
            alog { "Achieved with $rot rotations" }

            val base = folding.indexOfOrNull(after)!! * faceDimension
            return (base + when (rot) {
                0 -> when (heading) {
                    DOWN -> onFace.x to 0
                    UP -> onFace.x to faceDimension - 1
                    LEFT -> faceDimension - 1 to onFace.y
                    else -> 0 to onFace.y
                } // no rot
                1 -> when (heading) {
                    LEFT -> onFace.y to 0
                    RIGHT -> onFace.y to faceDimension - 1
                    else -> error(heading)
                } // left
                2 -> when (heading) {
                    DOWN -> faceDimension - 1 - onFace.x to faceDimension - 1
                    UP -> faceDimension - 1 - onFace.x to 0
                    RIGHT -> faceDimension - 1 to faceDimension - 1 - onFace.y
                    else -> 0 to faceDimension - 1 - onFace.y
                } // opposite
                3 -> when (heading) {
                    DOWN -> faceDimension - 1 to onFace.x
                    UP -> 0 to onFace.x
                    RIGHT -> faceDimension - 1 - onFace.y to 0
                    else -> error("BOOM $heading")
                } // 3x left = right

                else -> error("rot == $rot")
            } to rotHeading).also { log { "on $after we are at ${it.first} ${it.second}" } }
        }

        override fun toString(): String {
            return """
                |Cube Origami with face dimension $faceDimension
                |${folding.formatted(showHeaders = false)}
            """.trimMargin()
        }

    }

    override fun part2(): Int {
        val cube = CubeOrigami(m)
        log { cube }

        var p = cube.startingPositionOnPaper
        var h = RIGHT

        val trace = cube.toMutableGrid()
        trace[p] = h.symbol

        for ((walk, turn) in op) {
            alog { "We are at $p and will walk $walk $h" }
            alog { trace.formatted() }
            for (w in 1..walk) {
                val (newP, newH) = cube.walkOnPaper(p, h)
                if (cube[newP] == WALL) break

                p = newP
                h = newH
                trace[p] = h.symbol
            }
            h = when (turn) {
                'L' -> h.left
                'R' -> h.right
                else -> h
            }
            trace[p] = h.symbol
            alog { "Now at $p heading $h" }
            alog { }
        }
        alog { "We are at $p heading $h" }
        alog { trace.formatted() }

        val f = when (h) {
            RIGHT -> 0
            DOWN -> 1
            LEFT -> 2
            else -> 3
        }
        return 1000 * (p.y + 1) + 4 * (p.x + 1) + f
    }

    fun part2W(): Any? {
        val a = m.area
        var h = RIGHT
        var p = a.allPoints().first { m[it] == OPEN }

        val cs = 50
        val cm = """
            | AB
            | C
            |DE
            |F
        """.trimMargin().split("\n").map { it.toList() }.fixed(' ')

        fun originOf(f: Char) = cm.searchIndices(f).first() * cs

        val trace = m.toMutableGrid()

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
                    if (pair == p) h.symbol.toString()
                    else c.toString()
                }
            }

            alog { "Will now go $w times $h, then turning $t" }
            alog {}

            for (j in 1..w) {
                var np = p + h
                if (np !in a || m[np] == OUT) {
                    val (op, oh) = when (before) {
                        'A' -> {
                            when (h) {
                                UP -> originOf('F') + (0 to pOnFace.x) to RIGHT
                                LEFT -> originOf('D') + (0 to cs - 1 - pOnFace.y) to RIGHT
                                else -> error("A $h")
                            }
                        }

                        'B' -> {
                            when (h) {
                                RIGHT -> originOf('E') + (cs - 1 to cs - 1 - pOnFace.y) to LEFT
                                UP -> originOf('F') + (pOnFace.x to cs - 1) to UP
                                DOWN -> originOf('C') + (cs - 1 to pOnFace.x) to LEFT
                                else -> error("B $h")
                            }
                        }

                        'C' -> {
                            when (h) {
                                RIGHT -> originOf('B') + (pOnFace.y to cs - 1) to UP
                                LEFT -> originOf('D') + (pOnFace.y to 0) to DOWN
                                else -> error("C $h")
                            }
                        }

                        'D' -> {
                            when (h) {
                                UP -> originOf('C') + (0 to pOnFace.x) to RIGHT
                                LEFT -> originOf('A') + (0 to cs - 1 - pOnFace.y) to RIGHT
                                else -> error("D $h")
                            }
                        }

                        'E' -> {
                            when (h) {
                                RIGHT -> originOf('B') + (cs - 1 to cs - 1 - pOnFace.y) to LEFT
                                DOWN -> originOf('F') + (cs - 1 to pOnFace.x) to LEFT
                                else -> error("E $h")
                            }
                        }


                        else -> { // 'F'
                            when (h) {
                                RIGHT -> originOf('E') + (pOnFace.y to cs - 1) to UP
                                DOWN -> originOf('B') + (pOnFace.x to 0) to DOWN
                                LEFT -> originOf('A') + (pOnFace.y to 0) to DOWN
                                else -> error("F $h")
                            }
                        }
                    }
                    np = op
                    if (m[op] == OPEN)
                        h = oh

                }
                if (m[np] == WALL) break
                trace[np] = h.symbol
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
            DOWN -> 1
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

10R5L5R10L4R5L5""".trimIndent() /*part1 6032*/ part2 5031

    }
}

data class CubeFace(val up: Point3D = unitVecZ, val right: Point3D = unitVecY) {
    val faceVector = right x up

    fun down() = CubeFace(up.rotateAround(right), right)
    fun up() = CubeFace(up.rotateAround(-right), right)
    fun left() = CubeFace(up, right.rotateAround(-up))
    fun right() = CubeFace(up, right.rotateAround(up))
}

infix fun CubeFace.opposite(other: CubeFace) = faceVector == -other.faceVector