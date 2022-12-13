class Day13 : Day(13, 2022, "Distress Signal") {

    val pairs = inputAsGroups


    companion object {
        var level = 0

        fun String.elements(): List<Any> = buildList {
            val s = this@elements
            require(s.first() == '[' && s.last() == ']') { s }
            var idx = 1
            while (s[idx] != ']') {
                if (s[idx].isDigit()) add(s.drop(idx).sequenceContainedIntegers().first()) else
                    if (s[idx] == '[') {
                        var b = 1
                        var end = idx + 1
                        while (b > 0) {
                            if (s[end] == '[') b++
                            if (s[end] == ']') b--
                            end++
                        }
                        add(s.substring(idx, end))
                    }
                idx += this.last().toString().length
                if (s[idx] == ',') idx++
            }
        }


        fun compareS(pair: List<String>): Int {
            val (left: String, right: String) = pair
            val l = left.elements()
            val r = right.elements()
            println(" ".repeat(level * 2) + "Compare $l vs $r")

            level++
            return l.zip(r).map { (l, r) ->
                println(" ".repeat(level * 2) + "Compare $l vs $r")
                when {
                    (l is Int && r is Int) -> l.compareTo(r)
                    (l is String && r is String) -> compareS(listOf(l, r))
                    (l is Int && r is String) -> compareS(listOf("[$l]", r))
                    (l is String && r is Int) -> compareS(listOf(l, "[$r]"))

                    else -> error("$l $r")
                }
            }.dropWhile { it == 0 }.firstOrNull().also {
                level--
                print(" ".repeat(level * 2))
                println(it)
            } ?: if (l.size < r.size) -1 else if (l.size == r.size) 0 else +1
        }
    }

    override fun part1(): Int {
        return pairs.withIndex().filter { compareS(it.value) == -1 }.map { it.index + 1 }.also { println(it) }.sum()
    }

    object C : Comparator<String> {
        override fun compare(o1: String?, o2: String?): Int {
            return compareS(listOf(o1.orEmpty(), o2.orEmpty()))
        }
    }

    override fun part2(): Int {
        val x = (pairs.flatten() + "[[2]]" + "[[6]]").sortedWith(C)
        return (x.indexOf("[[2]]") + 1) * (x.indexOf("[[6]]") + 1)
    }

}

fun main() {
    solve<Day13>(true) {

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