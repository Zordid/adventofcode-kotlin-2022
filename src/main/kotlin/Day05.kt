class Day05 : Day(5, 2022, "Supply Stacks") {

    val initialStacks = createStacks(inputAsGroups.first())
    val instructions = inputAsGroups.last().map(::containedNumbers)

    override fun part1(): Any {
        val finalStacks = crateMover(initialStacks, instructions, oneByOne)
        return finalStacks.top()
    }

    override fun part2(): Any {
        val finalStacks = crateMover(initialStacks, instructions, enBlock)
        return finalStacks.top()
    }

    fun crateMover(
        initialStacks: List<Stack>,
        instructions: List<List<Int>>,
        stackingOrder: (List<Char>) -> List<Char>
    ): List<Stack> =
        instructions.fold(initialStacks) { stacks, (q, from, to) ->
            stacks.mapIndexed { index, stack ->
                when (index) {
                    from -> stack.dropLast(q)
                    to -> stack + stackingOrder(stacks[from].takeLast(q))
                    else -> stack
                }
            }
        }

    val enBlock = { crates: List<Char> -> crates }
    val oneByOne = { crates: List<Char> -> crates.reversed() }

    private fun List<List<Char>>.top() = mapNotNull { it.lastOrNull() }.joinToString("")

    private fun createStacks(drawing: List<String>): List<Stack> =
        listOf(emptyList<Char>()) +  // add one empty stack at index 0 to accommodate indexing with "+1"
                drawing.reversed()   // turn it upside down
                    .let { pure ->
                        val columnIndices = pure.first().withIndex().filter { it.value.isDigit() }
                        columnIndices.sortedBy { it.value }.map { (index, _) ->
                            pure.drop(1).mapNotNull { it.getOrNull(index)?.takeIf(Char::isLetter) }
                        }
                    }

    private fun containedNumbers(s: String) = s.split(Regex("\\D+")).mapNotNull { it.toIntOrNull() }

}

private typealias Stack = List<Char>

fun main() {
    solve<Day05> {
        """
       [D] 
[N]    [C] 
[Z] [P][M] 
 1   3  2  

move 1 from 2 to 1
move 3 from 1 to 3
move 2 from 2 to 1
move 1 from 1 to 2
    """.trimIndent()("CMZ", "MCD")
    }
}
