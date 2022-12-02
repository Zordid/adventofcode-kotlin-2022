class Day02 : Day(2, 2022, "Rock Paper Scissors") {

    private val p = input.map { it.split(" ").map(String::first) }

    // Rock == 0, Paper == 1, Scissors == 2

    override fun part1() = p.sumOf { (f, s) ->
        val (opponent, you) = f - 'A' to s - 'X'
        score(opponent, you)
    }

    override fun part2() = p.sumOf { (f, s) ->
        val opponent = f - 'A'
        val you = (opponent + (s - 'Y').mod(3))
        score(opponent, you)
    }

    private fun score(opponent: Int, you: Int) = you + 1 + when (you) {
        opponent -> 3           // tie
        (opponent + 1) % 3 -> 6 // you won
        else -> 0               // you lost
    }

}

fun main() {
    solve<Day02>(
        """
        A Y
        B X
        C Z
    """.trimIndent(), 15, 12
    )
}