const val SNAFU_BASE = 5L

class Day25 : Day(25, 2022, "Full of Hot Air") {

    val puzzle = input

    fun String.toDecimal(): Long {
        var n = 0L
        var b = 1L
        this.reversed().forEach {
            when (it) {
                '1' -> n += b
                '2' -> n += 2 * b
                '-' -> n -= b
                '=' -> n -= 2 * b
            }
            b *= SNAFU_BASE
        }
        return n
    }

    fun Long.toSnafu(): String {
        var n = this

        var result = ""
        do {
            val d = (n % SNAFU_BASE).toInt()
            n -= d
            if (d > 2) n += SNAFU_BASE
            result += when (d) {
                3 -> '='
                4 -> '-'
                else -> '0' + d
            }
            n /= 5
        } while (n != 0L)

        return result.reversed()
    }

    override fun part1() = puzzle.sumOf { it.toDecimal() }.toSnafu()

}

fun main() {
    solve<Day25> {

        """
            1=-0-2
            12111
            2=0=
            21
            2=01
            111
            20012
            112
            1=-1=
            1-12
            12
            1=
            122
        """.trimIndent() part1 "2=-1=0"

    }
}