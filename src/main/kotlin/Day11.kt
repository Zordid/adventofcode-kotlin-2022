@file:Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")

import utils.lcm
import utils.product

typealias WorryLevel = Long

fun String.toWorryLevel(): WorryLevel? = toLongOrNull()
infix fun WorryLevel.divisibleBy(divisor: Int) = this % divisor == 0L

data class Monkey(
    val operation: (WorryLevel) -> WorryLevel,
    val testDivisor: Int,
    val ifTrue: Int,
    val ifFalse: Int,
)

class Day11 : Day(11, 2022, "Monkey in the Middle") {

    val monkeys = inputAsGroups.map(::createMonkeyFromPuzzle)
    val startItems: List<List<WorryLevel>> = inputAsGroups.map { it[1].extractAllNumbers() }

    context (PlanetContext)
    private fun Monkey.playWithItemOfWorryLevel(worryLevel: WorryLevel): Pair<WorryLevel, Int> {
        val newWorryLevel = operation(worryLevel).treatOnThisPlanet()
        val throwTo = if (newWorryLevel divisibleBy testDivisor) ifTrue else ifFalse
        return newWorryLevel to throwTo
    }

    context (PlanetContext)
    fun letTheMonkeysPlay(rounds: Int): List<Int> {
        val inspections = MutableList(monkeys.size) { 0 }
        val currentState = startItems.map { it.toMutableList() }

        repeat(rounds) {
            monkeys.forEachIndexed { idx, monkey ->
                val currentlyHolds = currentState[idx]

                currentlyHolds.forEach {
                    inspections[idx]++
                    val (newLevel, toMonkey) = monkey.playWithItemOfWorryLevel(it)
                    currentState[toMonkey] += newLevel
                }
                currentlyHolds.clear()
            }
        }

        return inspections
    }

    override fun part1(): Long {
        with(DivisionPlanet(3)) {
            val totalInspections = letTheMonkeysPlay(20)
            return totalInspections.sortedDescending().take(2).product()
        }
    }

    override fun part2(): Long {
        val commonModulus = monkeys.map { it.testDivisor }.lcm().toInt()
        println("All monkeys happily live together in a modulus $commonModulus world!")

        with(ModulusPlanet(commonModulus)) {
            val totalInspections = letTheMonkeysPlay(10_000)
            return totalInspections.sortedDescending().take(2).product()
        }
    }

    interface PlanetContext {
        fun WorryLevel.treatOnThisPlanet(): WorryLevel
    }

    class DivisionPlanet(val divisor: Int) : PlanetContext {
        override fun WorryLevel.treatOnThisPlanet(): WorryLevel = this / divisor
    }

    class ModulusPlanet(val modulus: Int) : PlanetContext {
        override fun WorryLevel.treatOnThisPlanet(): WorryLevel = this % modulus
    }

}

fun main() {
    solve<Day11> {

        day11DemoInput part1 10605 part2 2713310158

    }
}

private fun createMonkeyFromPuzzle(s: List<String>): Monkey {
    val (o, t, tr, fa) = s.drop(2)
    return Monkey(
        createOperation(o.substringAfter("new = ")),
        t.extractAllIntegers().single(),
        tr.extractAllIntegers().single(),
        fa.extractAllIntegers().single(),
    )
}

private fun createOperation(s: String): (WorryLevel) -> WorryLevel =
    s.split(" ").let { (_, operator, op2) ->
        when (operator) {
            "+" -> { old -> old + (op2.toWorryLevel() ?: old) }
            "*" -> { old -> old * (op2.toWorryLevel() ?: old) }
            else -> error(s)
        }
    }

val day11DemoInput = """
Monkey 0:
  Starting items: 79, 98
  Operation: new = old * 19
  Test: divisible by 23
    If true: throw to monkey 2
    If false: throw to monkey 3

Monkey 1:
  Starting items: 54, 65, 75, 74
  Operation: new = old + 6
  Test: divisible by 19
    If true: throw to monkey 2
    If false: throw to monkey 0

Monkey 2:
  Starting items: 79, 60, 97
  Operation: new = old * old
  Test: divisible by 13
    If true: throw to monkey 1
    If false: throw to monkey 3

Monkey 3:
  Starting items: 74
  Operation: new = old + 3
  Test: divisible by 17
    If true: throw to monkey 0
    If false: throw to monkey 1
""".trimIndent()