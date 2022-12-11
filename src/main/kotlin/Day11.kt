import utils.product

class Day11 : Day(11, 2022) {

    val p = inputAsGroups.map {
        val (items, op, t, tr, fa) = it.drop(1)
        M(
            items.extractAllIntegers().toMutableList(),
            getOp(op.substringAfter("new = ")),
            getTest(t.substringAfter("Test: ")),
            tr.extractAllIntegers().single(),
            fa.extractAllIntegers().single(),
        )
    }

    fun getOp(s: String): (Int) -> Int {
        val (o1, o, o2) = s.split(" ")

        return when (o) {
            "+" -> { old -> o1.toIntOrNull()?:old + (o2.toIntOrNull()?:old) }
            "*" -> { old -> o1.toIntOrNull()?:old * (o2.toIntOrNull()?:old) }
            "-" -> { old -> o1.toIntOrNull()?:old - (o2.toIntOrNull()?:old) }
            "/" -> { old -> o1.toIntOrNull()?:old / (o2.toIntOrNull()?:old) }
            else -> error(s)
        }
    }

    fun getTest(s: String): (Int) -> Boolean {
        val (d, _, b) = s.split(" ")
        return when (d) {
            "divisible" -> { i -> i % b.toInt() == 0 }
            else -> error(s)
        }
    }

    inner  class M(val wl: MutableList<Int>, val operation: (Int) -> Int, val test: (Int) -> Boolean, val t: Int, val f: Int,
        var inspections: Int = 0) {

        fun inspections() {
            wl.forEach {
                inspections++
                println("inspecting $it")
                val nlevel = operation(it) / 3
                println("now $nlevel")
                if (test(nlevel)) {
                    println("throw at $t")
                    p[t].wl += nlevel
                } else {
                    println("throw at $f")
                    p[f].wl += nlevel
                }
            }
            wl.clear()
        }

        override fun toString(): String {
            return wl.toString()
        }
    }

    override fun part1(): Any? {


        repeat(20) {
            println("After round $it")
            p.forEach { println(it) }
            p.forEach { it.inspections() }
            println()
        }
        println("After end")
        p.forEach { println(it) }

        println()
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
""".trimIndent() part1 10605

    }
}