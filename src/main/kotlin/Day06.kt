class Day06 : Day(6, 2022, "Tuning Trouble") {

    private val signal = inputAsString

    override fun part1() = signal.detectStartOfMessage(4)

    override fun part2() = signal.detectStartOfMessage(14)

    private fun String.detectStartOfMessage(markerLength: Int) =
        asIterable().windowed(markerLength).indexOfFirst { it.countDistinct() == it.size } + markerLength

    private fun <T> Iterable<T>.countDistinct(): Int = toSet().size

}

fun main() {
    solve<Day06>()
}