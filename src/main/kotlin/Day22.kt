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

    val map = p.first().map(String::toList).fixed(' ').show()

    val op = p.last().single().let {
        val walk = it.sequenceContainedIntegers()
        val turn = "$it ".filterNot(Char::isDigit).asSequence()
        walk.zip(turn).toList()
    }.show("operations")

    override fun part1(): Int {
        log { map.formatted() }
        val area = map.area
        val startPos = map.searchIndices(OPEN).first()

        var p = startPos
        var h = RIGHT

        for ((w, t) in op) {
            for (j in 1..w) {
                var np = p + h
                if (np !in area || map[np] == OUT) np = when (h) {
                    RIGHT -> areaOf(0 to p.y, p).allPoints().toList()
                    LEFT -> areaOf(p, area.right to p.y).allPoints().toList().reversed()
                    UP -> areaOf(p.x to area.bottom, p).allPoints().toList().reversed()
                    else -> areaOf(p.x to 0, p).allPoints().toList()
                }.first { map[it] != OUT }

                if (map[np] == WALL) break
                p = np
            }
            h = when (t) {
                'L' -> h.left
                'R' -> h.right
                else -> h
            }
        }
        return result(p, h)
    }

    override fun part2(): Int {
        val cube = CubeOrigami(map)
        log { cube }

        var p = cube.startingPositionOnPaper
        var h = RIGHT

        val trace = cube.toMutableGrid()
        trace[p] = h.symbol

        for ((walk, turn) in op) {
            log { "We are at $p and will walk $walk x $h" }
            log { trace.formatted() }
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
            log { "Now at $p heading $h\n" }
        }
        log { trace.formatted() }

        return result(p, h)
    }

    private fun result(p: Point, h: Direction4) = 1000 * (p.y + 1) + 4 * (p.x + 1) + when (h) {
        RIGHT -> 0
        DOWN -> 1
        LEFT -> 2
        else -> 3
    }

}

fun main() {
    solve<Day22> {
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

10R5L5R10L4R5L5""".trimIndent() part1 6032 part2 5031
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
        val q = ArrayDeque(listOf('A'))
        while (q.isNotEmpty()) {
            val c = q.removeFirst()
            val i = folding.indexOfOrNull(c)!!
            val v = this[c]!!
            log { "$c vec $v" }
            i.directNeighbors(folding.area).map { it to folding[it] }
                .filter { (_, c) -> c != ' ' && c !in this }
                .forEach { (p, c) ->
                    this[c] = when (Direction4.ofVector(i, p)) {
                        DOWN -> v.down()
                        UP -> v.up()
                        LEFT -> v.left()
                        else -> v.right()
                    }
                    q.add(c)
                }
        }

        ('A'..'F').forEach { s ->
            log { "$s opposite of ${this@buildMap.entries.single { it.value.faceVector == -this@buildMap[s]!!.faceVector }}" }
        }
    }

    val startingPositionOnPaper = paper.searchIndices(OPEN).first()

    fun walkOnPaper(from: Point, heading: Direction4): Pair<Point, Direction4> {
        // first, simply try to stay on the paper
        val onPaper = from + heading
        if (onPaper in paperArea && paper[onPaper] != OUT) return onPaper to heading

        // when leaving the paper area, consider the cube faces and find the destination face
        val fromFaceId = folding[from / faceDimension]
        val face = faces[fromFaceId] ?: error("No face for ID '$fromFaceId'")
        val facePos = from % faceDimension

        val requiredFace = when (heading) {
            UP -> face.up()
            DOWN -> face.down()
            LEFT -> face.left()
            else -> face.right()
        }
        log { "Currently on $fromFaceId($face) - required face is $requiredFace" }
        val (toFaceId, toFace) = faces.entries.single { it.value.faceVector == requiredFace.faceVector }

        log { "Walk from $fromFaceId to $toFaceId! " }
        log { "Need up to be ${requiredFace.up} but found ${toFace.up}" }

        var rot = 0
        var rotUp = toFace.up
        var rotHeading = heading
        while (rotUp != requiredFace.up) {
            rotUp = rotUp.rotateAround(toFace.faceVector)
            rotHeading = rotHeading.left
            rot++
        }
        log { "Achieved with $rot rotations" }

        val last = faceDimension - 1

        val base = folding.indexOfOrNull(toFaceId)!! * faceDimension

        return (base + with(facePos) {when (heading to rotHeading) {
            DOWN to DOWN -> x to 0
            UP to UP -> x to last
            LEFT to LEFT -> last to y
            RIGHT to RIGHT -> 0 to y

            RIGHT to UP -> y to last
            RIGHT to DOWN -> last - y to 0
            RIGHT to LEFT -> last to last - y

            DOWN to UP -> last - x to last
            DOWN to LEFT -> last to x

            LEFT to RIGHT -> 0 to last - y
            LEFT to DOWN -> y to 0

            UP to RIGHT -> 0 to x

            else -> error("unsupported rotation $heading to $rotHeading (x $rot)")
        }} to rotHeading)

        return (base + when (rot) {
//            0 -> when (heading) {
//                DOWN -> facePos.x to 0
//                UP -> facePos.x to last
//                LEFT -> last to facePos.y
//                else -> 0 to facePos.y
//            } // no rot
//            1 -> when (heading) {
//                LEFT -> facePos.y to 0
//                RIGHT -> facePos.y to last
//                else -> error(heading)
//            } // left
            2 -> when (heading) {
//                DOWN -> last - facePos.x to last
                UP -> last - facePos.x to 0
//                RIGHT -> last to last - facePos.y
//                else -> 0 to last - facePos.y
            } // opposite
            3 -> when (heading) {
//                DOWN -> last to facePos.x
//                UP -> 0 to facePos.x
//                RIGHT -> last - facePos.y to 0
                else -> error("BOOM $heading")
            } // 3x left = right

            else -> error("rot == $rot")
        } to rotHeading).also { log { "on $toFaceId we are at ${it.first} ${it.second}" } }
    }

    override fun toString(): String {
        return """
                |Cube Origami with face dimension $faceDimension
                |${folding.formatted(showHeaders = false)}
            """.trimMargin()
    }

}