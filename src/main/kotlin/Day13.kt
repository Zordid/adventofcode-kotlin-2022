import utils.product

class Day13 : Day(13, 2022, "Distress Signal") {

    private val signalPairs = inputAsGroups.map {
        it.map(PacketElement::Packet)
    }

    sealed interface PacketElement : Comparable<PacketElement> {

        fun elements(): Sequence<PacketElement>

        @JvmInline
        value class Packet(private val data: String) : PacketElement {
            init {
                require(data.startsWith('[')) { "Not a valid packet: $data" }
            }

            override fun elements(): Sequence<PacketElement> = sequence {
                with(data) {
                    var idx = 1
                    while (get(idx) != ']') {
                        when (get(idx)) {
                            in '0'..'9' -> sequenceContainedIntegers(idx).first()
                                .also {
                                    yield(Integer(it))
                                    idx += "$it".length
                                }

                            '[' -> {
                                yield(Packet(substring(idx)))
                                idx = findClosingBracket(idx) + 1
                            }

                            ',' -> idx++

                            else -> error(drop(idx))
                        }
                    }
                }
            }
        }

        @JvmInline
        value class Integer(val int: Int) : PacketElement {
            override fun elements(): Sequence<PacketElement> = sequenceOf(this)
        }

        private object EndMarker : PacketElement {
            override fun elements() = emptySequence<PacketElement>()
        }

        override fun compareTo(other: PacketElement): Int = when {
            this === other -> 0
            this is EndMarker -> -1
            other is EndMarker -> +1
            this is Integer && other is Integer -> int.compareTo(other.int)
            else -> (elements() + EndMarker).zip((other.elements() + EndMarker)) { left, right ->
                left.compareTo(right)
            }.dropWhile { it == 0 }.firstOrNull() ?: 0
        }

    }

    fun Packet(data: String): PacketElement = PacketElement.Packet(data)

    override fun part1() =
        signalPairs.withIndex()
            .filter { (_, p) -> p.first() < p.last() }
            .sumOf { it.index + 1 }

    private val dividerPackets = listOf(Packet("[[2]]"), Packet("[[6]]"))

    override fun part2() =
        (signalPairs.flatten() + dividerPackets)
            .sorted()
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

// TODO: candidate for lib
fun String.findClosingBracket(startIndex: Int = 0, open: Char = '[', close: Char = ']'): Int {
    require(get(startIndex) == open) { "Given string does not start with opening bracket $open: ${this.drop(startIndex)}!" }
    var level = 1
    (startIndex + 1..lastIndex).forEach { index ->
        when (get(index)) {
            open -> level++
            close -> level--
        }
        if (level == 0) return index
    }
    error("No matching closing bracket found in ${this.drop(startIndex)}.")
}

fun String.tokenize(delimiters: String, startIndex: Int = 0): Sequence<String> = sequence {
    var idx = startIndex.coerceAtLeast(0)
    while (idx <= lastIndex) {
        var nextToken = idx
        while (nextToken <= lastIndex && get(nextToken) !in delimiters) {
            nextToken++
        }
        if (nextToken > lastIndex) break // ran till the end of the string

        // token at exactly nextToken position found!
        if (nextToken > idx) yield(substring(idx, nextToken + 1))
        yield(substring(nextToken, nextToken + 1))
        idx = nextToken + 1
    }
    if (idx <= lastIndex)
        yield(substring(idx))
}