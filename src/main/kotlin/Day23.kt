import utils.*
import utils.Direction8.*

class Day23 : Day(23, 2022) {

    val p = inputAsGrid.toMapGrid('.').show()

    override fun part2(): Any? {

        var elves = p.keys

        var pdir = listOf(
            listOf(NORTH, NORTHEAST, NORTHWEST),
            listOf(SOUTH, SOUTHEAST, SOUTHWEST),
            listOf(WEST, NORTHWEST, SOUTHWEST),
            listOf(EAST, NORTHEAST, SOUTHEAST)
        ).asInfiniteSequence().windowed(4, 1).iterator()

        log { elves.plot(off = ".") }

        var count = 0
       while(true) {
           count++
            check(elves.size == p.size)

            val propOrder = pdir.next()
            log { "P order is $propOrder" }
            val proposed = buildMap<Point, Point> {
                elves.forEach { e ->
                    if (e.surroundingNeighbors().any { it in elves }) {
                        val myp = propOrder.firstOrNull { pn ->
                            pn.none { (e + it) in elves }
                        }
                        if (myp != null)
                            put(e, e + myp.first())
                        else
                            put(e, e)
                    } else put(e,e)

                }
            }
            log { proposed }

            val ne = mutableSetOf<Point>()
            val allprop = proposed.values.groupingBy { it }.eachCount()
            var moved = false
            proposed.forEach { (e, p) ->
                if (allprop[p]!! == 1) {
                    ne += p
                    moved=true
                } else ne += e
            }

           if (ne == elves) return count
            elves = ne




            log { elves.plot(off = ".") }
        }

        return elves.boundingArea()!!.size - elves.size
    }

}

fun main() {
    solve<Day23>(true) {



        """
            ....#..
            ..###.#
            #...#.#
            .#...##
            #.###..
            ##.#.##
            .#..#..
        """.trimIndent() part1 110 part2 20

    }
}