@file:Suppress("MemberVisibilityCanBePrivate")

package day11breakdown.p1

import Day
import day11DemoInput
import extractAllIntegers
import solve
import utils.product

class Day11P1 : Day(11, 2022, "Monkey in the Middle") {

    val monkeys = inputAsGroups.map { description ->
        val (items, o, t, tr, fa) = description.drop(1)
        Monkey(
            items.extractAllIntegers().toMutableList(),
            createOperation(o.substringAfter("new = ")),
            t.extractAllIntegers().single(),
            tr.extractAllIntegers().single(),
            fa.extractAllIntegers().single(),
        )
    }

    private fun createOperation(s: String): (Int) -> Int {
        val (_, operator, op2) = s.split(" ")
        return when (operator) {
            "+" -> { old -> old + (op2.toIntOrNull() ?: old) }
            "*" -> { old -> old * (op2.toIntOrNull() ?: old) }
            else -> error(s)
        }
    }

    inner class Monkey(
        val itemWorryLevels: MutableList<Int>,
        val operation: (Int) -> Int,
        val test: Int,
        val ifTrue: Int,
        val ifFalse: Int,
        var inspections: Int = 0,
    ) {
        fun performInspections() {
            while (itemWorryLevels.isNotEmpty()) {
                val level = itemWorryLevels.removeFirst()
                inspections++
                val new = operation(level) / 3
                if (new isDivisibleBy test) {
                    monkeys[ifTrue].itemWorryLevels += new
                } else {
                    monkeys[ifFalse].itemWorryLevels += new
                }
            }
        }
    }

    override fun part1(): Long {
        repeat(20) {
            monkeys.forEach { it.performInspections() }
        }
        return monkeys.map { it.inspections }.sortedDescending().take(2).product()
    }

    private infix fun Int.isDivisibleBy(divisor: Int) = this % divisor == 0

}

fun main() {
    solve<Day11P1>(true) {
        day11DemoInput part1 10605
    }
}
