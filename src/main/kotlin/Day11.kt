import utils.lcm
import utils.product

class Day11 : Day(11, 2022) {

    val p = inputAsGroups.map {
        val (items, op, t, tr, fa) = it.drop(1)
        M(
            items.extractAllLongs().toMutableList(),
            getOp(op.substringAfter("new = ")),
            t.extractAllLongs().single(),
            tr.extractAllIntegers().single(),
            fa.extractAllIntegers().single(),
        )
    }

    val base = p.map { it.test }.lcm()!!

    fun getOp(s: String): (Long) -> Long {
        val (o1, o, o2) = s.split(" ")

        return when (o) {
            "+" -> { old -> o1.toLongOrNull()?:old + (o2.toLongOrNull()?:old) }
            "*" -> { old -> o1.toLongOrNull()?:old * (o2.toLongOrNull()?:old) }
            else -> error(s)
        }
    }

    inner  class M(val wl: MutableList<Long>, val operation: (Long) -> Long, val test: Long, val t: Int, val f: Int,
        var inspections: Int = 0) {

        fun inspections() {
            wl.forEach {
                inspections++
                val nlevel = operation(it) % base
                if (nlevel % test == 0L) {
                    p[t].wl += nlevel
                } else {
                    p[f].wl += nlevel
                }
            }
            wl.clear()
        }

        override fun toString(): String {
            return wl.toString()
        }
    }

    override fun part2(): Any? {

        println(base)

        repeat(10_000) {
            p.forEach { it.inspections() }
        }

        p.forEach { println(it.inspections) }

        return p.sortedByDescending { it.inspections }.map { it.inspections }.take(2).product()
    }

}

fun main() {
    solve<Day11>(true) {

"""
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
""".trimIndent() part2 2713310158

    }
}