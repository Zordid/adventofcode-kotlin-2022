class Day03 : Day(3, 2022, "Rucksack Reorganization") {

    override fun part1() =
        input
            .map { rucksack -> rucksack.chunked(rucksack.length / 2) { comp -> comp.toSet() } }
            .map { (comp1, comp2) -> (comp1 intersect comp2).single() }
            .sumOf { duplicateItem -> duplicateItem.priority }

    override fun part2() =
        input
            .chunked(3) { rucksacks -> rucksacks.map { it.toSet() } }
            .map { elves -> elves.reduce { common, elf -> common intersect elf }.single() }
            .sumOf { badge -> badge.priority }

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