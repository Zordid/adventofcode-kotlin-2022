class Day03 : Day(3, 2022, "Rucksack Reorganization") {

    override fun part1() =
        input
            .map { rucksack -> rucksack.chunked(rucksack.length / 2) }
            .sumOf { compartments -> compartments.singleCommonItem().priority }

    override fun part2() =
        input
            .chunked(3)
            .sumOf { rucksacks -> rucksacks.singleCommonItem().priority }

    private fun Iterable<CharSequence>.singleCommonItem() =
        asSequence().map(CharSequence::toSet).reduce(Set<Char>::intersect).single()

    private val Char.priority
        get() = when (this) {
            in 'a'..'z' -> this - 'a' + 1
            in 'A'..'Z' -> this - 'A' + 27
            else -> error("$this is not an item")
        }

}

fun main() {
    solve<Day03>(
        """
        vJrwpWtwJgWrhcsFMMfFFhFp
        jqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL
        PmmdzqPrVvPwwTWBwg
        wMqvLMZHhHMvwLHjbvcjnnSBnvTQFn
        ttgJtRGJQctTZtZT
        CrZsJsPPZsGzwwsLwLmpwMDw
    """.trimIndent(), 157, 70
    )
}