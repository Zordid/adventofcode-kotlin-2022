import kotlin.collections.associate
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4
import kotlin.collections.contains
import kotlin.collections.filter
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.set
import kotlin.collections.single
import kotlin.collections.toMutableMap

class Day21 : Day(21, 2022) {

    val p = input.map {
        val n = it.substringBefore(':')
        val num = it.extractAllLongs()
        if (num.isNotEmpty())
            n to num.single()
        else {
            val (_, a, op, b) = it.split(" ")
            n to Triple(a, op, b)
        }
    }.show()

    data class Monkey(val name: String)

    override fun part1(): Any? {

        val known = p.filter { it.second is Long }.associate { it.first to it.second as Long }.toMutableMap()
        val op = p.filter { it.second is Triple<*, *, *> }
            .associate { it.first to it.second as Triple<String, String, String> }.toMutableMap()

        while ("root" !in known && op.isNotEmpty()) {
            var goon = false
            val keys = op.keys.toList()
            for (name in keys) {
                val (an, o, bn) = op[name]!!
                val a = known[an] ?: an.toLongOrNull()
                val b = known[bn] ?: bn.toLongOrNull()
                if (a != null && b != null) {
                    known[name] = calc(a, o, b)
                    op.remove(name)
                } else if (a != null) {
                    op[name] = Triple(a.toString(), o, bn)
                } else if (b != null) {
                    op[name] = Triple(an, o, b.toString())
                }
                goon = true
            }
            if (!goon) {
                log { "We are stuck!" }


            }
        }

        return known["root"]

    }

    private fun calc(a: Long, op: String, b: Long): Long {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> a / b
            else -> error(op)
        }
    }
}

fun main() {
    solve<Day21>(true) {


    }
}