import Day13.PacketComparator.compareToPacket
import utils.product

class Day13 : Day(13, 2022, "Distress Signal") {

    private val signalPairs = inputAsGroups

    object PacketComparator : Comparator<String> {
        override fun compare(o1: String, o2: String): Int =
            o1 compareToPacket o2

        private fun String.elements(): List<Any> = buildList {
            val s = this@elements
            require(s.startsWith('[') && s.endsWith(']')) { s }
            var idx = 1
            while (s[idx] != ']') {
                add(
                    when (s[idx]) {
                        in '0'..'9' -> s.sequenceContainedIntegers(idx).first()
                        '[' -> {
                            var brackets = 1
                            var end = idx + 1
                            while (brackets > 0) {
                                if (s[end] == '[') brackets++
                                if (s[end] == ']') brackets--
                                end++
                            }
                            s.substring(idx, end)
                        }

                        else -> error(s.drop(idx))
                    }
                )
                idx += last().toString().length
                if (s[idx] == ',') idx++
            }
        }

        infix fun String.compareToPacket(other: String): Int =
            elements().zip(other.elements())
                .map { (l, r) ->
                    when {
                        (l is Int && r is Int) -> l.compareTo(r)
                        (l is String && r is String) -> l.compareToPacket(r)
                        (l is Int && r is String) -> "[$l]".compareToPacket(r)
                        (l is String && r is Int) -> l.compareToPacket("[$r]")
                        else -> error("$l $r")
                    }
                }.dropWhile { it == 0 }
                .firstOrNull() ?: elements().size.compareTo(other.elements().size)
    }

    override fun part1() =
        signalPairs.withIndex()
            .filter { (_, p) -> p.first() compareToPacket p.last() == -1 }
            .sumOf { it.index + 1 }

    private val dividerPackets = listOf("[[2]]", "[[6]]")

    override fun part2() =
        (signalPairs.flatten() + dividerPackets)
            .sortedWith(PacketComparator)
            .withIndex()
            .filter { it.value in dividerPackets }
            .map { it.index + 1 }.product()

}

fun main() {
    solve<Day13> {
        """
            [1,1,3,1,1]
            [1,1,5,1,1]
        
            [[1],[2,3,4]]
            [[1],4]
        
            [9]
            [[8,7,6]]
        
            [[4,4],4,4]
            [[4,4],4,4,4]
        
            [7,7,7,7]
            [7,7,7]
        
            []
            [3]
        
            [[[]]]
            [[]]
        
            [1,[2,[3,[4,[5,6,7]]]],8,9]
            [1,[2,[3,[4,[5,6,0]]]],8,9]
        """.trimIndent() part1 13 part2 140
    }
}