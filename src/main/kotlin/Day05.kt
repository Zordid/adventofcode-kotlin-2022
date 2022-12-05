class Day05 : Day(5, 2022, "Supply Stacks") {

    private val initialStacks = createStacks(inputAsGroups.first())
    private val instructions = inputAsGroups.last().map(::containedNumbers)

    override fun part1(): Any {
        val finalStacks = instructions.fold(initialStacks) { stacks, (q, from, to) ->
            stacks.mapIndexed { index, stack ->
                when (index) {
                    from -> stack.dropLast(q)
                    to -> stack + stacks[from].takeLast(q).reversed()
                    else -> stack
                }
            }
        }

        return finalStacks.top().joinToString("")
    }

    override fun part2(): Any {
        val finalStacks = instructions.fold(initialStacks) { stacks, (q, from, to) ->
            stacks.mapIndexed { index, stack ->
                when (index) {
                    from -> stack.dropLast(q)
                    to -> stack + stacks[from].takeLast(q)
                    else -> stack
                }
            }
        }

        return finalStacks.top().joinToString("")
    }

    private fun List<List<Char>>.top() =
        mapNotNull { it.lastOrNull() }

    private fun createStacks(drawing: List<String>) =
        listOf(emptyList<Char>()) +  // add one empty stack add index 0 to accommodate indexing
                drawing.reversed() // turn it upside down
                    .drop(1)    // and ignore the numbering
                    .let { pure ->
                        val columnIndices = pure.first().withIndex().filter { it.value.isLetter() }
                        columnIndices.map { (index, _) ->
                            pure.mapNotNull { it.getOrNull(index)?.takeIf(Char::isLetter) }
                        }
                    }

    private fun containedNumbers(s: String) = s.split(Regex("\\D+")).mapNotNull { it.toIntOrNull() }

}

fun main() {
    solve<Day05>(
        """
    [D]    
[N] [C]    
[Z] [M] [P]
 1   2   3 

move 1 from 2 to 1
move 3 from 1 to 3
move 2 from 2 to 1
move 1 from 1 to 2
    """.trimIndent(), "CMZ", "MCD"
    )
}
