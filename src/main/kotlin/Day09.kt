import utils.*

class Day09 : Day(9, 2022, "Rope Bridge") {

    val moves = input.map {
        it[0].let(Direction4::interpret) to it.substring(2).toInt()
    }

    override fun part1(): Int {
        val headPos = moves.moveHead()
        val tailPos = headPos.moveTail()
        return tailPos.countDistinct()
    }

    override fun part2(): Int {
        val headPos = moves.moveHead()
        val lastTail = (1..9).fold(headPos) { prev, _ ->
            prev.moveTail()
        }
        return lastTail.countDistinct()
    }

    fun List<Pair<Direction, Int>>.moveHead() =
        flatMap { m -> (1..m.second).map { m.first } } // flatten moves, e.g. R 4 -> R,R,R,R
            .runningFold(origin) { head, move -> head + move }

    fun List<Point>.moveTail() = runningFold(first()) { tail, head ->
        val distance = head - tail
        if (distance.x !in -1..1 || distance.y !in -1..1) // touching or not?
            tail + distance.sign
        else
            tail
    }

    private fun Iterable<*>.countDistinct() = toSet().size

}

fun main() {
    solve<Day09> {
        """
            R 4
            U 4
            L 3
            D 1
            R 4
            D 1
            L 5
            R 2
        """.trimIndent() part1 13

        """
            R 5
            U 8
            L 8
            D 3
            R 17
            D 10
            L 25
            U 20
        """.trimIndent() part2 36
    }
}