import utils.Graph
import utils.breadthFirstSearch
import utils.dim3d.Point3D
import utils.dim3d.boundingCube
import utils.dim3d.contains
import utils.dim3d.plus
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3

class Day18 : Day(18, 2022, "Boiling Boulders") {

    val p = input.map { it.extractAllIntegers().let { (x, y, z) -> Point3D(x, y, z) } }.toSet()

    val d = """
        1 0 0
        -1 0 0 
        0 1 0
        0 -1 0
        0 0 1
        0 0 -1
    """.trimIndent().split("\n").map { it.extractAllIntegers().let { (x, y, z) -> Point3D(x, y, z) } }

    override fun part1(): Int {
        var count = 0
        val g = object : Graph<Point3D> {
            override fun neighborsOf(node: Point3D): Collection<Point3D> {
                return d.map { node + it }.filter { it in p }.also { count += d.size - it.size }
            }
        }

        val notVisited = p.toMutableSet()
        while (notVisited.isNotEmpty()) {
            val f = notVisited.first()
            val all = g.breadthFirstSearch(f) { false }.nodesVisited
            notVisited -= all
        }

        return count
    }

    override fun part2(): Int {
        val outside = mutableSetOf<Point3D>()
        val inside = mutableSetOf<Point3D>()
        val bounds = p.boundingCube()

        val spillOut = object : Graph<Point3D> {
            override fun neighborsOf(node: Point3D): Collection<Point3D> {
                return d.map { node + it }.filter { it !in p }
            }
        }

        fun Point3D.isOutside(): Boolean {
            if (this in inside) return false
            if (this in outside) return true

            val flood = spillOut.breadthFirstSearch(this) { it !in bounds || it in outside || it in inside }
            if (flood.solution == null || flood.solution in inside) {
                inside += flood.nodesVisited
                return false
            }
            outside += flood.nodesVisited
            return true
        }

        var count = 0

        val sg = object : Graph<Point3D> {
            override fun neighborsOf(node: Point3D): Collection<Point3D> {
                val (connected, notConnected) = d.map { node + it }.partition { it in p }
                return connected.also {
                    count += notConnected.count { p ->
                        p.isOutside()
                    }
                }
            }
        }

        val notVisited = p.toMutableSet()
        while (notVisited.isNotEmpty()) {
            val f = notVisited.first()
            val all = sg.breadthFirstSearch(f) { false }.nodesVisited
            notVisited -= all
        }

        return count
    }

}

fun main() {
    solve<Day18> {
        """
            2,2,2
            1,2,2
            3,2,2
            2,1,2
            2,3,2
            2,2,1
            2,2,3
            2,2,4
            2,2,6
            1,2,5
            3,2,5
            2,1,5
            2,3,5
        """.trimIndent() part1 64 part2 58
    }
}