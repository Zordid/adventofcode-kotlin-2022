class Day03 : Day(3, 2022, "Rucksack Reorganization") {

    override fun part1() =
        input.asSequence()
            .map { rucksack -> rucksack.chunked(rucksack.length / 2) }
            .sumOf { compartments -> compartments.singleCommonItem().priority }

    override fun part2() =
        input.asSequence()
            .chunked(3)
            .sumOf { rucksacks -> rucksacks.singleCommonItem().priority }

    private fun Iterable<CharSequence>.singleCommonItem() =
        asSequence().map(CharSequence::toSet).reduce(Set<Char>::intersect).single()

    private val Char.priority get() = prioritiesInOrder.indexOf(this)
    private val prioritiesInOrder = listOf('-') + ('a'..'z') + ('A'..'Z')

}

fun main() {
    solve<Day03> {
        """
        vJrwpWtwJgWrhcsFMMfFFhFp
        jqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL
        PmmdzqPrVvPwwTWBwg
        wMqvLMZHhHMvwLHjbvcjnnSBnvTQFn
        ttgJtRGJQctTZtZT
        CrZsJsPPZsGzwwsLwLmpwMDw
        """.trimIndent()(157, 70)
    }
}