import utils.*

class Day10 : Day(10, 2022) {

    val p = input

    data class State(val cycle: Int, val x: Int)



    override fun part2(): Any? {

        fun noop(s: State): State = s.copy(cycle=s.cycle+1)
        fun addx1(s: State): State = noop(s)
        fun addx2( inc: Int): (State)->State = { it.copy(it.cycle+1, it.x+inc)}

        val mops = p.flatMap {
            val (c, p) = "$it 0".split(" ")
            when (c) {
                "noop"-> listOf(::noop)
                "addx"-> listOf(::addx1, addx2(p.toInt()))
                else -> error(it)
            }
        }


        val ci = listOf(20, 60, 100, 140, 180, 220)

        var cycle = 0
        var s = State(0, 1)
        var sum = 0
        for (l in mops.asInfiniteSequence()) {
            cycle++
            println("Start of $cycle, $s")
            if (cycle in ci) {
                sum += cycle * s.x
            }

            s = l(s)
            if (cycle>ci.last()) break
        }


        return sum
    }

}

fun main() {
    solve<Day10>(true) {


        """
            noop
            addx 3
            addx -5
        """.trimIndent()

"""
    addx 15
    addx -11
    addx 6
    addx -3
    addx 5
    addx -1
    addx -8
    addx 13
    addx 4
    noop
    addx -1
    addx 5
    addx -1
    addx 5
    addx -1
    addx 5
    addx -1
    addx 5
    addx -1
    addx -35
    addx 1
    addx 24
    addx -19
    addx 1
    addx 16
    addx -11
    noop
    noop
    addx 21
    addx -15
    noop
    noop
    addx -3
    addx 9
    addx 1
    addx -3
    addx 8
    addx 1
    addx 5
    noop
    noop
    noop
    noop
    noop
    addx -36
    noop
    addx 1
    addx 7
    noop
    noop
    noop
    addx 2
    addx 6
    noop
    noop
    noop
    noop
    noop
    addx 1
    noop
    noop
    addx 7
    addx 1
    noop
    addx -13
    addx 13
    addx 7
    noop
    addx 1
    addx -33
    noop
    noop
    noop
    addx 2
    noop
    noop
    noop
    addx 8
    noop
    addx -1
    addx 2
    addx 1
    noop
    addx 17
    addx -9
    addx 1
    addx 1
    addx -3
    addx 11
    noop
    noop
    addx 1
    noop
    addx 1
    noop
    noop
    addx -13
    addx -19
    addx 1
    addx 3
    addx 26
    addx -30
    addx 12
    addx -1
    addx 3
    addx 1
    noop
    noop
    noop
    addx -9
    addx 18
    addx 1
    addx 2
    noop
    noop
    addx 9
    noop
    noop
    noop
    addx -1
    addx 2
    addx -37
    addx 1
    addx 3
    noop
    addx 15
    addx -21
    addx 22
    addx -6
    addx 1
    noop
    addx 2
    addx 1
    noop
    addx -10
    noop
    noop
    addx 20
    addx 1
    addx 2
    addx 2
    addx -6
    addx -11
    noop
    noop
    noop
""".trimIndent() part1 13140

    }
}