import utils.MutableGrid
import utils.formatted

class Day10 : Day(10, 2022, "Cathode-Ray Tube") {

    data class State(val x: Int = 1)

    private fun noop(s: State): State = s
    private fun add(inc: Int): (State) -> State = { it.copy(x = it.x + inc) }

    val microOps = input.flatMap {
        val (mne, param) = "$it 0".split(" ")
        when (mne) {
            "noop" -> listOf(::noop)
            "addx" -> listOf(::noop, add(param.toInt()))
            else -> error(mne)
        }
    }

    inline fun simulator(
        code: List<(State) -> State>,
        preCycle: (Int, State) -> Unit = { _, _ -> },
        postCycle: (Int, State) -> Unit = { _, _ -> },
    ): State {
        var state = State()
        var cycle = 0
        for (microOp in code) {
            cycle++
            preCycle(cycle, state)
            state = microOp(state)
            postCycle(cycle, state)
        }
        return state
    }

    override fun part1(): Int {
        val cyclesOfInterest = 20..220 step 40
        var signalStrengthSum = 0
        simulator(microOps,
            preCycle = { cycle, state ->
                if (cycle in cyclesOfInterest)
                    signalStrengthSum += cycle * state.x
            })
        return signalStrengthSum
    }

    override fun part2(): String {
        val screen = MutableGrid(40, 6) { ' ' }

        simulator(microOps,
            preCycle = { cycle, state ->
                val row = (cycle - 1) / 40
                val col = (cycle - 1) % 40
                val drawPixel = state.x in col - 1..col + 1
                if (drawPixel)
                    screen[row][col] = '#'
            }
        )

        return screen.formatted(showHeaders = false)
    }

}

fun main() {
    solve<Day10> {
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