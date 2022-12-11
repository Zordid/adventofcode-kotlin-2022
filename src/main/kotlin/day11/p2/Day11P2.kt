@file:Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")

package day11.p2

import Day
import day11DemoInput
import extractAllIntegers
import extractAllNumbers
import solve
import utils.lcm
import utils.product

/**
 * one "problem" in Kotlin is that once you started with Int numbers and *then* need to deal with larger
 * types, it is a nightmare to modify the code... a typealias can help, but it's still not "free" at all due
 * to functions needed like [toInt] vs [toLong]
 */
typealias WorryLevel = Long

fun String.toWorryLevel(): WorryLevel? = toLongOrNull()
infix fun WorryLevel.divisibleBy(divisor: Int) = this % divisor == 0L

data class Monkey(
    val id: Int,
    val operation: (WorryLevel) -> WorryLevel,
    val testDivisor: Int,
    val ifTrue: Int,
    val ifFalse: Int,
)

class Day11P2 : Day(11, 2022, "Monkey in the Middle") {

    val monkeys = inputAsGroups.mapIndexed(::createMonkeyFromPuzzle)
    val startItems: List<List<WorryLevel>> = inputAsGroups.map { it[1].extractAllNumbers() }

    // From each individual Monkey's perspective, its "world" can be seen as a modulus world!
    // Its only action depends on my worry level - but e.g.
    //  - a Monkey with testDivisor 3 treats 0, 3, 6, 9, 12, 15 all alike - it does not "see" a difference in them!
    //  - a Monkey with testDivisor 5 treats 0, 5, 10, 15 all alike
    //
    // As those two Monkeys share a common world, they *both* treat all numbers in modulus world 15 identical!
    // => this is the least common multiple of their respective divisors.

    val commonModulus = monkeys.map { it.testDivisor }.lcm().toInt()

    fun Monkey.playWithItemOfWorryLevel(worryLevel: WorryLevel): Pair<WorryLevel, Int> {
        val newWorryLevel = operation(worryLevel) % commonModulus
        val throwTo = if (newWorryLevel divisibleBy testDivisor) ifTrue else ifFalse
        return newWorryLevel to throwTo
    }

    fun letTheMonkeysPlay(rounds: Int): IntArray {
        val inspections = IntArray(monkeys.size)
        val currentState = startItems.map { it.toMutableList() }

        repeat(rounds) {
            monkeys.zip(currentState).forEach { (monkey, currentlyHolds) ->
                currentlyHolds.forEach {
                    val (newLevel, toMonkey) = monkey.playWithItemOfWorryLevel(it)
                    currentState[toMonkey] += newLevel
                }
                inspections[monkey.id] += currentlyHolds.size
                currentlyHolds.clear()
            }
        }

        return inspections
    }

    override fun part2(): Long {
        val totalInspections = letTheMonkeysPlay(10_000)
        return totalInspections.sortedDescending().take(2).product()
    }

}

fun main() {
    solve<Day11P2> {
        day11DemoInput part2 2713310158
    }
}

private fun createMonkeyFromPuzzle(id: Int, s: List<String>): Monkey {
    val (o, t, tr, fa) = s.drop(2)
    return Monkey(
        id,
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
