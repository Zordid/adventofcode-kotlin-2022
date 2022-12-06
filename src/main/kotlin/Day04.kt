class Day04 : Day(4, 2022, "Camp Cleanup") {

    private val p = input.map {
        it.split(',', '-').map(String::toInt)
            .let { (a, b, c, d) ->
                (a..b) to (c..d)
            }
    }

    override fun part1() = p.count { (f, s) -> f in s || s in f }

    override fun part2() = p.count { (f, s) -> f overlaps s }

    private operator fun IntRange.contains(other: IntRange) =
        first >= other.first && last <= other.last

    private infix fun IntRange.overlaps(other: IntRange) =
        first <= other.last && last >= other.first

}

fun main() {
    solve<Day04> {
        """
        2-4,6-8
        2-3,4-5
        5-7,7-9
        2-8,3-7
        6-6,4-6
        2-6,4-8
        """.trimIndent()(2, 4)
    }
}