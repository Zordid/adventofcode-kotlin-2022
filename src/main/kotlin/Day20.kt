import utils.CircularList
import utils.CircularListElement
import utils.toCircularList

class Day20 : Day(20, 2022, "Grove Positioning System") {

    val p = inputAsLongs

    override fun part1(): Long {
        val cl = p.toCircularList()
        val originalOrder = cl.entries.toList()

        cl.mix(originalOrder)

        return cl.sumCoordinates()
    }

    override fun part2(): Long {
        val key = 811589153L
        val cl = p.map { it * key }.toCircularList()
        val originalOrder = cl.entries.toList()

        repeat(10) {
            cl.mix(originalOrder)
        }

        return cl.sumCoordinates()
    }

    private fun CircularList<Long>.mix(order: Iterable<CircularListElement<Long>>) =
        order.forEach { element ->
            val v = element.value
            log { "Moving $element" }
            val moves = v.mod(size - 1)
            if (moves > 0) {
                val target = element.forward(moves)
                insertAfter(target, element)
            }
            log { this }
            log { }
        }

    private fun CircularList<Long>.sumCoordinates(): Long {
        val zero = first { it == 0L }
        return listOf(1000, 1000, 1000).fold(zero to 0L) { (v, s), step ->
            v.forward(step).let { it to s + it.value }
        }.second
    }

}

fun main() {
    solve<Day20>(true) {
        """
            1
            2
            -3
            3
            -2
            0
            4
        """.trimIndent() part1 3 part2 1623178306
    }
}