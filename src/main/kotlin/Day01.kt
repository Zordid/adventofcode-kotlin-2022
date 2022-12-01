import utils.maxN
import utils.splitByNulls

class Day01 : Day(1, 2022, "Calorie Counting") {
    private val calories = input.map { it.toIntOrNull() }
    private val elvesCalories = calories.splitByNulls().map { it.sum() }

    override fun part1() = elvesCalories.max()

    override fun part2() = elvesCalories.maxN(3).sum()
    // Using pure Kotlin stdlib would be less efficient here because of sorting the complete list:
    // elvesCalories.sortedDescending().take(3).sum()
}

fun main() {
    solve<Day01>("""
        1000
        2000
        3000

        4000

        5000
        6000

        7000
        8000
        9000

        10000
    """.trimIndent(), 24000, 45000)
}