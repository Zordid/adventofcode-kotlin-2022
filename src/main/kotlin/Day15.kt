import utils.*
import kotlin.math.absoluteValue

class Day15 : Day(15, 2022, "Beacon Exclusion Zone") {

    private val sensorsAndBeacons = input.map { it.extractAllIntegers().chunked(2).map(::asPoint) }

    override fun part1(): Int {
        val rowOfInterest = if (testInput) 10 else 2000000

        val impossible = sensorsAndBeacons.mapNotNull { sb ->
            sb.scannerRangeOnRow(rowOfInterest)
        }.merge()
        log { impossible }

        val beaconsAndSensors = sensorsAndBeacons.flatten().filter { it.y == rowOfInterest }.map { it.x }.toSet()
        log { beaconsAndSensors }

        return impossible.sumOf { it.last - it.first + 1 } - beaconsAndSensors.size
    }

    override fun part2(): Long {
        val rangeOfInterest = if (testInput) 0..20 else 0..4000000

        for (l in rangeOfInterest) {
            val impossible = sensorsAndBeacons.mapNotNull { sb ->
                sb.scannerRangeOnRow(l)
            }.merge()

            if (impossible.size > 1) {
                return (impossible.first().last + 1L) * 4000000L + l
            }
        }
        error("not found")
    }

    private fun List<Point>.scannerRangeOnRow(l: Int): IntRange? {
        val (s, b) = this
        val m = s manhattanDistanceTo b
        val dtoL = (s.y - l).absoluteValue
        return if (dtoL > m) {
            null
        } else {
            val onItsRow = 2 * m + 1
            val onRow = (onItsRow - (dtoL.absoluteValue) * 2)
            val span = m - dtoL
            val r = if (onRow > 0) (s.x - span..(s.x + span)) else null
            r
        }
    }

}

fun main() {
    solve<Day15> {

        """
            Sensor at x=2, y=18: closest beacon is at x=-2, y=15
            Sensor at x=9, y=16: closest beacon is at x=10, y=16
            Sensor at x=13, y=2: closest beacon is at x=15, y=3
            Sensor at x=12, y=14: closest beacon is at x=10, y=16
            Sensor at x=10, y=20: closest beacon is at x=10, y=16
            Sensor at x=14, y=17: closest beacon is at x=10, y=16
            Sensor at x=8, y=7: closest beacon is at x=2, y=10
            Sensor at x=2, y=0: closest beacon is at x=2, y=10
            Sensor at x=0, y=11: closest beacon is at x=2, y=10
            Sensor at x=20, y=14: closest beacon is at x=25, y=17
            Sensor at x=17, y=20: closest beacon is at x=21, y=22
            Sensor at x=16, y=7: closest beacon is at x=15, y=3
            Sensor at x=14, y=3: closest beacon is at x=15, y=3
            Sensor at x=20, y=1: closest beacon is at x=15, y=3
        """.trimIndent() part1 26 part2 56000011
    }
}