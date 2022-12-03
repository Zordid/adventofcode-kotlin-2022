class Day03 : Day(3, 2022, "Rucksack Reorganization") {

    override fun part1(): Int =
        input.sumOf { rucksack ->
            val half = rucksack.length / 2
            val itemsIn1 = rucksack.take(half).toSet()
            val itemsIn2 = rucksack.drop(half).toSet()

            val commonItem = (itemsIn1 intersect itemsIn2).single()
            commonItem.priority
        }

    override fun part2(): Int {
        val groups = input.chunked(3)
        return groups.sumOf { rucksacks ->
            val badge = rucksacks.map { it.toSet() }
                .reduce { commonItems, content -> commonItems intersect content }
                .single()

            badge.priority
        }
    }

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