@file:Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode")

package day11breakdown.p1

import Day
import day11DemoInput
import extractAllIntegers
import extractAllNumbers
import solve
import utils.product

/**
 * I prefer "immutable Monkeys" over the mutable kind!
 *
 * After all, it's not a **monkey state** how worried I am, the Monkey acts independent of my state!
 */
data class Monkey(
    val id: Int,
    val operation: (Int) -> Int,
    val testDivisor: Int,
    val ifTrue: Int,
    val ifFalse: Int,
)

// could also use a Pair<Int, Int> instead, but this one might read better
data class MonkeyPlayResult(val newWorryLevel: Int, val throwTo: Int)

class Day11P1Fun : Day(11, 2022, "Monkey in the Middle") {

    val monkeys = inputAsGroups.mapIndexed(::createMonkeyFromPuzzle)
    val startItems: List<List<Int>> = inputAsGroups.map { it[1].extractAllNumbers() }

    fun Monkey.playWithItemOfWorryLevel(worryLevel: Int): MonkeyPlayResult {
        val newWorryLevel = operation(worryLevel) / 3
        val throwTo = if (newWorryLevel % testDivisor == 0) ifTrue else ifFalse
        return MonkeyPlayResult(newWorryLevel, throwTo)
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

    override fun part1(): Long {
        val totalInspections = letTheMonkeysPlay(20)
        return totalInspections.sortedDescending().take(2).product()
    }

}

fun main() {
    solve<Day11P1Fun> {
        day11DemoInput part1 10605
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

private fun createOperation(s: String): (Int) -> Int =
    s.split(" ").let { (_, operator, op2) ->
        when (operator) {
            "+" -> { old -> old + (op2.toIntOrNull() ?: old) }
            "*" -> { old -> old * (op2.toIntOrNull() ?: old) }
            else -> error(s)
        }
    }
